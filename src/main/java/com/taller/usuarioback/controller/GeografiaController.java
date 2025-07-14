package com.taller.usuarioback.controller;

import com.taller.usuarioback.repository.RegionRepository;
import com.taller.usuarioback.repository.ProvinciaRepository;
import com.taller.usuarioback.repository.ComunaRepository;
import com.taller.usuarioback.model.Region;
import com.taller.usuarioback.model.Provincia;
import com.taller.usuarioback.model.Comuna;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/geografia")
@CrossOrigin(origins = "*")
public class GeografiaController {

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private ProvinciaRepository provinciaRepository;

    @Autowired
    private ComunaRepository comunaRepository;

    // Obtener todas las regiones
    @GetMapping("/regiones")
    public ResponseEntity<List<Region>> obtenerRegiones() {
        List<Region> regiones = regionRepository.findAll();
        return ResponseEntity.ok(regiones);
    }

    // Obtener provincias por región
    @GetMapping("/provincias/{regionId}")
    public ResponseEntity<List<Provincia>> obtenerProvinciasPorRegion(@PathVariable Long regionId) {
        List<Provincia> provincias = provinciaRepository.findByRegionId(regionId);
        return ResponseEntity.ok(provincias);
    }

    // Obtener comunas por provincia
    @GetMapping("/comunas/{provinciaId}")
    public ResponseEntity<List<Comuna>> obtenerComunasPorProvincia(@PathVariable Long provinciaId) {
        List<Comuna> comunas = comunaRepository.findByProvinciaId(provinciaId);
        return ResponseEntity.ok(comunas);
    }

    // Obtener todas las provincias (para casos especiales)
    @GetMapping("/provincias")
    public ResponseEntity<List<Provincia>> obtenerTodasProvincias() {
        List<Provincia> provincias = provinciaRepository.findAll();
        return ResponseEntity.ok(provincias);
    }

    // Obtener todas las comunas (para casos especiales)
    @GetMapping("/comunas")
    public ResponseEntity<List<Comuna>> obtenerTodasComunas() {
        List<Comuna> comunas = comunaRepository.findAll();
        return ResponseEntity.ok(comunas);
    }
} 