package com.recepcion.hotel.client;



import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hotel-service", url = "${application.config.hotel-service-url:http://localhost:8081}")
public interface HotelClient {

    @PatchMapping("/api/v1/habitaciones/{id}/estado/{idEstado}")
    void actualizarEstadoHabitacion(
            @PathVariable("id") Integer id,
            @PathVariable("idEstado") Integer idEstado
    );
}