package com.taller.usuarioback.config;

import com.taller.usuarioback.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        try {
            System.out.println("=== OAuth2 Authentication Success ===");
            System.out.println("Authentication: " + authentication);
            
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            System.out.println("OAuth2User: " + oauth2User);
            
            // Log all available attributes for debugging
            Map<String, Object> attributes = oauth2User.getAttributes();
            System.out.println("Available attributes: " + attributes.keySet());
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }

            String email = null;
            // Try to find email from common claim names
            if (attributes.get("email") != null) {
                email = attributes.get("email").toString();
            } else if (attributes.get("emails") != null && attributes.get("emails") instanceof List && !((List<?>) attributes.get("emails")).isEmpty()) {
                email = ((List<?>) attributes.get("emails")).get(0).toString();
            } else if (attributes.get("upn") != null) { // User Principal Name
                email = attributes.get("upn").toString();
            } else if (attributes.get("preferred_username") != null) {
                email = attributes.get("preferred_username").toString();
            }

            System.out.println("Extracted email: " + email);

            if (email == null) {
                // Email no disponible, redirigir a error
                System.err.println("Email not found in OAuth2 attributes. Please ensure the 'Email Addresses' claim is enabled in your Azure B2C user flow.");
                response.sendRedirect("http://localhost:4200/login?error=EmailNotFoundInClaims");
                return;
            }

            boolean userExists = usuarioRepository.findByCorreo(email).isPresent();
            System.out.println("User exists in database: " + userExists);

            String name = oauth2User.getAttribute("name");
            if (name == null) {
                // Try other common name attributes
                if (attributes.get("given_name") != null) {
                    name = attributes.get("given_name").toString();
                    if(attributes.get("family_name") != null) {
                        name += " " + attributes.get("family_name").toString();
                    }
                }
            }

            String encodedName = name != null
                    ? URLEncoder.encode(name, StandardCharsets.UTF_8)
                    : "";

            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);

            if (userExists) {
                // Usuario registrado, redirigir al dashboard
                System.out.println("Redirecting to dashboard");
                response.sendRedirect("http://localhost:4200/dashboard");
            } else {
                // Usuario no registrado, enviar al registro
                System.out.println("Redirecting to complete registration");
                response.sendRedirect("http://localhost:4200/complete-registration?email=" + encodedEmail + "&name=" + encodedName);
            }
            
        } catch (Exception e) {
            System.err.println("Error in CustomAuthenticationSuccessHandler: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("http://localhost:4200/error?reason=AuthenticationError");
        }
    }
}
