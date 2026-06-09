package com.laserranita.backend.controllers;

import com.laserranita.backend.models.Producto;
import com.laserranita.backend.services.ProductoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    //GET
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> listarProductos() {
        return productoService.obtenerTodos();
    }

    //POST
    @PostMapping
    public Producto crearProducto(@RequestBody Producto producto) {
        // @RequestBody le dice a Spring que transforme el JSON que llega de internet en un objeto Java
        return productoService.guardar(producto);
    }

    // La "U" - Endpoint para Actualizar
    // Recibe el ID en la URL (ej. /api/v1/productos/1) y los datos nuevos en el Body
    @PutMapping("/{id}")
    public Producto actualizarProducto(@PathVariable Integer id, @RequestBody Producto producto) {
        return productoService.actualizar(id, producto);
    }

    // La "D" - Endpoint para Eliminar
    // Solo necesita el ID en la URL para saber a quién borrar
    @DeleteMapping("/{id}")
    public void eliminarProducto(@PathVariable Integer id) {
        productoService.eliminar(id);
    }

    @GetMapping("/low-stock")
    public List<Producto> listarProductosLowStock() {
        return productoService.obtenerLowStock();
    }

    @GetMapping("/expiring-soon")
    public List<Producto> listarProductosExpiringSoon() {
        return productoService.obtenerExpiringSoon();
    }

}