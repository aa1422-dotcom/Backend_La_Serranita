package com.laserranita.backend.controllers;

import com.laserranita.backend.models.MovimientoInventario;
import com.laserranita.backend.models.Producto;
import com.laserranita.backend.models.Usuario;
import com.laserranita.backend.services.MovimientoInventarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventario")
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoService;

    public MovimientoInventarioController(MovimientoInventarioService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @GetMapping("/movimientos")
    public List<MovimientoInventario> listarMovimientos() {
        return movimientoService.obtenerTodos();
    }

    @PostMapping("/ajuste")
    public ResponseEntity<?> crearMovimiento(@RequestBody java.util.Map<String, Object> data) {
        try {
            System.out.println("DEBUG: Recibiendo ajuste: " + data);
            
            MovimientoInventario mov = new MovimientoInventario();
            
            // 1. Tipo de Movimiento
            mov.setTipoMovimiento(data.getOrDefault("tipoMovimiento", "AJUSTE").toString());

            // 2. Cantidad (Manejo ultra-robusto de números)
            Object cantObj = data.get("cantidad");
            if (cantObj == null) throw new RuntimeException("La cantidad es obligatoria");
            mov.setCantidad(Integer.parseInt(cantObj.toString()));

            // 3. Observación
            mov.setObservacion(data.getOrDefault("observacion", "").toString());

            // 4. Producto
            Object idProdObj = data.get("idProducto");
            if (idProdObj == null) throw new RuntimeException("El ID del producto es obligatorio");
            
            Producto p = new Producto();
            p.setIdProducto(Integer.parseInt(idProdObj.toString()));
            mov.setProducto(p);

            // 5. Usuario (Opcional, pero intentamos capturarlo si viene)
            if (data.containsKey("idUsuario")) {
                Usuario u = new Usuario();
                u.setIdUsuario(Integer.parseInt(data.get("idUsuario").toString()));
                mov.setUsuario(u);
            }

            return ResponseEntity.ok(movimientoService.registrarMovimiento(mov));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error en inventario: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarMovimiento(@PathVariable Integer id, @RequestBody MovimientoInventario mov) {
        try {
            return ResponseEntity.ok(movimientoService.actualizarMovimiento(id, mov));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al actualizar: " + e.getMessage());
        }
    }
}
