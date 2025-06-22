package com.taller.usuarioback.controller;

import com.taller.usuarioback.model.Usuario;
import com.taller.usuarioback.service.UsuarioService;
import com.taller.usuarioback.service.S3Service;
import com.taller.usuarioback.model.RolUsuario;
import com.taller.usuarioback.model.EstadoUsuario;
import com.taller.usuarioback.repository.RolUsuarioRepository;
import com.taller.usuarioback.repository.EstadoUsuarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private RolUsuarioRepository rolUsuarioRepository;

    @Autowired
    private EstadoUsuarioRepository estadoUsuarioRepository;

    // 🟢 POST /api/registro
    // Crea un nuevo usuario después de validar RUT, correo, usuario, rol y estado.
    @PostMapping("/registro")
    public ResponseEntity<String> registrarUsuario(@Valid @RequestBody Usuario usuario) {
        String resultado = usuarioService.crearUsuario(usuario);
        if (resultado.startsWith("Error")) {
            return ResponseEntity.badRequest().body(resultado);
        }
        return ResponseEntity.ok(resultado);
    }

    // 🟢 POST /api/registro-completo
    // Crea un nuevo usuario después de validar RUT, correo, usuario, rol y estado, y maneja la carga de archivos.
    @PostMapping("/registro-completo")
    public ResponseEntity<?> registrarUsuarioCompleto(
            @RequestParam("rut") String rut,
            @RequestParam("primerNombre") String primerNombre,
            @RequestParam("segundoNombre") String segundoNombre,
            @RequestParam("apellidoPaterno") String apellidoPaterno,
            @RequestParam("apellidoMaterno") String apellidoMaterno,
            @RequestParam("direccion") String direccion,
            @RequestParam("usuario") String usuario,
            @RequestParam("correo") String correo,
            @RequestParam("rol") String rolJson,
            @RequestParam("estado") String estadoJson,
            @RequestParam(value = "proveedorAutenticacion", required = false) String proveedorAutenticacion,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpServletRequest request) {
        
        try {
            // Log the registration attempt
            System.out.println("=== REGISTRATION ATTEMPT ===");
            System.out.println("RUT: " + rut);
            System.out.println("Email: " + correo);
            System.out.println("Username: " + usuario);
            System.out.println("Request ID: " + request.getSession().getId());
            
            ObjectMapper mapper = new ObjectMapper();

            // --- Fetch Rol ---
            Map<String, Long> rolMap = mapper.readValue(rolJson, new TypeReference<Map<String, Long>>() {});
            Long rolId = rolMap.get("id");
            RolUsuario rol = rolUsuarioRepository.findById(rolId)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado para el ID: " + rolId));

            // --- Fetch Estado ---
            Map<String, Long> estadoMap = mapper.readValue(estadoJson, new TypeReference<Map<String, Long>>() {});
            Long estadoId = estadoMap.get("id");
            EstadoUsuario estado = estadoUsuarioRepository.findById(estadoId)
                    .orElseThrow(() -> new RuntimeException("Estado no encontrado para el ID: " + estadoId));

            // Create user object
            Usuario usuarioObj = new Usuario();
            usuarioObj.setRut(rut);
            usuarioObj.setPrimerNombre(primerNombre);
            usuarioObj.setSegundoNombre(segundoNombre);
            usuarioObj.setApellidoPaterno(apellidoPaterno);
            usuarioObj.setApellidoMaterno(apellidoMaterno);
            usuarioObj.setDireccion(direccion);
            usuarioObj.setUsuario(usuario);
            usuarioObj.setCorreo(correo);
            usuarioObj.setRol(rol);
            usuarioObj.setEstado(estado);
            usuarioObj.setProveedorAutenticacion(proveedorAutenticacion);

            // Handle file upload if present
            if (file != null && !file.isEmpty()) {
                String fileUrl = s3Service.uploadFile(file);
                usuarioObj.setUrlContrato(fileUrl);
            }

            // Save user
            Usuario nuevoUsuario = usuarioService.crearUsuarioYDevolver(usuarioObj);
            
            System.out.println("✅ User created successfully with ID: " + nuevoUsuario.getId());

            // --- Programmatic Login ---
            // Create a new authentication token
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                nuevoUsuario.getUsuario(), 
                null, // Credentials can be null for programmatic login
                nuevoUsuario.getAuthorities() // Assumes getAuthorities() is implemented in Usuario
            );

            // Set the authentication in the security context
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            // Save the context in the session
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

            return ResponseEntity.ok(nuevoUsuario);

        } catch (RuntimeException e) {
            // Handle specific validation errors
            System.err.println("❌ Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            // Handle other unexpected errors
            System.err.println("❌ Unexpected error during registration: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al procesar la solicitud: " + e.getMessage());
        }
    }

    // 🟢 GET /api/usuarios
    // Lista todos los usuarios registrados.
    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    // 🟡 PUT /api/{id}
    // Actualiza un usuario existente por su ID.
    @PutMapping("/{id}")
    public ResponseEntity<String> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioActualizado) {
        String resultado = usuarioService.actualizarUsuario(id, usuarioActualizado);
        if (resultado.startsWith("Error")) {
            return ResponseEntity.badRequest().body(resultado);
        }
        return ResponseEntity.ok(resultado);
    }

    // 🔴 DELETE /api/{id}
    // Elimina un usuario por su ID.
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable Long id) {
        String resultado = usuarioService.eliminarUsuario(id);
        if (resultado.startsWith("Error")) {
            return ResponseEntity.badRequest().body(resultado);
        }
        return ResponseEntity.ok(resultado);
    }

    // 🔍 GET /api/usuarios/correo/{correo}
    // Busca un usuario por correo electrónico.
    @GetMapping("/usuarios/correo/{correo}")
    public ResponseEntity<Usuario> buscarPorCorreo(@PathVariable String correo) {
        String correoDecodificado = URLDecoder.decode(correo, StandardCharsets.UTF_8);
        return usuarioService.buscarPorCorreo(correoDecodificado)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // 🔍 GET /api/usuarios/usuario/{nombreUsuario}
    // Busca un usuario por nombre de usuario.
    @GetMapping("/usuarios/usuario/{nombreUsuario}")
    public ResponseEntity<Usuario> buscarPorUsuario(@PathVariable String nombreUsuario) {
        return usuarioService.buscarPorUsuario(nombreUsuario)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // 🔍 GET /api/usuarios/rut/{rut}
    // Busca un usuario por su RUT.
    @GetMapping("/usuarios/rut/{rut}")
    public ResponseEntity<Usuario> buscarPorRut(@PathVariable String rut) {
        return usuarioService.buscarPorRut(rut)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OidcUser principal) {
        if (principal == null) {
            return Map.of("error", "No user authenticated");
        }
        return Map.of(
            "email", principal.getEmail(),
            "name", principal.getFullName(),
            "attributes", principal.getAttributes()
        );
    }
}
