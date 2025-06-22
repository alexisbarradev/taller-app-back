package com.taller.usuarioback.service;

import com.taller.usuarioback.model.EstadoUsuario;
import com.taller.usuarioback.model.Usuario;
import com.taller.usuarioback.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Esta clase implementa UserDetailsService de Spring Security.
 * Su tarea es buscar en la base de datos al usuario (por nombre de usuario)
 * y devolver un UserDetails con sus credenciales para que Spring Security pueda autenticarlo.
 * 
 * Validamos también que el usuario tenga el rol de "ADMIN" (por nombre),
 * para permitir el acceso sólo a los administradores.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca al usuario en la base de datos
        Usuario usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Validamos que el usuario esté activo
        if (usuario.getEstado() == null || !"ACTIVO".equals(usuario.getEstado().getEstado())) {
            throw new UsernameNotFoundException("El usuario no está activo.");
        }

        // Creamos la lista de authorities (roles)
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre())
        );

        // Creamos y devolvemos el UserDetails
        return new org.springframework.security.core.userdetails.User(
                usuario.getUsuario(),
                "", // La contraseña no es necesaria para OAuth2
                authorities
        );
    }
}
