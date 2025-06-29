package com.taller.usuarioback.config;

import com.taller.usuarioback.repository.UsuarioRepository;
import com.taller.usuarioback.service.UserDetailsServiceImpl;
import com.taller.usuarioback.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String FRONTEND_BASE_URL = "https://3.135.134.201:4200";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        try {
            System.out.println("=== OAuth2 Authentication Success ===");
            System.out.println("Authentication: " + authentication);

            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oauth2User.getAttributes();

            // Extract email from OAuth2 attributes
            String email = extractEmailFromAttributes(attributes);
            System.out.println("Extracted email: " + email);

            if (email == null) {
                System.err.println("ERROR: No email found in OAuth2 attributes");
                response.sendRedirect(FRONTEND_BASE_URL + "/login?error=EmailNotFoundInClaims");
                return;
            }

            // Extract name from OAuth2 attributes
            String name = extractNameFromAttributes(oauth2User, attributes);
            String encodedName = name != null ? URLEncoder.encode(name, StandardCharsets.UTF_8) : "";
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);

            // Check if user exists in database
            boolean userExists = usuarioRepository.findByCorreo(email).isPresent();
            System.out.println("User exists in database: " + userExists);

            if (userExists) {
                // Existing user - generate JWT token and redirect to dashboard
                System.out.println("Processing existing user: " + email);
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    String token = jwtUtil.generateToken(userDetails);
                    String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
                    System.out.println("Generated JWT token for existing user");
                    response.sendRedirect(FRONTEND_BASE_URL + "/dashboard?token=" + encodedToken);
                } catch (Exception e) {
                    System.err.println("ERROR: Failed to load user details for existing user: " + e.getMessage());
                    e.printStackTrace();
                    response.sendRedirect(FRONTEND_BASE_URL + "/error?reason=UserDetailsLoadError");
                }
            } else {
                // New user - redirect to complete registration
                System.out.println("Processing new user: " + email);
                System.out.println("Redirecting to complete registration with email: " + email + " and name: " + name);
                response.sendRedirect(FRONTEND_BASE_URL + "/complete-registration?email=" + encodedEmail + "&name=" + encodedName);
            }

        } catch (Exception e) {
            System.err.println("ERROR: Unexpected error in authentication success handler: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(FRONTEND_BASE_URL + "/error?reason=AuthenticationError");
        }
    }

    private String extractEmailFromAttributes(Map<String, Object> attributes) {
        // Try different possible email claim names
        if (attributes.get("email") != null) {
            return attributes.get("email").toString();
        } else if (attributes.get("emails") instanceof List<?> emails && !emails.isEmpty()) {
            return emails.get(0).toString();
        } else if (attributes.get("upn") != null) {
            return attributes.get("upn").toString();
        } else if (attributes.get("preferred_username") != null) {
            return attributes.get("preferred_username").toString();
        }
        return null;
    }

    private String extractNameFromAttributes(OAuth2User oauth2User, Map<String, Object> attributes) {
        // Try to get name from OAuth2User first
        String name = oauth2User.getAttribute("name");
        
        // If not found, try to construct from given_name and family_name
        if (name == null) {
            String givenName = null;
            String familyName = null;
            
            if (attributes.get("given_name") != null) {
                givenName = attributes.get("given_name").toString();
            }
            if (attributes.get("family_name") != null) {
                familyName = attributes.get("family_name").toString();
            }
            
            if (givenName != null && familyName != null) {
                name = givenName + " " + familyName;
            } else if (givenName != null) {
                name = givenName;
            } else if (familyName != null) {
                name = familyName;
            }
        }
        
        return name;
    }
}
