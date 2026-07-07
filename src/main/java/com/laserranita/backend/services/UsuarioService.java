package com.laserranita.backend.services;

import com.laserranita.backend.config.AdaptivePasswordEncoder;
import com.laserranita.backend.models.Usuario;
import com.laserranita.backend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AdaptivePasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, AdaptivePasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario guardar(Usuario usuario) {
        // Encriptar la contraseña antes de guardar el nuevo usuario
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            if (!passwordEncoder.isBCrypt(usuario.getPassword())) {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Integer id, Usuario usuarioActualizado) {
        return usuarioRepository.findById(id).map(usuarioExistente -> {
            usuarioExistente.setUsername(usuarioActualizado.getUsername());
            
            // Solo actualizamos y encriptamos si se suministró una contraseña válida modificada
            if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isEmpty()) {
                if (!passwordEncoder.isBCrypt(usuarioActualizado.getPassword())) {
                    usuarioExistente.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
                } else {
                    usuarioExistente.setPassword(usuarioActualizado.getPassword());
                }
            }
            
            usuarioExistente.setEstado(usuarioActualizado.getEstado());
            usuarioExistente.setCorreo(usuarioActualizado.getCorreo());
            usuarioExistente.setRol(usuarioActualizado.getRol());

            return usuarioRepository.save(usuarioExistente);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    public void eliminar(Integer id) {
        usuarioRepository.deleteById(id);
    }
}
