package com.laserranita.backend.services;

import com.laserranita.backend.models.Producto;
import com.laserranita.backend.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class ProductoService {

    @Value("${upload.path}")
    private String uploadPath;

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    // POST
    public Producto guardar(Producto producto) {
        String finalImageUrl = processImage(producto.getImageUrl());
        
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre(producto.getNombre());
        nuevoProducto.setSku(producto.getSku());
        nuevoProducto.setCategoria(producto.getCategoria());
        nuevoProducto.setPrecioVentaActual(producto.getPrecioVentaActual());
        nuevoProducto.setStockActual(producto.getStockActual());
        nuevoProducto.setStockMinimo(producto.getStockMinimo());
        nuevoProducto.setProveedor(producto.getProveedor());
        nuevoProducto.setFechaVencimiento(producto.getFechaVencimiento());
        nuevoProducto.setImageUrl(finalImageUrl);
        return productoRepository.save(nuevoProducto);
    }

    //Update (Actualizar)
    public Producto actualizar(Integer id, Producto productoActualizado) {
        String finalImageUrl = processImage(productoActualizado.getImageUrl());

        return productoRepository.findById(id).map(productoExistente -> {
            productoExistente.setNombre(productoActualizado.getNombre());
            productoExistente.setSku(productoActualizado.getSku());
            productoExistente.setCategoria(productoActualizado.getCategoria());
            productoExistente.setPrecioVentaActual(productoActualizado.getPrecioVentaActual());
            productoExistente.setStockActual(productoActualizado.getStockActual());
            productoExistente.setStockMinimo(productoActualizado.getStockMinimo());
            productoExistente.setProveedor(productoActualizado.getProveedor());
            productoExistente.setFechaVencimiento(productoActualizado.getFechaVencimiento());
            
            // Solo actualizamos la imagen si se envió una nueva (en formato base64)
            if (finalImageUrl != null && finalImageUrl.startsWith("/api/v1/uploads/")) {
                productoExistente.setImageUrl(finalImageUrl);
            }

            return productoRepository.save(productoExistente);
        }).orElseThrow(() -> new RuntimeException("Error: Producto no encontrado con el ID " + id));
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