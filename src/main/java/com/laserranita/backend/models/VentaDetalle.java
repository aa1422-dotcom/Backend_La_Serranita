package com.laserranita.backend.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "venta_detalle")
public class VentaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle") // <-- Verifica en tu BD
    private Integer idDetalle;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario_historico", precision = 10, scale = 2)
    private BigDecimal precio_unitario_historico;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    // RELACIÓN: Muchos detalles pertenecen a UNA Venta
    @ManyToOne
    @JoinColumn(name = "id_venta") // <-- Verifica en tu BD
    private Venta venta;

    // RELACIÓN: Muchos detalles apuntan a UN Producto
    @ManyToOne
    @JoinColumn(name = "id_producto") // <-- Verifica en tu BD
    private Producto producto;

    public Integer getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }


    public BigDecimal getPrecio_unitario_historico() {
        return precio_unitario_historico;
    }

    public void setPrecio_unitario_historico(BigDecimal precio_unitario_historico) {
        this.precio_unitario_historico = precio_unitario_historico;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }
}
