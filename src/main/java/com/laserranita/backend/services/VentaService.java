package com.laserranita.backend.services;

import com.laserranita.backend.dto.VentaDTO;
import com.laserranita.backend.models.*;
import com.laserranita.backend.repositories.ProductoRepository;
import com.laserranita.backend.repositories.UsuarioRepository;
import com.laserranita.backend.repositories.VentaDetalleRepository;
import com.laserranita.backend.repositories.VentaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository detalleRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioService movimientoService;
    private final UsuarioRepository usuarioRepository;

    @Value("${upload.path}")
    private String uploadPath;

    public VentaService(VentaRepository ventaRepository,
                        VentaDetalleRepository detalleRepository,
                        ProductoRepository productoRepository,
                        MovimientoInventarioService movimientoService,
                        UsuarioRepository usuarioRepository) {
        this.ventaRepository = ventaRepository;
        this.detalleRepository = detalleRepository;
        this.productoRepository = productoRepository;
        this.movimientoService = movimientoService;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Venta registrarVentaCompleta(VentaDTO ventaDTO) {
        Venta venta = new Venta();
        venta.setFechaVenta(LocalDateTime.now());
        
        // Determinar estado de la venta
        String estado = ventaDTO.getEstado() != null ? ventaDTO.getEstado() : "PAGADO";
        venta.setEstado(estado);
        venta.setTipoComprobante("BOLETA");
        venta.setTotal(BigDecimal.ZERO);
        venta.setTotalVenta(BigDecimal.ZERO);
        venta.setNombreCliente(ventaDTO.getNombreCliente());

        // Procesar imagen del comprobante si viene en base64
        if (ventaDTO.getComprobanteUrl() != null) {
            String finalComprobanteUrl = processImage(ventaDTO.getComprobanteUrl());
            if (finalComprobanteUrl != null) {
                venta.setComprobanteUrl(finalComprobanteUrl);
            }
        }

        // Obtener el usuario autenticado actualmente y asociarlo a la venta
        try {
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                String currentUsername = auth.getName();
                usuarioRepository.findByUsername(currentUsername).ifPresent(venta::setUsuario);
            }
        } catch (Exception e) {
            System.err.println("WARNING: No se pudo capturar el usuario responsable de la venta: " + e.getMessage());
        }
        
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

            // SOLO REGISTRAR MOVIMIENTO DE INVENTARIO (SALIDA) SI LA VENTA PROCEDE DE INMEDIATO (NO PREPROGRAMADO)
            if (!"PREPROGRAMADO".equals(estado)) {
                MovimientoInventario mov = new MovimientoInventario();
                mov.setTipoMovimiento("SALIDA");
                mov.setCantidad(detalle.getCantidad());
                mov.setObservacion("Venta #" + ventaGuardada.getIdVenta());
                mov.setProducto(producto);
                mov.setFechaMovimiento(LocalDateTime.now());
                if (ventaGuardada.getUsuario() != null) {
                    mov.setUsuario(ventaGuardada.getUsuario());
                }
                movimientoService.registrarMovimiento(mov);
            }
        }

        ventaGuardada.setTotal(totalAcumulado);
        ventaGuardada.setTotalVenta(totalAcumulado);
        return ventaRepository.save(ventaGuardada);
    }

    private String processImage(String base64Image) {
        if (base64Image == null || !base64Image.contains(";base64,")) {
            return base64Image; // No es una nueva imagen en base64
        }

        try {
            // 1. Extraer extensión y datos
            String part1 = base64Image.split(";base64,")[0]; // data:image/png
            String extension = part1.split("/")[1]; // png
            String base64Data = base64Image.split(";base64,")[1];

            // 2. Generar nombre único
            String fileName = UUID.randomUUID().toString() + "." + extension;
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // 3. Asegurar que el directorio existe
            Path directoryPath = Paths.get(uploadPath);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // 4. Guardar archivo
            Path filePath = directoryPath.resolve(fileName);
            Files.write(filePath, imageBytes);

            // 5. Retornar la URL relativa para guardar en BD
            return "/api/v1/uploads/" + fileName;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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

    @Transactional
    public Venta aprobarVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID " + id));
        
        if (!"PREPROGRAMADO".equals(venta.getEstado())) {
            throw new RuntimeException("Solo se pueden aprobar ventas en estado PREPROGRAMADO");
        }

        venta.setEstado("PAGADO");

        // Registrar los movimientos de salida y reducir el stock de los productos
        java.util.List<VentaDetalle> detalles = detalleRepository.findByVenta_IdVenta(id);
        for (VentaDetalle detalle : detalles) {
            MovimientoInventario mov = new MovimientoInventario();
            mov.setTipoMovimiento("SALIDA");
            mov.setCantidad(detalle.getCantidad());
            mov.setObservacion("Aprobación Venta #" + venta.getIdVenta());
            mov.setProducto(detalle.getProducto());
            mov.setFechaMovimiento(LocalDateTime.now());
            if (venta.getUsuario() != null) {
                mov.setUsuario(venta.getUsuario());
            }
            movimientoService.registrarMovimiento(mov);
        }

        return ventaRepository.save(venta);
    }

    @Transactional
    public Venta rechazarVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID " + id));
        
        if (!"PREPROGRAMADO".equals(venta.getEstado())) {
            throw new RuntimeException("Solo se pueden rechazar ventas en estado PREPROGRAMADO");
        }

        venta.setEstado("RECHAZADO");
        return ventaRepository.save(venta);
    }
}
