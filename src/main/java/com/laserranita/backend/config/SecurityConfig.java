package com.laserranita.backend.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/uploads/**").permitAll()
                
                // Endpoints exclusivos de administración
                .requestMatchers("/api/v1/usuarios/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/v1/roles/**").hasRole("ADMINISTRADOR")
                // Modificaciones de proveedores solo para Admin, consulta (GET) permitida para vendedor
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/proveedores/**").hasRole("ADMINISTRADOR")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/proveedores/**").hasRole("ADMINISTRADOR")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/v1/proveedores/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/v1/inventario/ajuste/**").hasRole("ADMINISTRADOR")
                
                // Modificaciones de productos solo para Admin
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/productos/**").hasRole("ADMINISTRADOR")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/productos/**").hasRole("ADMINISTRADOR")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/v1/productos/**").hasRole("ADMINISTRADOR")
                
                // Modificaciones de movimientos e inventario solo para Admin
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/inventario/**").hasRole("ADMINISTRADOR")
                
                // Aprobación o rechazo de ventas preprogramadas permitidas para ambos roles
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/ventas/*/aprobar").hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/ventas/*/rechazar").hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                
                // Modificaciones de ventas confirmadas solo para Admin
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/ventas/**").hasRole("ADMINISTRADOR")
                
                // Pedidos (consultar, crear y actualizar estado permitidos para ambos roles)
                .requestMatchers("/api/v1/pedidos/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                
                // Cualquier otra petición autenticada (GET productos, GET ventas, POST ventas/crear, etc.)
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    // Cuando falla la autenticación, respondemos 401 Unauthorized en lugar de 403 Forbidden.
                    // Esto activa la limpieza de localStorage en el frontend (axios.js) y redirige al login.
                    System.out.println("DEBUG SECURITY: Acceso denegado (no autenticado). Enviando 401.");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"mensaje\": \"Debe iniciar sesión para acceder a este recurso.\"}");
                })
            );

        // Agregamos nuestro filtro de JWT antes del filtro de usuario/contraseña estándar
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
