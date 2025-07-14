package com.taller.usuarioback.repository;

import com.taller.usuarioback.model.Comuna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComunaRepository extends JpaRepository<Comuna, Long> {
    Comuna findByNombre(String nombre);
    List<Comuna> findByProvinciaId(Long provinciaId);
} 