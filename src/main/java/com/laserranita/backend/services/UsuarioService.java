package com.laserranita.backend.services;


import com.laserranita.backend.models.Usuario;
import com.laserranita.backend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario guardar(Usuario usuario) {
        //aquí encriptaríamos
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Integer id, Usuario usuarioActualizado) {
        return usuarioRepository.findById(id).map(usuarioExistente -> {
            usuarioExistente.setUsername(usuarioActualizado.getUsername());
            usuarioExistente.setPassword(usuarioActualizado.getPassword());
            usuarioExistente.setEstado(usuarioActualizado.getEstado());
            usuarioExistente.setCorreo(usuarioActualizado.getCorreo());

            //actualizamos la relación del Rol
            usuarioExistente.setRol(usuarioActualizado.getRol());

            return usuarioRepository.save(usuarioExistente);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    public void eliminar(Integer id) {
        //desactivan.
        usuarioRepository.deleteById(id);
    }
}
