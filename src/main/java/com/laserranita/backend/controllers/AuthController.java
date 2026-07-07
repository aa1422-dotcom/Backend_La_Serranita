package com.laserranita.backend.controllers;

import com.laserranita.backend.config.AdaptivePasswordEncoder;
import com.laserranita.backend.config.JwtUtils;
import com.laserranita.backend.models.Usuario;
import com.laserranita.backend.repositories.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtils jwtUtils;
    private final AdaptivePasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          UsuarioRepository usuarioRepository,
                          JwtUtils jwtUtils,
                          AdaptivePasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String inputIdentifier = credentials.get("username") != null ? credentials.get("username").trim() : "";
        String inputPassword = credentials.get("password") != null ? credentials.get("password").trim() : "";

        System.out.println("DEBUG: Intento de login para: [" + inputIdentifier + "]");

        try {
            // Autenticamos usando el AuthenticationManager de Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(inputIdentifier, inputPassword)
            );

            // Obtenemos el nombre de usuario autenticado
            String username = authentication.getName();
            Usuario u = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado después de la autenticación"));

            // MIGRACIÓN ACTIVA DE CONTRASEÑA: si detectamos texto plano, lo encriptamos en BCrypt
            if (!passwordEncoder.isBCrypt(u.getPassword())) {
                u.setPassword(passwordEncoder.encode(inputPassword));
                usuarioRepository.save(u);
                System.out.println("DEBUG: Contraseña de '" + username + "' migrada a BCrypt de manera transparente.");
            }

            // Obtenemos el rol para el token y la respuesta
            String roleName = u.getRol() != null ? u.getRol().getNombreRol() : "USER";
            
            // Generamos un token JWT real firmado
            String token = jwtUtils.generateToken(username, roleName);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("usuario", u);
            response.put("rol", roleName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("DEBUG: Falló la autenticación. Razón: " + e.getMessage());
            String debugMsg = String.format(
                "Error: Credenciales no coinciden. Recibido: [User: '%s']. Detalle: %s",
                inputIdentifier, e.getMessage()
            );
            return ResponseEntity.status(401).body(debugMsg);
        }
    }
}