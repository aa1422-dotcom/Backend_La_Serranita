package com.laserranita.backend.services;

import com.laserranita.backend.models.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendStockAlert(Producto producto) {
        if (producto.getProveedor() == null || producto.getProveedor().getCorreo() == null
                || producto.getProveedor().getCorreo().isBlank()) {
            log.warn("Producto '{}' no tiene proveedor con correo registrado. No se envió alerta.", producto.getNombre());
            return;
        }

        String destinatario = producto.getProveedor().getCorreo();
        String asunto = "Alerta de Stock Bajo - " + producto.getNombre();
        String cuerpo = String.format("""
                Estimado proveedor %s,

                Le informamos que el siguiente producto ha alcanzado niveles críticos de stock:

                Producto: %s
                SKU: %s
                Stock actual: %d
                Stock mínimo: %d

                Por favor, tome las acciones necesarias para reabastecer el producto.

                Saludos cordiales,
                La Serranita - Sistema de Inventarios
                """,
                producto.getProveedor().getRazonSocial(),
                producto.getNombre(),
                producto.getSku() != null ? producto.getSku() : "N/A",
                producto.getStockActual() != null ? producto.getStockActual() : 0,
                producto.getStockMinimo() != null ? producto.getStockMinimo() : 0
        );

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
            log.info("Alerta de stock enviada a {} para el producto '{}'", destinatario, producto.getNombre());
        } catch (Exception e) {
            log.error("Error al enviar alerta de stock a {} para el producto '{}': {}",
                    destinatario, producto.getNombre(), e.getMessage());
        }
    }
}
