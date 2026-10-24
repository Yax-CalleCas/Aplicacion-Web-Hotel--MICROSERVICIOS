package com.auth.seguridad.config;

import com.auth.seguridad.entity.Persona;
import com.auth.seguridad.entity.Sede;
import com.auth.seguridad.entity.TipoPersona;
import com.auth.seguridad.repository.PersonaRepository;
import com.auth.seguridad.repository.SedeRepository;
import com.auth.seguridad.repository.TipoPersonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TipoPersonaRepository tipoPersonaRepository;
    private final PersonaRepository personaRepository;
    private final SedeRepository sedeRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("--- INICIANDO CARGA DE DATOS ---");

        // 1. Cargar tipos de persona de forma segura
        crearTipoPersonaSiNoExiste(1, "Administrador");
        crearTipoPersonaSiNoExiste(2, "Empleado");
        crearTipoPersonaSiNoExiste(3, "Cliente");

        // 2. Cargar las 3 sedes iniciales requeridas
        crearSedeSiNoExiste("LIM-MIRA", "Lima Miraflores");
        crearSedeSiNoExiste("AQP-CENTRO", "Arequipa");
        crearSedeSiNoExiste("CHX-CENTRO", "Chiclayo");

        // 3. Cargar usuarios con documentos de 8 dígitos (válidos para @Size min=8)
        crearUsuarioSiNoExiste(
                "45454530",
                "Yaxon",
                "Castillo",
                "yaxcallecastillo@gmail.com",
                "123123",
                1,
                "DNI",
                "https://acortar.link/NlkrTo"
        );

        crearUsuarioSiNoExiste(
                "43534340",
                "Paul",
                "Calle",
                "Trabajadorpaul@gmail.com",
                "456456",
                2,
                "DNI",
                "https://acortar.link/RdFh25"
        );

        System.out.println("--- PROCESO DE CARGA FINALIZADO ---");
    }

    private void crearTipoPersonaSiNoExiste(Integer id, String desc) {
        if (tipoPersonaRepository.findById(id).isEmpty()) {
            tipoPersonaRepository.save(
                    TipoPersona.builder()
                            .idTipoPersona(id)
                            .descripcion(desc)
                            .estado(true)
                            .fechaCreacion(LocalDateTime.now())
                            .build()
            );
            System.out.println("Tipo creado: " + desc);
        }
    }

    private void crearSedeSiNoExiste(String codigo, String nombre) {
        if (sedeRepository.findByCodigo(codigo).isEmpty()) {
            sedeRepository.save(
                    Sede.builder()
                            .codigo(codigo)
                            .nombre(nombre)
                            .estado(true)
                            .fechaCreacion(LocalDateTime.now())
                            .build()
            );
            System.out.println("Sede creada: " + nombre);
        } else {
            System.out.println("Sede ya existente, saltando: " + nombre);
        }
    }

    private void crearUsuarioSiNoExiste(
            String doc,
            String nom,
            String ape,
            String correo,
            String clave,
            Integer idTipo,
            String tipoDocumento,
            String fotoUrl) {

        if (personaRepository.findByCorreo(correo).isEmpty()) {

            TipoPersona tipo = tipoPersonaRepository.findById(idTipo)
                    .orElseThrow(() ->
                            new RuntimeException("Tipo de persona no encontrado: " + idTipo));

            personaRepository.save(
                    Persona.builder()
                            .tipoDocumento(tipoDocumento)
                            .documento(doc)
                            .nombre(nom)
                            .apellido(ape)
                            .correo(correo)
                            .clave(passwordEncoder.encode(clave))
                            .fotoUrl(fotoUrl)
                            .tipoPersona(tipo)
                            .estado(true)
                            .fechaCreacion(LocalDateTime.now())
                            .build()
            );

            System.out.println("Usuario creado: " + correo);

        } else {
            System.out.println("Usuario ya existente, saltando: " + correo);
        }
    }
}