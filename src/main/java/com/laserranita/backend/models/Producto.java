package com.laserranita.backend.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "productos")

public class Producto {
    @Id  // Indica que esta es la Llave Primaria (Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Indica que es un SERIAL (Autoincrementable)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "sku", unique = true, length = 50)
    private String sku;

    @Column(name = "categoria", length = 50)
    private String categoria;

    // BigDecimal es el estándar empresarial para manejar dinero (nunca uses double o float)
    @Column(name = "precio_venta_actual", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVentaActual;

    @Column(name = "stock_actual")
    private Integer stockActual;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    // RELACIÓN: Muchos productos pertenecen a UN Proveedor
    @ManyToOne
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @JsonProperty("imageUrl")
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "alerta_enviada")
    private Boolean alertaEnviada = false;

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPrecioVentaActual() {
        return precioVentaActual;
    }

    public void setPrecioVentaActual(BigDecimal precioVentaActual) {
        this.precioVentaActual = precioVentaActual;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    public void setStockActual(Integer stockActual) {
        this.stockActual = stockActual;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getAlertaEnviada() {
        return alertaEnviada;
    }

    public void setAlertaEnviada(Boolean alertaEnviada) {
        this.alertaEnviada = alertaEnviada;
    }
}
