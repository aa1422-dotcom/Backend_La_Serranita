package com.laserranita.backend.services;

import com.laserranita.backend.models.MovimientoInventario;
import com.laserranita.backend.models.Producto;
import com.laserranita.backend.models.Usuario;
import com.laserranita.backend.repositories.MovimientoInventarioRepository;
import com.laserranita.backend.repositories.ProductoRepository;
import com.laserranita.backend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; //Importación totalmenteultravital

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificationService notificationService;

    public MovimientoInventarioService(MovimientoInventarioRepository movimientoRepository,
                                       ProductoRepository productoRepository,
                                       UsuarioRepository usuarioRepository,
                                       NotificationService notificationService) {
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificationService = notificationService;
    }

    public List<MovimientoInventario> obtenerTodos() {
        return movimientoRepository.findAll();
    }

    // Lógica de negocio con fe la entiende lo detallare como pa niño de primaria xd
    @Transactional
    public MovimientoInventario registrarMovimiento(MovimientoInventario movimiento) {

        // 1. Verificamos que el producto exista
        Producto producto = productoRepository.findById(movimiento.getProducto().getIdProducto())
                .orElseThrow(() -> new RuntimeException("Error: Producto no encontrado"));

        // El usuario es opcional para movimientos de ajuste si no viene en el JSON
        Usuario usuario = null;
        if (movimiento.getUsuario() != null && movimiento.getUsuario().getIdUsuario() != null) {
            usuario = usuarioRepository.findById(movimiento.getUsuario().getIdUsuario()).orElse(null);
        }

        // 2. Calculamos el nuevo stock dependiendo del tipo de movimiento
        // Manejamos el caso de que el stockActual sea NULL en la BD
        int stockActual = (producto.getStockActual() != null) ? producto.getStockActual() : 0;
        
        String tipo = movimiento.getTipoMovimiento().toUpperCase();
        if (tipo.contains("ENTRADA")) {
            producto.setStockActual(stockActual + movimiento.getCantidad());
        } else {
            // MERMA, AJUSTE (negativo), SALIDA_VENTA
            if (stockActual < movimiento.getCantidad()) {
                throw new RuntimeException("Error: Stock insuficiente. Tienes " + stockActual + " y quieres sacar " + movimiento.getCantidad());
            }
            producto.setStockActual(stockActual - movimiento.getCantidad());
        }

        // 3. Llenamos los datos automáticos
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setProducto(producto);
        movimiento.setUsuario(usuario);

        // 4. Guardamos el producto (con su nuevo stock) y guardamos el movimiento
        productoRepository.save(producto);

        // 5. Verificar alerta de stock bajo
        verificarStockBajo(producto);

        return movimientoRepository.save(movimiento);
    }

    private void verificarStockBajo(Producto producto) {
        int stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;
        int stockMinimo = producto.getStockMinimo() != null ? producto.getStockMinimo() : 0;

        if (stockActual <= stockMinimo || stockActual == 0) {
            Boolean alerta = producto.getAlertaEnviada();
            if (alerta == null || Boolean.FALSE.equals(alerta)) {
                notificationService.sendStockAlert(producto);
                producto.setAlertaEnviada(true);
                productoRepository.save(producto);
            }
        } else {
            if (Boolean.TRUE.equals(producto.getAlertaEnviada())) {
                producto.setAlertaEnviada(false);
                productoRepository.save(producto);
            }
        }
    }

    @Transactional
    public MovimientoInventario actualizarMovimiento(Integer id, MovimientoInventario data) {
        MovimientoInventario mov = movimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
        
        if (data.getObservacion() != null) {
            mov.setObservacion(data.getObservacion());
        }
        
        return movimientoRepository.save(mov);
    }
}