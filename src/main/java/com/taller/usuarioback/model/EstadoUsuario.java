package com.taller.usuarioback.model;

import jakarta.persistence.*;

@Entity
@Table(name = "estados_usuario")
public class EstadoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String estado; // Ej: ACTIVO, INACTIVO, BLOQUEADO

    public EstadoUsuario() {}

    // Getters y setters
    public Long getId() {
        return id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public EstadoUsuario(Long id, String estado) {
    this.id = id;
    this.estado = estado;
}

}
