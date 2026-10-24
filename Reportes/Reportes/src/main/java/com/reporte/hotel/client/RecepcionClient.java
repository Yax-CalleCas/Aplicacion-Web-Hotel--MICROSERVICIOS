package com.reporte.hotel.client;




import com.reporte.hotel.dto.ApiResponse;
import com.reporte.hotel.dto.RecepcionResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "recepcion-service")
public interface RecepcionClient {

    @GetMapping("/api/recepcion/listar")
    ApiResponse<List<RecepcionResponseDTO>> listarTodasLasRecepciones(
            @RequestParam(required = false) Boolean estado
    );
}