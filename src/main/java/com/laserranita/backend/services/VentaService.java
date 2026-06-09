package com.laserranita.backend.services;

import com.laserranita.backend.dto.VentaDTO;
import com.laserranita.backend.models.*;
import com.laserranita.backend.repositories.ProductoRepository;
import com.laserranita.backend.repositories.VentaDetalleRepository;
import com.laserranita.backend.repositories.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class VentaService {

    //Esto si no se como lograran entenederlo yo solo lo comento y hasta ahi llego no me pidan explicacion  xdxddxxdxd

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository detalleRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioService movimientoService;

    public VentaService(VentaRepository ventaRepository,
                        VentaDetalleRepository detalleRepository,
                        ProductoRepository productoRepository,
                        MovimientoInventarioService movimientoService) {
        this.ventaRepository = ventaRepository;
        this.detalleRepository = detalleRepository;
        this.productoRepository = productoRepository;
        this.movimientoService = movimientoService;
    }

    @Transactional
    public Venta registrarVentaCompleta(VentaDTO ventaDTO) {
        Venta venta = new Venta();
        venta.setFechaVenta(LocalDateTime.now());
        venta.setEstado("PAGADO");
        venta.setTipoComprobante("BOLETA");
        venta.setTotal(BigDecimal.ZERO);
        venta.setTotalVenta(BigDecimal.ZERO);
        venta.setNombreCliente(ventaDTO.getNombreCliente());

        // Si tuviéramos un campo nombre_cliente en el modelo Venta, lo asignaríamos.
        // Como no existe (según bd.sql), este dato se pierde por ahora o se asocia a un Cliente anónimo.
        
        Venta ventaGuardada = ventaRepository.save(venta);
        BigDecimal totalAcumulado = BigDecimal.ZERO;

        for (VentaDTO.DetalleVentaDTO detDTO : ventaDTO.getDetalle()) {
            Producto producto = productoRepository.findById(detDTO.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detDTO.getIdProducto()));

            VentaDetalle detalle = new VentaDetalle();
            detalle.setVenta(ventaGuardada);
            detalle.setProducto(producto);
            detalle.setCantidad(detDTO.getCantidad());
            detalle.setPrecio_unitario_historico(detDTO.getPrecioUnitario() != null ? detDTO.getPrecioUnitario() : producto.getPrecioVentaActual());
            
            BigDecimal subtotal = detalle.getPrecio_unitario_historico().multiply(new BigDecimal(detalle.getCantidad()));
            detalle.setSubtotal(subtotal);
            totalAcumulado = totalAcumulado.add(subtotal);

            detalleRepository.save(detalle);

            // Registrar movimiento de inventario (SALIDA)
            MovimientoInventario mov = new MovimientoInventario();
            mov.setTipoMovimiento("SALIDA");
            mov.setCantidad(detalle.getCantidad());
            mov.setObservacion("Venta #" + ventaGuardada.getIdVenta());
            mov.setProducto(producto);
            mov.setFechaMovimiento(LocalDateTime.now());
            
            movimientoService.registrarMovimiento(mov);
        }

        ventaGuardada.setTotal(totalAcumulado);
        ventaGuardada.setTotalVenta(totalAcumulado);
        return ventaRepository.save(ventaGuardada);
    }

    // Añade este metodo para leer todas las ventas
    public java.util.List<Venta> obtenerTodas() {
        return ventaRepository.findAll();
    }

    public java.util.List<VentaDetalle> obtenerDetallesPorVenta(Integer idVenta) {
        return detalleRepository.findByVenta_IdVenta(idVenta);
    }

    @Transactional
    public Venta actualizarVenta(Integer id, Venta ventaData) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        
        if (ventaData.getNombreCliente() != null) {
            venta.setNombreCliente(ventaData.getNombreCliente());
        }
        
        return ventaRepository.save(venta);
    }
}
