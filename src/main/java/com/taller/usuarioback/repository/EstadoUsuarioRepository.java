package com.taller.usuarioback.repository;

import com.taller.usuarioback.model.EstadoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoUsuarioRepository extends JpaRepository<EstadoUsuario, Long> {
    Optional<EstadoUsuario> findByEstado(String estado);
}
