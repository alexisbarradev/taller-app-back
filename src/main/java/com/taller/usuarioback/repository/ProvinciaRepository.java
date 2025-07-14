package com.taller.usuarioback.repository;

import com.taller.usuarioback.model.Provincia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProvinciaRepository extends JpaRepository<Provincia, Long> {
    Provincia findByNombre(String nombre);
    List<Provincia> findByRegionId(Long regionId);
} 