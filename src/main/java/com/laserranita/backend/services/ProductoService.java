package com.laserranita.backend.services;

import com.laserranita.backend.models.Producto;
import com.laserranita.backend.repositories.ProductoRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProductoService {

    //GET

    private final ProductoRepository productoRepository;


    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    // POST
    public Producto guardar(Producto producto) {
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre(producto.getNombre());
        nuevoProducto.setSku(producto.getSku());
        nuevoProducto.setCategoria(producto.getCategoria());
        nuevoProducto.setPrecioVentaActual(producto.getPrecioVentaActual());
        nuevoProducto.setStockActual(producto.getStockActual());
        nuevoProducto.setStockMinimo(producto.getStockMinimo());
        nuevoProducto.setProveedor(producto.getProveedor());
        nuevoProducto.setFechaVencimiento(producto.getFechaVencimiento());
        nuevoProducto.setImageUrl(producto.getImageUrl());
        return productoRepository.save(nuevoProducto);
    }

    //Update (Actualizar)
    public Producto actualizar(Integer id, Producto productoActualizado) {
        // 1. Buscamos el producto en la base de datos por su ID
        return productoRepository.findById(id).map(productoExistente -> {
            // 2. Si existe, actualizamos sus campos con los nuevos datos
            productoExistente.setNombre(productoActualizado.getNombre());
            productoExistente.setSku(productoActualizado.getSku());
            productoExistente.setCategoria(productoActualizado.getCategoria());
            productoExistente.setPrecioVentaActual(productoActualizado.getPrecioVentaActual());
            productoExistente.setStockActual(productoActualizado.getStockActual());
            productoExistente.setStockMinimo(productoActualizado.getStockMinimo());
            productoExistente.setProveedor(productoActualizado.getProveedor());
            productoExistente.setFechaVencimiento(productoActualizado.getFechaVencimiento());
            productoExistente.setImageUrl(productoActualizado.getImageUrl());

            // 3. Guardamos los cambios (Spring Data JPA es inteligente y hace un UPDATE en vez de un INSERT porque el objeto ya tiene ID)
            return productoRepository.save(productoExistente);
        }).orElseThrow(() -> new RuntimeException("Error: Producto no encontrado con el ID " + id));
    }

    public List<Producto> obtenerLowStock() {
        return productoRepository.findLowStock();
    }

    public List<Producto> obtenerExpiringSoon() {
        LocalDate limit = LocalDate.now().plusMonths(2);
        return productoRepository.findExpiringSoon(limit);
    }

    //Borrar
    public void eliminar(Integer id) {
        productoRepository.deleteById(id);
    }
}