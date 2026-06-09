package com.laserranita.backend.services;

import com.laserranita.backend.models.Cliente;
import com.laserranita.backend.repositories.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
    }

    public Cliente guardar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public Cliente actualizar(Integer id, Cliente clienteActualizado) {
        return clienteRepository.findById(id).map(clienteExistente -> {
            // Solo actualizamos las columnas que existen en la tabla
            clienteExistente.setNombres(clienteActualizado.getNombres());
            clienteExistente.setApellidos(clienteActualizado.getApellidos());
            clienteExistente.setDni(clienteActualizado.getDni());
            clienteExistente.setTelefono(clienteActualizado.getTelefono());
            clienteExistente.setCorreo(clienteActualizado.getCorreo());

            return clienteRepository.save(clienteExistente);
        }).orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
    }

    public void eliminar(Integer id) {
        clienteRepository.deleteById(id);
    }
}
