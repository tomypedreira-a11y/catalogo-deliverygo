package com.catalogodeliverygo.demo.Secutiry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;
    private final List<String> origenesPermitidos;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter,
                          JsonAuthenticationEntryPoint authenticationEntryPoint,
                          JsonAccessDeniedHandler accessDeniedHandler,
                          @Value("${app.cors.allowed-origins}") String origenesPermitidos) {
        this.jwtFilter = jwtFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        // DM-19: lista separada por coma (CORS_ALLOWED_ORIGINS); se ignoran espacios y elementos vacíos
        this.origenesPermitidos = Arrays.stream(origenesPermitidos.split(","))
                                        .map(String::trim)
                                        .filter(origen -> !origen.isEmpty())
                                        .toList();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).cors(cors -> {
                }).headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs.yaml", "/h2-console/**", "/error").permitAll()
                        // Health check del ecosistema: público, sin token; va antes de las reglas generales
                        .requestMatchers(HttpMethod.GET, "/api/v1/status").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/comercios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/catalogo/**", "/api/v1/productos/**").permitAll()
                        // DM-16: CU-02, CU-03, CU-04 y CU-09 escriben productos; solo ADMIN o COMERCIO.
                        // Van antes de anyRequest(), que solo exigiría estar autenticado
                        .requestMatchers(HttpMethod.POST, "/api/v1/productos/**").hasAnyRole("ADMIN", "COMERCIO")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/productos/**").hasAnyRole("ADMIN", "COMERCIO")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/productos/**").hasAnyRole("ADMIN", "COMERCIO")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/productos/**").hasAnyRole("ADMIN", "COMERCIO")
                        // CU-08: categorías de lectura pública y administradas solo por ADMIN (DM-02)
                        .requestMatchers(HttpMethod.GET, "/api/v1/categorias/**").permitAll()
                        .requestMatchers("/api/v1/categorias/**").hasRole("ADMIN")
                        // CU-07: el comercio puede borrar sus sucursales; va antes de la regla general de DELETE
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/comercios/*/sucursales/**").hasAnyRole("ADMIN", "COMERCIO")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/comercios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/comercios/**").hasAnyRole("ADMIN", "COMERCIO")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/comercios/**").hasAnyRole("ADMIN", "COMERCIO")
                        .anyRequest().authenticated())
                // DM-15: 401 sin token válido, 403 con rol insuficiente; ambos con ErrorResponseDTO
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(origenesPermitidos);
        // PATCH lo usan API-03, API-04 y API-12; sin él el navegador bloquea esas llamadas del front
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }
}
