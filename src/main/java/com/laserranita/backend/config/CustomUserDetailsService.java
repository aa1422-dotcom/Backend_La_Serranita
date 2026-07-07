package com.laserranita.backend.config;

import com.laserranita.backend.models.Usuario;
import com.laserranita.backend.repositories.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Permitimos buscar tanto por nombre de usuario como por correo electrónico
        Usuario usuario = usuarioRepository.findByUsernameOrCorreo(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con identificador: " + usernameOrEmail));

        if (usuario.getEstado() != null && !usuario.getEstado()) {
            throw new RuntimeException("El usuario está inactivo en el sistema");
        }

        String roleName = usuario.getRol() != null ? usuario.getRol().getNombreRol() : "USER";
        String authority = "ROLE_" + roleName.toUpperCase().trim();

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(authority))
        );
    }
}
