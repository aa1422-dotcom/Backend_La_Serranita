package com.laserranita.backend.dto;

import java.util.List;
import java.math.BigDecimal;

public class VentaDTO {
    private String nombreCliente;
    private List<DetalleVentaDTO> detalle;

    // Getters y Setters
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public List<DetalleVentaDTO> getDetalle() { return detalle; }
    public void setDetalle(List<DetalleVentaDTO> detalle) { this.detalle = detalle; }

    public static class DetalleVentaDTO {
        private Integer idProducto;
        private Integer cantidad;
        private BigDecimal precioUnitario;

        // Getters y Setters
        public Integer getIdProducto() { return idProducto; }
        public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    }
}
