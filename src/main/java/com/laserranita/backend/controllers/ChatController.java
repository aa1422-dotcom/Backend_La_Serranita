package com.laserranita.backend.controllers;

import com.laserranita.backend.services.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<?> consultarChat(@RequestBody Map<String, String> request) {
        String mensaje = request.get("message");
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El mensaje no puede estar vacío.");
        }

        try {
            String respuestaIA = chatService.procesarConsultaChat(mensaje);
            Map<String, String> response = new HashMap<>();
            response.put("reply", respuestaIA);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno en el chat de IA: " + e.getMessage());
        }
    }
}
