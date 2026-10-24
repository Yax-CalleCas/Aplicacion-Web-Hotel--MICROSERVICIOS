package com.reporte.hotel.service;

import com.reporte.hotel.client.RecepcionClient;
import com.reporte.hotel.dto.ApiResponse;
import com.reporte.hotel.dto.RecepcionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteRecepcionService {

    private final RecepcionClient recepcionClient;

    // 1. Reporte de alquileres de habitaciones (Historial, activas o finalizadas)
    public List<RecepcionResponseDTO> obtenerReporteAlquileres(Boolean estado) {
        ApiResponse<List<RecepcionResponseDTO>> response = recepcionClient.listarTodasLasRecepciones(estado);

        if (!response.isSuccess() || response.getData() == null) {
            throw new RuntimeException("No se pudieron obtener las recepciones desde el servicio.");
        }

        return response.getData();
    }

    // 2. Reporte de Cuentas por Cobrar (Estancias activas con saldo pendiente mayor a 0)
    public List<RecepcionResponseDTO> obtenerCuentasPorCobrar() {
        ApiResponse<List<RecepcionResponseDTO>> response = recepcionClient.listarTodasLasRecepciones(true); // Solo activas

        if (!response.isSuccess() || response.getData() == null) {
            throw new RuntimeException("No se pudieron obtener las recepciones activas.");
        }

        return response.getData().stream()
                .filter(r -> r.getPrecioRestante() != null && r.getPrecioRestante().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
    }

    // 3. Reporte de Penalidades Aplicadas
    public Map<String, Object> obtenerResumenPenalidades() {
        ApiResponse<List<RecepcionResponseDTO>> response = recepcionClient.listarTodasLasRecepciones(null); // Todas

        if (!response.isSuccess() || response.getData() == null) {
            throw new RuntimeException("Error al consultar las recepciones.");
        }

        List<RecepcionResponseDTO> lista = response.getData();

        List<RecepcionResponseDTO> conPenalidad = lista.stream()
                .filter(r -> r.getCostoPenalidad() != null && r.getCostoPenalidad().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        BigDecimal totalPenalidadesRecaudado = conPenalidad.stream()
                .map(RecepcionResponseDTO::getCostoPenalidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "cantidadHabitacionesMultadas", conPenalidad.size(),
                "montoTotalPenalidades", totalPenalidadesRecaudado,
                "detalleRecepciones", conPenalidad
        );
    }
}