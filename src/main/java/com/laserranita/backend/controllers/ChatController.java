package com.laserranita.backend.controllers;

import com.laserranita.backend.services.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public Map<String, String> getChatResponse(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String response = chatService.getChatResponse(userMessage);
        return Map.of("response", response);
    }
}
