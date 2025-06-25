package com.taller.usuarioback.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.cors.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    // Exponer AuthenticationManager para poder usarlo (p.ej. en tests)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/registro",
                    "/api/upload",
                    "/api/registro-completo",
                    "/create-admin",
                    "/oauth2/**",
                    "/login/**",
                    "/api/oauth2-test",
                    "/api/oauth2-error",
                    "/api/oauth2-debug"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                // URL de inicio del login OAuth2
                .loginPage("/oauth2/authorization/B2C_1_DuocUCDemoAzure_Login")
                .successHandler(customAuthenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler())
            )
            .logout(logout -> logout
                .logoutSuccessUrl("http://localhost:4200")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new AuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, 
                                              HttpServletResponse response, 
                                              org.springframework.security.core.AuthenticationException exception) 
                    throws IOException, ServletException {
                
                System.err.println("OAuth2 Authentication failed: " + exception.getMessage());
                exception.printStackTrace();
                
                // Redirect to Angular with error information
                String errorMessage = exception.getMessage() != null ? 
                    exception.getMessage().replaceAll("[^a-zA-Z0-9\\s]", "") : "Authentication failed";
                response.sendRedirect("http://localhost:4200/login?error=" + errorMessage);
            }
        };
    }

    // Configuración de CORS para permitir tu Angular desde http://localhost:4200
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of(
            "http://localhost:4200",
            "http://3.135.134.201",
            "https://3.135.134.201"
        ));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

}
