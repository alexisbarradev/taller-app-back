package com.taller.usuarioback.repository;

import com.taller.usuarioback.model.Comunidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComunidadRepository extends JpaRepository<Comunidad, Long> {
    List<Comunidad> findByComunaId(Long comunaId);
    Comunidad findByNombreComunidad(String nombreComunidad);
} 