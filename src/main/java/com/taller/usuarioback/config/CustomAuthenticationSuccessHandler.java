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

    private static final String FRONTEND_BASE_URL = "http://3.135.134.201:4200";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        try {
            System.out.println("=== OAuth2 Authentication Success ===");
            System.out.println("Authentication: " + authentication);
            
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            System.out.println("OAuth2User: " + oauth2User);
            
            Map<String, Object> attributes = oauth2User.getAttributes();
            System.out.println("Available attributes: " + attributes.keySet());
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }

            String email = null;
            if (attributes.get("email") != null) {
                email = attributes.get("email").toString();
            } else if (attributes.get("emails") instanceof List<?> emails && !emails.isEmpty()) {
                email = emails.get(0).toString();
            } else if (attributes.get("upn") != null) {
                email = attributes.get("upn").toString();
            } else if (attributes.get("preferred_username") != null) {
                email = attributes.get("preferred_username").toString();
            }

            System.out.println("Extracted email: " + email);

            if (email == null) {
                System.err.println("Email not found in OAuth2 attributes.");
                response.sendRedirect(FRONTEND_BASE_URL + "/login?error=EmailNotFoundInClaims");
                return;
            }

            boolean userExists = usuarioRepository.findByCorreo(email).isPresent();
            System.out.println("User exists in database: " + userExists);

            String name = oauth2User.getAttribute("name");
            if (name == null && attributes.get("given_name") != null) {
                name = attributes.get("given_name").toString();
                if (attributes.get("family_name") != null) {
                    name += " " + attributes.get("family_name").toString();
                }
            }

            String encodedName = name != null
                    ? URLEncoder.encode(name, StandardCharsets.UTF_8)
                    : "";

            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);

            if (userExists) {
                System.out.println("Redirecting to dashboard");
                response.sendRedirect(FRONTEND_BASE_URL + "/dashboard");
            } else {
                System.out.println("Redirecting to complete registration");
                response.sendRedirect(FRONTEND_BASE_URL + "/complete-registration?email=" + encodedEmail + "&name=" + encodedName);
            }

        } catch (Exception e) {
            System.err.println("Error in CustomAuthenticationSuccessHandler: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(FRONTEND_BASE_URL + "/error?reason=AuthenticationError");
        }
    }
}
