package com.taller.usuarioback.controller;

import com.taller.usuarioback.model.EstadoUsuario;
import com.taller.usuarioback.model.RolUsuario;
import com.taller.usuarioback.model.Usuario;
import com.taller.usuarioback.repository.EstadoUsuarioRepository;
import com.taller.usuarioback.repository.RolUsuarioRepository;
import com.taller.usuarioback.repository.UsuarioRepository;
import com.taller.usuarioback.service.UserDetailsServiceImpl;
import com.taller.usuarioback.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para el endpoint /login.
 * Recibe las credenciales y devuelve el JWT si es correcto.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolUsuarioRepository rolUsuarioRepository;

    @Autowired
    private EstadoUsuarioRepository estadoUsuarioRepository;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * Endpoint POST /login.
     */
   

    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdminUser() {
        Optional<Usuario> existingAdmin = usuarioRepository.findByUsuario("admin");
        if (existingAdmin.isPresent()) {
            return ResponseEntity.badRequest().body("El usuario 'admin' ya existe.");
        }

        RolUsuario adminRol = rolUsuarioRepository.findByNombre("ADMIN")
                .orElseThrow(() -> new RuntimeException("Rol 'ADMIN' no encontrado."));
        
        EstadoUsuario activeStatus = estadoUsuarioRepository.findByEstado("ACTIVO")
                .orElseThrow(() -> new RuntimeException("Estado 'ACTIVO' no encontrado."));

        Usuario adminUser = new Usuario();
        adminUser.setUsuario("admin");
        adminUser.setRut("11111111-1");
        adminUser.setPrimerNombre("Admin");
        adminUser.setApellidoPaterno("User");
        adminUser.setApellidoMaterno("System");
        adminUser.setCorreo("admin@example.com");
        adminUser.setDireccion("123 Admin Street");
        adminUser.setProveedorAutenticacion("local");
        adminUser.setRol(adminRol);
        adminUser.setEstado(activeStatus);

        usuarioRepository.save(adminUser);

        return ResponseEntity.ok("Usuario 'admin' creado exitosamente.");
    }

    @GetMapping("/oauth2-test")
    public ResponseEntity<Map<String, Object>> testOAuth2Config() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "OAuth2 configuration is accessible");
        response.put("timestamp", new Date());
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/oauth2-error")
    public ResponseEntity<Map<String, Object>> getOAuth2Error() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "OAuth2 error endpoint");
        response.put("timestamp", new Date());
        response.put("status", "ERROR");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/oauth2-debug")
    public ResponseEntity<Map<String, Object>> debugOAuth2() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "OAuth2 debug endpoint");
        response.put("timestamp", new Date());
        response.put("status", "DEBUG");
        response.put("clientId", "c7cf710c-52ff-441e-bf5b-44204554da6b");
        response.put("authorizationUri", "https://proyectouc.b2clogin.com/proyectouc.onmicrosoft.com/B2C_1_DuocUCDemoAzure_Login/oauth2/v2.0/authorize");
        response.put("tokenUri", "https://proyectouc.b2clogin.com/proyectouc.onmicrosoft.com/B2C_1_DuocUCDemoAzure_Login/oauth2/v2.0/token");
        return ResponseEntity.ok(response);
    }
}
