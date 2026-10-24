package com.auth.seguridad.service.impl;

import com.auth.seguridad.config.RabbitMQConfig;
import com.auth.seguridad.dto.PersonaDto;
import com.auth.seguridad.dto.RegistroClienteDto;
import com.auth.seguridad.dto.SedeDto;
import com.auth.seguridad.dto.UserEventDto;
import com.auth.seguridad.entity.Persona;
import com.auth.seguridad.entity.Sede;
import com.auth.seguridad.entity.TipoPersona;
import com.auth.seguridad.exception.BusinessException;
import com.auth.seguridad.exception.ResourceNotFoundException;
import com.auth.seguridad.messaging.UserEventProducer;
import com.auth.seguridad.repository.PersonaRepository;
import com.auth.seguridad.repository.SedeRepository;
import com.auth.seguridad.repository.TipoPersonaRepository;
import com.auth.seguridad.service.PersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonaServiceImpl implements PersonaService {

    private static final String ROL_CLIENTE = "Cliente";

    private final PersonaRepository repository;
    private final SedeRepository sedeRepository;
    private final TipoPersonaRepository tipoPersonaRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserEventProducer eventProducer; // Productor de RabbitMQ

    @Override
    @Transactional(readOnly = true)
    public List<PersonaDto> listarTodos() {
        return repository.findAllConTipo().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaDto buscarPorId(Integer id) {
        return repository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado"));
    }

    @Override
    @Transactional
    public PersonaDto guardar(PersonaDto dto) {
        validarReglasNegocioCreacion(dto);

        TipoPersona tipoPersona = buscarTipoPersona(dto.getIdTipoPersona());
        Set<Sede> sedes = resolverSedes(dto.getSedes());

        Persona persona = Persona.builder()
                .tipoDocumento(dto.getTipoDocumento())
                .documento(dto.getDocumento())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .correo(dto.getCorreo())
                .clave(passwordEncoder.encode(dto.getClave()))
                .tipoPersona(tipoPersona)
                .sedes(sedes)
                .estado(true)
                .fotoUrl(dto.getFotoUrl() != null ? dto.getFotoUrl() : "")
                .build();

        Persona guardada = repository.save(persona);
        PersonaDto resultado = toDto(repository.findByCorreoWithTipo(guardada.getCorreo()).orElse(guardada));

        // Publicar evento de Creación hacia RabbitMQ
        publicarEvento(resultado, RabbitMQConfig.RoutingKeys.USER_CREATED, "CREATED");

        return resultado;
    }

    @Override
    @Transactional
    public PersonaDto registrarCliente(RegistroClienteDto dto) {
        TipoPersona cliente = tipoPersonaRepository.findByDescripcionIgnoreCase(ROL_CLIENTE)
                .orElseThrow(() -> new BusinessException("Error: El registro de clientes no está disponible."));
        if (!Boolean.TRUE.equals(cliente.getEstado())) {
            throw new BusinessException("Error: El registro de clientes no está disponible.");
        }

        PersonaDto base = PersonaDto.builder()
                .tipoDocumento(dto.getTipoDocumento())
                .documento(dto.getDocumento())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .correo(dto.getCorreo())
                .clave(dto.getClave())
                .idTipoPersona(cliente.getIdTipoPersona())
                .build();

        return guardar(base);
    }

    @Override
    @Transactional
    public PersonaDto actualizar(Integer id, PersonaDto dto) {
        Persona p = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado"));

        validarReglasNegocioActualizacion(dto, id);

        String claveFinal = (dto.getClave() == null || dto.getClave().isEmpty() || dto.getClave().equals("**********"))
                ? p.getClave() : passwordEncoder.encode(dto.getClave());

        TipoPersona tipoPersona = buscarTipoPersona(dto.getIdTipoPersona());

        p.setTipoDocumento(dto.getTipoDocumento());
        p.setDocumento(dto.getDocumento());
        p.setNombre(dto.getNombre());
        p.setApellido(dto.getApellido());
        p.setCorreo(dto.getCorreo());
        p.setClave(claveFinal);
        p.setTipoPersona(tipoPersona);

        if (dto.getSedes() != null) {
            p.setSedes(resolverSedes(dto.getSedes()));
        }
        if (dto.getEstado() != null) {
            p.setEstado(dto.getEstado());
        }
        p.setFotoUrl(dto.getFotoUrl() != null ? dto.getFotoUrl() : "");

        Persona actualizada = repository.save(p);
        PersonaDto resultado = toDto(repository.findByCorreoWithTipo(actualizada.getCorreo()).orElse(actualizada));

        // Publicar evento de Actualización hacia RabbitMQ
        publicarEvento(resultado, RabbitMQConfig.RoutingKeys.USER_UPDATED, "UPDATED");

        return resultado;
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Persona p = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado"));
        p.setEstado(false);
        Persona actualizada = repository.save(p);

        PersonaDto resultado = toDto(actualizada);
        // Publicar evento de Eliminación lógica/cambio de estado hacia RabbitMQ
        publicarEvento(resultado, RabbitMQConfig.RoutingKeys.USER_DELETED, "DELETED");
    }

    // --- Métodos Privados Auxiliares para Limpieza y Principio SOLID (SRP) ---

    private void validarReglasNegocioCreacion(PersonaDto dto) {
        if (dto.getClave() == null || dto.getClave().isBlank()) {
            throw new BusinessException("Error: La clave es obligatoria.");
        }
        if (dto.getCorreo() == null || dto.getCorreo().isBlank()) {
            throw new BusinessException("Error: El correo es obligatorio.");
        }
        if (repository.existsByDocumento(dto.getDocumento())) {
            throw new BusinessException("Error: El documento ya existe.");
        }
        if (repository.existsByCorreo(dto.getCorreo())) {
            throw new BusinessException("Error: El correo ya existe.");
        }
    }

    private void validarReglasNegocioActualizacion(PersonaDto dto, Integer id) {
        if (dto.getCorreo() == null || dto.getCorreo().isBlank()) {
            throw new BusinessException("Error: El correo es obligatorio.");
        }
        if (repository.existsByDocumentoAndIdPersonaNot(dto.getDocumento(), id)) {
            throw new BusinessException("Error: El documento ya está registrado por otra persona.");
        }
        if (repository.existsByCorreoAndIdPersonaNot(dto.getCorreo(), id)) {
            throw new BusinessException("Error: El correo ya está registrado por otra persona.");
        }
    }

    private void publicarEvento(PersonaDto personaDto, String routingKey, String eventType) {
        TipoPersona tipo = tipoPersonaRepository.findById(personaDto.getIdTipoPersona()).orElse(null);
        String descripcionTipo = tipo != null ? tipo.getDescripcion() : "";

        UserEventDto eventDto = UserEventDto.builder()
                .idPersona(personaDto.getIdPersona())
                .tipoDocumento(personaDto.getTipoDocumento())
                .documento(personaDto.getDocumento())
                .nombre(personaDto.getNombre())
                .apellido(personaDto.getApellido())
                .correo(personaDto.getCorreo())
                .fotoUrl(personaDto.getFotoUrl())
                .idTipoPersona(personaDto.getIdTipoPersona())
                .descripcionTipoPersona(descripcionTipo)
                .estado(personaDto.getEstado())
                .eventType(eventType)
                .timestamp(LocalDateTime.now())
                .build();

        eventProducer.sendUserEvent(eventDto, routingKey);
    }

    private TipoPersona buscarTipoPersona(Integer idTipoPersona) {
        return tipoPersonaRepository.findById(idTipoPersona)
                .orElseThrow(() -> new BusinessException("Error: El tipo de persona no existe."));
    }

    private Set<Sede> resolverSedes(Set<SedeDto> sedesDto) {
        if (sedesDto == null || sedesDto.isEmpty()) {
            return new HashSet<>();
        }

        if (sedesDto.stream().anyMatch(s -> s == null || s.getIdSede() == null)) {
            throw new BusinessException("Error: Cada sede debe indicar su idSede.");
        }

        Set<Integer> idsSolicitados = sedesDto.stream()
                .map(SedeDto::getIdSede)
                .collect(Collectors.toSet());

        List<Sede> encontradas = sedeRepository.findAllById(idsSolicitados);

        if (encontradas.size() != idsSolicitados.size()) {
            Set<Integer> idsEncontrados = encontradas.stream()
                    .map(Sede::getIdSede)
                    .collect(Collectors.toSet());
            Set<Integer> faltantes = idsSolicitados.stream()
                    .filter(i -> !idsEncontrados.contains(i))
                    .collect(Collectors.toSet());
            throw new BusinessException("Error: Las siguientes sedes no existen: " + faltantes);
        }

        Set<Integer> inactivas = encontradas.stream()
                .filter(s -> !Boolean.TRUE.equals(s.getEstado()))
                .map(Sede::getIdSede)
                .collect(Collectors.toSet());
        if (!inactivas.isEmpty()) {
            throw new BusinessException("Error: Las siguientes sedes están inactivas: " + inactivas);
        }

        return new HashSet<>(encontradas);
    }

    private PersonaDto toDto(Persona e) {
        Set<SedeDto> sedesDto = e.getSedes() != null ? e.getSedes().stream().map(s ->
                SedeDto.builder()
                        .idSede(s.getIdSede())
                        .codigo(s.getCodigo())
                        .nombre(s.getNombre())
                        .estado(s.getEstado())
                        .fechaCreacion(s.getFechaCreacion())
                        .build()
        ).collect(Collectors.toSet()) : new HashSet<>();

        return PersonaDto.builder()
                .idPersona(e.getIdPersona())
                .tipoDocumento(e.getTipoDocumento())
                .documento(e.getDocumento())
                .nombre(e.getNombre())
                .apellido(e.getApellido())
                .correo(e.getCorreo())
                .idTipoPersona(e.getTipoPersona() != null ? e.getTipoPersona().getIdTipoPersona() : null)
                .sedes(sedesDto)
                .estado(e.getEstado())
                .fotoUrl(e.getFotoUrl())
                .fechaCreacion(e.getFechaCreacion())
                .build();
    }
}