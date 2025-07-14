package com.taller.usuarioback.model;

import jakarta.persistence.*;

@Entity
@Table(name = "comunidades")
public class Comunidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreComunidad;

    @Column(nullable = false)
    private String direccion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_comuna")
    private Comuna comuna;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_provincia")
    private Provincia provincia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_region")
    private Region region;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreComunidad() { return nombreComunidad; }
    public void setNombreComunidad(String nombreComunidad) { this.nombreComunidad = nombreComunidad; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public Comuna getComuna() { return comuna; }
    public void setComuna(Comuna comuna) { this.comuna = comuna; }
    public Provincia getProvincia() { return provincia; }
    public void setProvincia(Provincia provincia) { this.provincia = provincia; }
    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }
} 