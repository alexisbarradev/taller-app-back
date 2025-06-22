package com.taller.usuarioback.config;

import com.taller.usuarioback.model.EstadoUsuario;
import com.taller.usuarioback.model.RolUsuario;
import com.taller.usuarioback.repository.EstadoUsuarioRepository;
import com.taller.usuarioback.repository.RolUsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatosIniciales(RolUsuarioRepository rolRepo, EstadoUsuarioRepository estadoRepo) {
        return args -> {
            if (rolRepo.count() == 0) {
                rolRepo.save(new RolUsuario(1L, "ADMIN"));
                rolRepo.save(new RolUsuario(2L, "USER"));
                rolRepo.save(new RolUsuario(3L, "INVITADO"));
            }

            if (estadoRepo.count() == 0) {
                estadoRepo.save(new EstadoUsuario(1L, "ACTIVO"));
                estadoRepo.save(new EstadoUsuario(2L, "INACTIVO"));
                estadoRepo.save(new EstadoUsuario(3L, "BLOQUEADO"));
            }
        };
    }
}
