package com.auth.seguridad.security;

import com.auth.seguridad.entity.Persona;
import com.auth.seguridad.repository.PersonaRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PersonaRepository personaRepository;

    public CustomUserDetailsService(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Persona persona = personaRepository.findByCorreoWithTipo(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + username));

        String rolFormateado =
                "ROLE_" +
                        persona.getTipoPersona()
                                .getDescripcion()
                                .trim()
                                .replace(" ", "_")
                                .toUpperCase();

        return new User(
                persona.getCorreo(),
                persona.getClave(),
                Collections.singletonList(new SimpleGrantedAuthority(rolFormateado))
        );
    }
}