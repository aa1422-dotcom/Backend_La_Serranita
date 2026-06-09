package com.laserranita.backend.controllers;

import com.laserranita.backend.models.Usuario;
import com.laserranita.backend.repositories.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody java.util.Map<String, String> credentials) {
        // Capturamos lo que sea que venga en el JSON
        String inputIdentifier = credentials.get("username") != null ? credentials.get("username").trim() : "";
        String inputPassword = credentials.get("password") != null ? credentials.get("password").trim() : "";

        System.out.println("DEBUG: Recibido identifier: [" + inputIdentifier + "]");
        System.out.println("DEBUG: Recibido password: [" + inputPassword + "]");

        java.util.List<Usuario> todosLosUsuarios = usuarioRepository.findAll();
        System.out.println("DEBUG: Usuarios en BD: " + todosLosUsuarios.size());
        
        for (Usuario u : todosLosUsuarios) {
            String dbUser = u.getUsername() != null ? u.getUsername().trim() : "";
            String dbEmail = u.getCorreo() != null ? u.getCorreo().trim() : "";
            String dbPass = u.getPassword() != null ? u.getPassword().trim() : "";

            boolean identifierMatches = inputIdentifier.equalsIgnoreCase(dbUser) || 
                                         inputIdentifier.equalsIgnoreCase(dbEmail);
            
            if (identifierMatches && inputPassword.equals(dbPass)) {
                java.util.Map<String, Object> response = new java.util.HashMap<>();
                response.put("token", "fake-jwt-token-for-development");
                response.put("usuario", u);
                response.put("rol", u.getRol() != null ? u.getRol().getNombreRol() : "USER");
                return ResponseEntity.ok(response);    
            }
        }

        String debugMsg = String.format(
            "Error: Credenciales no coinciden. Recibido: [User: '%s', Pass: '%s']. Usuarios en BD: %d",
            inputIdentifier, inputPassword, todosLosUsuarios.size()
        );
        return ResponseEntity.status(401).body(debugMsg);
    }
}