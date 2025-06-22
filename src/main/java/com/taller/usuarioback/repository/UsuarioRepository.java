package com.taller.usuarioback.repository;

import com.taller.usuarioback.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByRut(String rut);
    boolean existsByCorreo(String correo);
    boolean existsByUsuario(String usuario);
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByUsuario(String usuario);
    Optional<Usuario> findByRut(String rut);

}
