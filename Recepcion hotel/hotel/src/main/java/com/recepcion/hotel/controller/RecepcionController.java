package com.recepcion.hotel.controller;

import com.recepcion.hotel.dto.ApiResponse;
import com.recepcion.hotel.dto.RecepcionRequestDTO;
import com.recepcion.hotel.dto.RecepcionResponseDTO;
import com.recepcion.hotel.dto.SalidaRequest;
import com.recepcion.hotel.service.RecepcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recepcion")
@RequiredArgsConstructor
@Slf4j
public class RecepcionController {

    private final RecepcionService recepcionService;

    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<List<RecepcionResponseDTO>>> listar(
            @RequestParam(required = false) Boolean estado) {
        List<RecepcionResponseDTO> lista;
        if (estado != null) {
            lista = recepcionService.listarPorEstado(estado);
        } else {
            lista = recepcionService.listarTodas();
        }
        return ResponseEntity.ok(ApiResponse.success(lista, "Listado de recepciones obtenido correctamente"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecepcionResponseDTO>> obtenerPorId(@PathVariable Integer id) {
        RecepcionResponseDTO recepcion = recepcionService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(recepcion, "Recepción encontrada"));
    }

    @GetMapping("/habitacion-activa/{idHabitacion}")
    public ResponseEntity<ApiResponse<RecepcionResponseDTO>> buscarActivaPorHabitacion(@PathVariable Integer idHabitacion) {
        RecepcionResponseDTO recepcion = recepcionService.buscarActivaPorHabitacion(idHabitacion);
        return ResponseEntity.ok(ApiResponse.success(recepcion, "Recepción activa encontrada para la habitación"));
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<ApiResponse<List<RecepcionResponseDTO>>> listarPorCliente(@PathVariable Integer idCliente) {
        List<RecepcionResponseDTO> lista = recepcionService.listarPorCliente(idCliente);
        return ResponseEntity.ok(ApiResponse.success(lista, "Recepciones del cliente obtenidas correctamente"));
    }

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<RecepcionResponseDTO>> registrar(@Valid @RequestBody RecepcionRequestDTO request) {
        log.info("Registrando recepción (Check-in) y publicando evento RabbitMQ para habitación ID: {}", request.getIdHabitacion());
        RecepcionResponseDTO nuevaRecepcion = recepcionService.registrarCheckIn(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(nuevaRecepcion, "Check-in realizado con éxito"));
    }

    @PostMapping("/registrar-salida/{id}")
    public ResponseEntity<ApiResponse<RecepcionResponseDTO>> registrarSalida(
            @PathVariable Integer id,
            @Valid @RequestBody SalidaRequest request) {
        log.info("Procesando salida (Check-out) de recepción ID: {} y enviando habitación a LIMPIEZA vía RabbitMQ", id);
        RecepcionResponseDTO recepcionFinalizada = recepcionService.registrarCheckOut(id, request);
        return ResponseEntity.ok(ApiResponse.success(recepcionFinalizada, "Salida procesada con éxito"));
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<ApiResponse<RecepcionResponseDTO>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody RecepcionRequestDTO dto) {
        log.info("Actualizando recepción ID: {}", id);
        RecepcionResponseDTO actualizada = recepcionService.actualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.success(actualizada, "Recepción actualizada correctamente"));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        log.info("Intentando eliminar/anular recepción ID: {}", id);
        recepcionService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Registro procesado correctamente"));
    }
}