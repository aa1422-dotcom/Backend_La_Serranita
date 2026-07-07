package com.laserranita.backend.repositories;

import com.laserranita.backend.models.Rol;
import com.laserranita.backend.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByUsernameOrCorreo(String username, String correo);
}