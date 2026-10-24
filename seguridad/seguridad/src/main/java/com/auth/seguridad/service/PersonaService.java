package com.auth.seguridad.service;

import com.auth.seguridad.dto.PersonaDto;
import com.auth.seguridad.dto.RegistroClienteDto;
import java.util.List;

public interface PersonaService {
    List<PersonaDto> listarTodos();
    PersonaDto buscarPorId(Integer id);
    PersonaDto guardar(PersonaDto dto);
    PersonaDto registrarCliente(RegistroClienteDto dto);
    PersonaDto actualizar(Integer id, PersonaDto dto);
    void eliminar(Integer id);
}