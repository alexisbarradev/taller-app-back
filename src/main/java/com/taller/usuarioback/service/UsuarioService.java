package com.taller.usuarioback.service;

import com.taller.usuarioback.model.Usuario;
import com.taller.usuarioback.repository.UsuarioRepository;
import com.taller.usuarioback.repository.RolUsuarioRepository;
import com.taller.usuarioback.repository.EstadoUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolUsuarioRepository rolRepository;

    @Autowired
    private EstadoUsuarioRepository estadoRepository;

    public String crearUsuario(Usuario usuario) {
        if (usuarioRepository.existsByRut(usuario.getRut())) {
            return "Error: El RUT ya existe.";
        }
        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            return "Error: El correo ya está registrado.";
        }
        if (usuarioRepository.existsByUsuario(usuario.getUsuario())) {
            return "Error: El nombre de usuario ya está en uso.";
        }
        // Validar que el rol y estado existan
        boolean rolExiste = rolRepository.existsById(usuario.getRol().getId());
        boolean estadoExiste = estadoRepository.existsById(usuario.getEstado().getId());

        if (!rolExiste || !estadoExiste) {
            return "Error: El rol o el estado no existen.";
        }

        usuarioRepository.save(usuario);
        return "Usuario creado exitosamente.";
    }

    public Usuario crearUsuarioYDevolver(Usuario usuario) {
        if (usuarioRepository.existsByRut(usuario.getRut())) {
            throw new RuntimeException("El RUT ya existe en el sistema.");
        }
        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new RuntimeException("El correo electrónico ya está registrado.");
        }
        if (usuarioRepository.existsByUsuario(usuario.getUsuario())) {
            throw new RuntimeException("El nombre de usuario ya está en uso.");
        }
        
        boolean rolExiste = rolRepository.existsById(usuario.getRol().getId());
        boolean estadoExiste = estadoRepository.existsById(usuario.getEstado().getId());

        if (!rolExiste || !estadoExiste) {
            throw new RuntimeException("El rol o el estado especificado no existe.");
        }

        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public String actualizarUsuario(Long id, Usuario usuarioActualizado) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);

        if (usuarioOptional.isEmpty()) {
            return "Error: Usuario no encontrado.";
        }

        Usuario existente = usuarioOptional.get();

        // Actualiza los campos individuales
        existente.setPrimerNombre(usuarioActualizado.getPrimerNombre());
        existente.setSegundoNombre(usuarioActualizado.getSegundoNombre());
        existente.setApellidoPaterno(usuarioActualizado.getApellidoPaterno());
        existente.setApellidoMaterno(usuarioActualizado.getApellidoMaterno());
        existente.setCorreo(usuarioActualizado.getCorreo());
        existente.setUsuario(usuarioActualizado.getUsuario());
        existente.setRut(usuarioActualizado.getRut());
        existente.setNumeroContacto(usuarioActualizado.getNumeroContacto());
        existente.setDireccion(usuarioActualizado.getDireccion());
        existente.setRol(usuarioActualizado.getRol());
        existente.setEstado(usuarioActualizado.getEstado());
        existente.setProveedorAutenticacion(usuarioActualizado.getProveedorAutenticacion());
        existente.setRegion(usuarioActualizado.getRegion());
        existente.setProvincia(usuarioActualizado.getProvincia());
        existente.setComuna(usuarioActualizado.getComuna());
        existente.setComunidad(usuarioActualizado.getComunidad());
        
        if (usuarioActualizado.getUrlContrato() != null) {
            existente.setUrlContrato(usuarioActualizado.getUrlContrato());
        }

        // 🧪 Logging antes de guardar
        System.out.println("🧪 Guardando usuario:");
        System.out.println("ID: " + existente.getId());
        System.out.println("Correo: " + existente.getCorreo());
        System.out.println("RUT: " + existente.getRut());
        System.out.println("Usuario: " + existente.getUsuario());
        System.out.println("Número de contacto: " + existente.getNumeroContacto());
        System.out.println("Región: " + (existente.getRegion() != null ? existente.getRegion().getNombre() : "null"));
        System.out.println("Provincia: " + (existente.getProvincia() != null ? existente.getProvincia().getNombre() : "null"));
        System.out.println("Comuna: " + (existente.getComuna() != null ? existente.getComuna().getNombre() : "null"));
        System.out.println("Comunidad: " + (existente.getComunidad() != null ? existente.getComunidad().getNombreComunidad() : "null"));

        usuarioRepository.save(existente);

        return "Usuario actualizado correctamente.";
    }

    public String eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            return "Error: Usuario no encontrado.";
        }
        usuarioRepository.deleteById(id);
        return "Usuario eliminado correctamente.";
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    public Optional<Usuario> buscarPorUsuario(String nombreUsuario) {
        return usuarioRepository.findByUsuario(nombreUsuario);
    }

    public Optional<Usuario> buscarPorRut(String rut) {
        return usuarioRepository.findByRut(rut);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }
}
