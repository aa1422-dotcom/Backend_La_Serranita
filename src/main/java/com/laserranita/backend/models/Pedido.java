package com.laserranita.backend.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;

    @Column(name = "cliente", nullable = false, length = 150)
    private String cliente;

    @Column(name = "nombre_empresa", length = 150)
    @JsonProperty("nombreEmpresa")
    private String nombreEmpresa;

    @Column(name = "doc_identidad", length = 20)
    @JsonProperty("docIdentidad")
    private String docIdentidad;

    @Column(name = "producto", columnDefinition = "TEXT")
    private String producto;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "comprobante_url", columnDefinition = "TEXT")
    @JsonProperty("comprobanteUrl")
    private String comprobanteUrl;

    @Column(name = "estado", length = 30)
    private String estado; // "ORDEN_REALIZADA", "EN_CAMINO", "ENTREGADA"

    @Column(name = "fecha_pedido")
    @JsonProperty("fechaPedido")
    private LocalDateTime fechaPedido;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    // Getters and Setters
    public Integer getIdPedido() { return idPedido; }
    public void setIdPedido(Integer idPedido) { this.idPedido = idPedido; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public String getDocIdentidad() { return docIdentidad; }
    public void setDocIdentidad(String docIdentidad) { this.docIdentidad = docIdentidad; }

    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getComprobanteUrl() { return comprobanteUrl; }
    public void setComprobanteUrl(String comprobanteUrl) { this.comprobanteUrl = comprobanteUrl; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDateTime fechaPedido) { this.fechaPedido = fechaPedido; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
