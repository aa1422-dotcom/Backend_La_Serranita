package com.laserranita.backend.repositories;

import com.laserranita.backend.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    @Query("SELECT p FROM Producto p WHERE p.stockActual IS NOT NULL AND " +
           "(p.stockActual <= COALESCE(p.stockMinimo, 5) OR p.stockActual = 0)")
    List<Producto> findLowStock();

    @Query("SELECT p FROM Producto p WHERE p.fechaVencimiento IS NOT NULL AND " +
           "p.fechaVencimiento <= :limit AND p.fechaVencimiento >= CURRENT_DATE")
    List<Producto> findExpiringSoon(@Param("limit") LocalDate limit);
}