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

    private static final String FRONTEND_BASE_URL = "http://3.135.134.201";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        try {
            System.out.println("=== OAuth2 Authentication Success ===");
            System.out.println("Authentication: " + authentication);

            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oauth2User.getAttributes();

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
                response.sendRedirect(FRONTEND_BASE_URL + "/login?error=EmailNotFoundInClaims");
                return;
            }

            boolean userExists = usuarioRepository.findByCorreo(email).isPresent();

            String name = oauth2User.getAttribute("name");
            if (name == null && attributes.get("given_name") != null) {
                name = attributes.get("given_name").toString();
                if (attributes.get("family_name") != null) {
                    name += " " + attributes.get("family_name").toString();
                }
            }

            String encodedName = name != null ? URLEncoder.encode(name, StandardCharsets.UTF_8) : "";
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);

            if (userExists) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                String token = jwtUtil.generateToken(userDetails);
                String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
                response.sendRedirect(FRONTEND_BASE_URL + "/dashboard?token=" + encodedToken);
            } else {
                // Nuevo usuario → no se genera token
                response.sendRedirect(FRONTEND_BASE_URL + "/complete-registration?email=" + encodedEmail + "&name=" + encodedName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(FRONTEND_BASE_URL + "/error?reason=AuthenticationError");
        }
    }
}
