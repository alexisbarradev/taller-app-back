package com.taller.usuarioback.repository;

import com.taller.usuarioback.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {
    Region findByNombre(String nombre);
} 