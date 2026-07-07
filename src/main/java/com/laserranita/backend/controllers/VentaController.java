package com.laserranita.backend.controllers;

import com.laserranita.backend.dto.VentaDTO;
import com.laserranita.backend.models.Venta;
import com.laserranita.backend.services.VentaService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    public ResponseEntity<?> procesarVenta(@RequestBody VentaDTO ventaDTO) {
        try {
            return ResponseEntity.ok(ventaService.registrarVentaCompleta(ventaDTO));
        } catch (Exception e) {
            e.printStackTrace(); // Para ver en la consola del backend
            return ResponseEntity.status(500).body("Error al procesar venta: " + e.getMessage());
        }
    }
    // Abrimos la puerta para que el navegador pueda leer
    @GetMapping
    public List<Venta> listarVentas() {
        return ventaService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerVenta(@PathVariable Integer id) {
        // Return details for simplicity to build the receipt
        return ResponseEntity.ok(ventaService.obtenerDetallesPorVenta(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarVenta(@PathVariable Integer id, @RequestBody Venta venta) {
        try {
            return ResponseEntity.ok(ventaService.actualizarVenta(id, venta));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al actualizar venta: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobarVenta(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(ventaService.aprobarVenta(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al aprobar venta: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazarVenta(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(ventaService.rechazarVenta(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al rechazar venta: " + e.getMessage());
        }
    }
}