package com.laserranita.backend.services;

import com.laserranita.backend.models.Pedido;
import com.laserranita.backend.repositories.PedidoRepository;
import com.laserranita.backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${upload.path}")
    private String uploadPath;

    public PedidoService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Pedido> obtenerTodos() {
        return pedidoRepository.findAll();
    }

    @Transactional
    public Pedido registrarPedido(Pedido pedido) {
        pedido.setFechaPedido(LocalDateTime.now());
        if (pedido.getEstado() == null) {
            pedido.setEstado("ORDEN_REALIZADA");
        }

        // Procesar imagen del comprobante si viene en base64
        if (pedido.getComprobanteUrl() != null) {
            String finalComprobanteUrl = processImage(pedido.getComprobanteUrl());
            if (finalComprobanteUrl != null) {
                pedido.setComprobanteUrl(finalComprobanteUrl);
            }
        }

        // Obtener el usuario autenticado actualmente y asociarlo al pedido
        try {
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                String currentUsername = auth.getName();
                usuarioRepository.findByUsername(currentUsername).ifPresent(pedido::setUsuario);
            }
        } catch (Exception e) {
            System.err.println("WARNING: No se pudo capturar el usuario responsable del pedido: " + e.getMessage());
        }

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido actualizarEstado(Integer id, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
        
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    private String processImage(String base64Image) {
        if (base64Image == null || !base64Image.contains(";base64,")) {
            return base64Image;
        }

        try {
            String part1 = base64Image.split(";base64,")[0];
            String extension = part1.split("/")[1];
            String base64Data = base64Image.split(";base64,")[1];

            String fileName = UUID.randomUUID().toString() + "." + extension;
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            Path directoryPath = Paths.get(uploadPath);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            Path filePath = directoryPath.resolve(fileName);
            Files.write(filePath, imageBytes);

            return "/api/v1/uploads/" + fileName;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
