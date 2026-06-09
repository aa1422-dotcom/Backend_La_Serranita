package com.laserranita.backend.services;

import com.laserranita.backend.models.Proveedor;
import com.laserranita.backend.repositories.ProveedorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class

ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    public List<Proveedor> obtenerTodos() {
        return proveedorRepository.findAll();
    }

    public Proveedor guardar(Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }

    public void eliminar(Integer id) {
        proveedorRepository.deleteById(id);
    }

    public Proveedor actualizar(Integer id, Proveedor proveedorActualizado) {
        Proveedor existente = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));

        existente.setRazonSocial(proveedorActualizado.getRazonSocial());
        existente.setRuc(proveedorActualizado.getRuc());
        existente.setTelefono(proveedorActualizado.getTelefono());
        existente.setDireccion(proveedorActualizado.getDireccion());
        existente.setCorreo(proveedorActualizado.getCorreo());

        return proveedorRepository.save(existente);
    }
}
