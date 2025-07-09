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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Optional;




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
// 🟢 POST /api/usuarios/imagen

@PostMapping("/registro-completo")
public ResponseEntity<?> registrarUsuarioCompleto(
        @RequestParam("rut") String rut,
        @RequestParam("primerNombre") String primerNombre,
        @RequestParam("segundoNombre") String segundoNombre,
        @RequestParam("apellidoPaterno") String apellidoPaterno,
        @RequestParam("apellidoMaterno") String apellidoMaterno,
        @RequestParam("direccion") String direccion,
        @RequestParam("usuario") String usuario,
        @RequestParam("rol") String rolJson,
        @RequestParam("estado") String estadoJson,
        @RequestParam(value = "proveedorAutenticacion", required = false) String proveedorAutenticacion,
        @RequestParam(value = "file", required = false) MultipartFile file,
        HttpServletRequest request,
        @AuthenticationPrincipal Jwt jwt) {

    try {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no presente o inválido.");
        }

        System.out.println("=== REGISTRATION ATTEMPT ===");
        System.out.println("RUT: " + rut);
        System.out.println("Username: " + usuario);
        System.out.println("Request ID: " + request.getSession().getId());

        List<String> emails = jwt.getClaimAsStringList("emails");
        if (emails == null || emails.isEmpty()) {
            return ResponseEntity.badRequest().body("El token no contiene un correo electrónico.");
        }
        String correo = emails.get(0);
        System.out.println("Correo extraído del token: " + correo);

        ObjectMapper mapper = new ObjectMapper();

        Map<String, Long> rolMap = mapper.readValue(rolJson, new TypeReference<>() {});
        Long rolId = rolMap.get("id");
        RolUsuario rol = rolUsuarioRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado para el ID: " + rolId));

        Map<String, Long> estadoMap = mapper.readValue(estadoJson, new TypeReference<>() {});
        Long estadoId = estadoMap.get("id");
        EstadoUsuario estado = estadoUsuarioRepository.findById(estadoId)
                .orElseThrow(() -> new RuntimeException("Estado no encontrado para el ID: " + estadoId));

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

        if (file != null && !file.isEmpty()) {
            String fileUrl = s3Service.uploadFile(file);
            usuarioObj.setUrlContrato(fileUrl);
        }

        Usuario nuevoUsuario = usuarioService.crearUsuarioYDevolver(usuarioObj);
        System.out.println("✅ User created successfully with ID: " + nuevoUsuario.getId());

        // 🔁 Incluir el mismo token JWT en la respuesta
        Map<String, Object> response = Map.of(
    "usuario", nuevoUsuario,
    "token", jwt.getTokenValue()  // ✅ solo esto, sin getToken()
);


        return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
        System.err.println("❌ Registration failed: " + e.getMessage());
        return ResponseEntity.badRequest().body("Error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("❌ Unexpected error during registration: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.badRequest().body("Error al procesar la solicitud: " + e.getMessage());
    }
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

    // MUY IMPORTANTE, ES EL QUE VALIDA SI EL USUARIO EXISTE O NO
    // Busca un usuario por correo electrónico.
    // 🔁 POST /api/usuarios/correo
    @PostMapping("/usuarios/correo")
    public ResponseEntity<Map<String, Boolean>> verificarCorreo(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsStringList("emails").get(0);

        if (email == null || email.isBlank()) {
            System.out.println("❌ Email no disponible en el token.");
            return ResponseEntity.badRequest().body(Map.of("exists", false));
        }

        boolean exists = usuarioService.buscarPorCorreo(email).isPresent();
        System.out.println("🔐 Verificando existencia de usuario con correo autenticado '" + email + "' → " + exists);

        return ResponseEntity.ok(Map.of("exists", exists));
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

    // 05 JUL:
    // 🔍 GET /api/usuarios/correo/{correo}
    // Devuelve el usuario completo a partir de su correo
    @GetMapping("/usuarios/correo/{correo}")
    public ResponseEntity<Usuario> obtenerUsuarioPorCorreo(@PathVariable String correo) {
        return usuarioService.buscarPorCorreo(correo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    // ✅ GET /api/usuarios
    // Lista todos los usuarios registrados.
    // Este endpoint ahora valida que:
    // 1. El JWT del usuario esté presente y sea válido.
    // 2. El correo del usuario exista en la base de datos.
    // 3. El usuario tenga rol de administrador (rol_id == 1).
    // Solo en ese caso se permite acceder a la lista completa de usuarios.
    // Si no se cumple alguna condición, se devuelve un error 403 (Prohibido).
    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios(@AuthenticationPrincipal Jwt jwt) {
        try {
            List<String> emails = jwt.getClaimAsStringList("emails");
            if (emails == null || emails.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No se pudo extraer el correo del token JWT."));
            }
            String correo = emails.get(0);

            Optional<Usuario> optUsuario = usuarioService.buscarPorCorreo(correo);
            if (optUsuario.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Usuario no registrado en el sistema."));
            }

            Usuario usuario = optUsuario.get();

            if (usuario.getRol().getId() != 1) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: se requieren privilegios de administrador."));
            }

            List<Usuario> usuarios = usuarioService.listarUsuarios();
            return ResponseEntity.ok(usuarios);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno al procesar la solicitud."));
        }
    }


    // ENDPOINT PARA RASTREAR UN USUARIO...LO USAREMOS PARA EL PUT:
    // 🔍 GET /api/usuarios/{id}
    // Obtiene un usuario por su ID
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

// 🟡 PUT /api/usuarios/{id}
// Actualiza un usuario existente con todos los datos, incluyendo imagen y JWT
@PutMapping("/usuarios/{id}")
public ResponseEntity<?> actualizarUsuarioCompleto(
        @PathVariable Long id,
        @RequestParam("rut") String rut,
        @RequestParam("primerNombre") String primerNombre,
        @RequestParam("segundoNombre") String segundoNombre,
        @RequestParam("apellidoPaterno") String apellidoPaterno,
        @RequestParam("apellidoMaterno") String apellidoMaterno,
        @RequestParam("direccion") String direccion,
        @RequestParam("usuario") String usuario,
        @RequestParam("correo") String correo, // ✅ Correo desde Angular
        @RequestParam("rol") String rolJson,
        @RequestParam("estado") String estadoJson,
        @RequestParam(value = "proveedorAutenticacion", required = false) String proveedorAutenticacion,
        @RequestParam(value = "file", required = false) MultipartFile file,
        @AuthenticationPrincipal Jwt jwt) {

    try {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token JWT no presente o inválido.");
        }

        ObjectMapper mapper = new ObjectMapper();

        Map<String, Long> rolMap = mapper.readValue(rolJson, new TypeReference<>() {});
        Long rolId = rolMap.get("id");
        RolUsuario rol = rolUsuarioRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado para el ID: " + rolId));

        Map<String, Long> estadoMap = mapper.readValue(estadoJson, new TypeReference<>() {});
        Long estadoId = estadoMap.get("id");
        EstadoUsuario estado = estadoUsuarioRepository.findById(estadoId)
                .orElseThrow(() -> new RuntimeException("Estado no encontrado para el ID: " + estadoId));

        Usuario usuarioObj = new Usuario();
        usuarioObj.setId(id);
        usuarioObj.setRut(rut);
        usuarioObj.setPrimerNombre(primerNombre);
        usuarioObj.setSegundoNombre(segundoNombre);
        usuarioObj.setApellidoPaterno(apellidoPaterno);
        usuarioObj.setApellidoMaterno(apellidoMaterno);
        usuarioObj.setDireccion(direccion);
        usuarioObj.setUsuario(usuario);
        usuarioObj.setCorreo(correo); // ✅ Usar correo enviado por el formulario
        usuarioObj.setRol(rol);
        usuarioObj.setEstado(estado);
        usuarioObj.setProveedorAutenticacion(proveedorAutenticacion);

        if (file != null && !file.isEmpty()) {
            String fileUrl = s3Service.uploadFile(file);
            usuarioObj.setUrlContrato(fileUrl);
        }

        String resultado = usuarioService.actualizarUsuario(id, usuarioObj);
        if (resultado.startsWith("Error")) {
            return ResponseEntity.badRequest().body(resultado);
        }

        return ResponseEntity.ok(Map.of("mensaje", resultado));

    } catch (Exception e) {
        return ResponseEntity.badRequest()
                .body("Error al actualizar el usuario: " + e.getMessage());
    }
}

@GetMapping("/usuarios/id-por-correo/{correo}")
public ResponseEntity<Map<String, Long>> obtenerIdPorCorreo(@PathVariable String correo) {
    return usuarioService.buscarPorCorreo(correo)
        .map(usuario -> ResponseEntity.ok(Map.of("id", usuario.getId())))
        .orElse(ResponseEntity.notFound().build());
}

} // Cierre de la clase UsuarioController
