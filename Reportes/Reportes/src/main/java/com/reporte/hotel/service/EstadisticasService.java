package com.reporte.hotel.service;

import com.reporte.hotel.client.InventarioVentaClient;
import com.reporte.hotel.client.RecepcionClient;
import com.reporte.hotel.dto.*;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstadisticasService {

    private static final int UMBRAL_STOCK_BAJO = 5;
    private static final int TOP_PRODUCTOS = 5;
    private static final int TIMEOUT_SEGUNDOS = 8;

    private final InventarioVentaClient inventarioVentaClient;
    private final RecepcionClient recepcionClient;
    private final ApplicationEventPublisher publisher;

    // "Hoy" se calcula en la zona horaria del hotel, no en la del servidor
    @Value("${estadisticas.zona-horaria:America/Lima}")
    private String zonaHoraria;

    // 3 hilos: ventas, productos y recepciones se consultan EN PARALELO
    private final ExecutorService pool = Executors.newFixedThreadPool(3);

    // Último snapshot en memoria: los admins leen de aquí, no golpean los microservicios
    private final AtomicReference<DashboardEstadisticasDTO> ultimo = new AtomicReference<>();

    @PreDestroy
    void cerrar() {
        pool.shutdown();
    }

    public DashboardEstadisticasDTO obtenerUltimo() {
        DashboardEstadisticasDTO actual = ultimo.get();
        return actual != null ? actual : refrescar();
    }

    // fixedDelay: espera a terminar el ciclo anterior (no se pisan). Ajustable con estadisticas.refresco-ms
    @Scheduled(fixedDelayString = "${estadisticas.refresco-ms:5000}")
    public synchronized DashboardEstadisticasDTO refrescar() {
        try {
            long t0 = System.nanoTime();
            DashboardEstadisticasDTO snapshot = construirSnapshot();
            ultimo.set(snapshot);
            publisher.publishEvent(snapshot); // el controller lo empuja por SSE
            log.debug("Estadísticas refrescadas en {} ms", (System.nanoTime() - t0) / 1_000_000);
            return snapshot;
        } catch (Exception e) {
            log.warn("No se pudo refrescar estadísticas: {}", e.getMessage());
            return ultimo.get(); // si todo cae, se conserva el último dato bueno
        }
    }

    private DashboardEstadisticasDTO construirSnapshot() {
        LocalDate hoy = LocalDate.now(ZoneId.of(zonaHoraria));
        DashboardEstadisticasDTO previo = ultimo.get();

        // 💡 Capturar el contexto actual (incluyendo el Token JWT) para pasarlo a los hilos secundarios
        var contextAttributes = RequestContextHolder.getRequestAttributes();

        // Una sola llamada por microservicio, las tres a la vez de forma segura con el token propagado
        CompletableFuture<List<VentaDto>> fVentas = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        if (contextAttributes != null) RequestContextHolder.setRequestAttributes(contextAttributes);
                        return extraer(inventarioVentaClient.listarVentas(), "ventas");
                    } finally {
                        RequestContextHolder.resetRequestAttributes();
                    }
                }, pool)
                .orTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);

        CompletableFuture<List<ProductoDto>> fProductos = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        if (contextAttributes != null) RequestContextHolder.setRequestAttributes(contextAttributes);
                        return extraer(inventarioVentaClient.listarProductos(), "productos");
                    } finally {
                        RequestContextHolder.resetRequestAttributes();
                    }
                }, pool)
                .orTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);

        CompletableFuture<List<RecepcionResponseDTO>> fRecepciones = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        if (contextAttributes != null) RequestContextHolder.setRequestAttributes(contextAttributes);
                        return extraer(recepcionClient.listarTodasLasRecepciones(null), "recepciones");
                    } finally {
                        RequestContextHolder.resetRequestAttributes();
                    }
                }, pool)
                .orTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);

        List<VentaDto> ventas = esperar(fVentas, "ventas");
        List<ProductoDto> productos = esperar(fProductos, "productos");
        List<RecepcionResponseDTO> recepciones = esperar(fRecepciones, "recepciones");

        if (ventas == null && productos == null && recepciones == null) {
            throw new IllegalStateException("Ningún microservicio respondió");
        }

        DashboardEstadisticasDTO d = new DashboardEstadisticasDTO();
        d.setGeneradoEn(LocalDateTime.now(ZoneId.of(zonaHoraria)));

        // Cada bloque es independiente: si un microservicio falla, ese bloque conserva su último valor
        if (ventas != null) calcularVentas(d, ventas, hoy);
        else if (previo != null) copiarVentas(d, previo);

        if (productos != null) calcularStock(d, productos);
        else if (previo != null) copiarStock(d, previo);

        if (recepciones != null) calcularRecepciones(d, recepciones, hoy);
        else if (previo != null) copiarRecepciones(d, previo);

        d.setIngresoTotalHoy(dec(d.getIngresoVentasHoy()).add(dec(d.getIngresoHospedajeHoy())));
        return d;
    }

    // ---------------------------------------------------------------- VENTAS (una sola pasada)

    private void calcularVentas(DashboardEstadisticasDTO d, List<VentaDto> ventas, LocalDate hoy) {
        int ventasHoy = 0;
        BigDecimal ingresoHoy = BigDecimal.ZERO;
        Map<Integer, Acumulado> porProducto = new HashMap<>();

        for (VentaDto v : ventas) {
            if (!(v.getEstado() == null || "COMPLETADA".equalsIgnoreCase(v.getEstado()))) continue;

            if (v.getFechaCreacion() != null && v.getFechaCreacion().toLocalDate().equals(hoy)) {
                ventasHoy++;
                ingresoHoy = ingresoHoy.add(dec(v.getTotal()));
            }

            if (v.getDetalleVentas() == null) continue;
            for (DetalleVentaDto det : v.getDetalleVentas()) {
                if (det.getIdProducto() == null) continue;
                Acumulado a = porProducto.computeIfAbsent(det.getIdProducto(), k -> new Acumulado());
                if (a.nombre == null) a.nombre = det.getNombreProducto();
                a.cantidad += ent(det.getCantidad());
                a.ingreso = a.ingreso.add(dec(det.getSubTotal()));
            }
        }

        List<ReporteProductoPopularDTO> top = porProducto.entrySet().stream()
                .sorted((x, y) -> {
                    int c = Integer.compare(y.getValue().cantidad, x.getValue().cantidad);
                    return c != 0 ? c : Integer.compare(x.getKey(), y.getKey()); // desempate estable
                })
                .limit(TOP_PRODUCTOS)
                .map(e -> ReporteProductoPopularDTO.builder()
                        .idProducto(e.getKey())
                        .nombreProducto(e.getValue().nombre != null ? e.getValue().nombre : "Producto Desconocido")
                        .cantidadVendida(e.getValue().cantidad)
                        .ingresoGenerado(e.getValue().ingreso)
                        .fechaCorte(hoy)
                        .build())
                .toList();

        d.setVentasHoy(ventasHoy);
        d.setIngresoVentasHoy(ingresoHoy);
        d.setTopProductos(top);
    }

    // ---------------------------------------------------------------- STOCK

    private void calcularStock(DashboardEstadisticasDTO d, List<ProductoDto> productos) {
        List<ProductoDto> stockBajo = productos.stream()
                .filter(p -> p.getCantidad() != null && p.getCantidad() <= UMBRAL_STOCK_BAJO)
                .filter(p -> !Boolean.FALSE.equals(p.getEstado())) // ignora productos dados de baja
                .sorted(Comparator.comparingInt(ProductoDto::getCantidad))
                .toList();
        d.setCantidadStockBajo(stockBajo.size());
        d.setProductosStockBajo(stockBajo);
    }

    // ---------------------------------------------------------------- RECEPCIÓN (una sola pasada)

    private void calcularRecepciones(DashboardEstadisticasDTO d, List<RecepcionResponseDTO> lista, LocalDate hoy) {
        int activas = 0, checkInsHoy = 0, salidasPrevistas = 0, salidasHoy = 0, porCobrar = 0, penalCant = 0;
        BigDecimal montoPorCobrar = BigDecimal.ZERO, ingresoHospedaje = BigDecimal.ZERO, penalMonto = BigDecimal.ZERO;

        for (RecepcionResponseDTO r : lista) {
            if (Boolean.TRUE.equals(r.getEstado())) {
                activas++;
                if (r.getPrecioRestante() != null && r.getPrecioRestante().signum() > 0) {
                    porCobrar++;
                    montoPorCobrar = montoPorCobrar.add(r.getPrecioRestante());
                }
                if (hoy.equals(r.getFechaSalida())) salidasPrevistas++;
            }

            if (hoy.equals(r.getFechaEntrada())) checkInsHoy++;

            LocalDateTime cierre = r.getFechaSalidaConfirmacion();
            if (cierre != null && cierre.toLocalDate().equals(hoy)) {
                salidasHoy++;
                ingresoHospedaje = ingresoHospedaje.add(dec(r.getTotalPagado()));
                if (r.getCostoPenalidad() != null && r.getCostoPenalidad().signum() > 0) {
                    penalCant++;
                    penalMonto = penalMonto.add(r.getCostoPenalidad());
                }
            }
        }

        d.setEstanciasActivas(activas);
        d.setCheckInsHoy(checkInsHoy);
        d.setSalidasPrevistasHoy(salidasPrevistas);
        d.setSalidasHoy(salidasHoy);
        d.setCuentasPorCobrarCantidad(porCobrar);
        d.setMontoPorCobrar(montoPorCobrar);
        d.setIngresoHospedajeHoy(ingresoHospedaje);
        d.setPenalidadesHoyCantidad(penalCant);
        d.setPenalidadesHoyMonto(penalMonto);
    }

    // ---------------------------------------------------------------- copia del último valor bueno

    private void copiarVentas(DashboardEstadisticasDTO d, DashboardEstadisticasDTO p) {
        d.setVentasHoy(p.getVentasHoy());
        d.setIngresoVentasHoy(p.getIngresoVentasHoy());
        d.setTopProductos(p.getTopProductos());
    }

    private void copiarStock(DashboardEstadisticasDTO d, DashboardEstadisticasDTO p) {
        d.setCantidadStockBajo(p.getCantidadStockBajo());
        d.setProductosStockBajo(p.getProductosStockBajo());
    }

    private void copiarRecepciones(DashboardEstadisticasDTO d, DashboardEstadisticasDTO p) {
        d.setEstanciasActivas(p.getEstanciasActivas());
        d.setCheckInsHoy(p.getCheckInsHoy());
        d.setSalidasPrevistasHoy(p.getSalidasPrevistasHoy());
        d.setSalidasHoy(p.getSalidasHoy());
        d.setCuentasPorCobrarCantidad(p.getCuentasPorCobrarCantidad());
        d.setMontoPorCobrar(p.getMontoPorCobrar());
        d.setIngresoHospedajeHoy(p.getIngresoHospedajeHoy());
        d.setPenalidadesHoyCantidad(p.getPenalidadesHoyCantidad());
        d.setPenalidadesHoyMonto(p.getPenalidadesHoyMonto());
    }

    // ---------------------------------------------------------------- utilidades

    private static <T> List<T> extraer(ApiResponse<List<T>> r, String nombre) {
        if (r == null || !r.isSuccess() || r.getData() == null) {
            log.warn("Advertencia: {} no disponible temporalmente", nombre);
            return Collections.emptyList();
        }
        return r.getData();
    }

    private static <T> T esperar(CompletableFuture<T> f, String nombre) {
        try {
            return f.join();
        } catch (Exception e) {
            Throwable causa = e.getCause() != null ? e.getCause() : e;
            log.warn("Fallo al obtener {}: {}", nombre, causa.getMessage());
            return null;
        }
    }

    private static BigDecimal dec(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static int ent(Integer v) {
        return v != null ? v : 0;
    }

    private static final class Acumulado {
        String nombre;
        int cantidad;
        BigDecimal ingreso = BigDecimal.ZERO;
    }
}