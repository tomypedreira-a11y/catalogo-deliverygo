package com.catalogodeliverygo.demo.Secutiry;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

// DM-16: escribir productos exige rol ADMIN o COMERCIO. Sin token 401, con otro rol 403 (DM-15).
// Base en memoria propia, igual que OpenApiYamlTest, para no cargar data.sql dos veces sobre la misma base
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:seguridad-test")
@AutoConfigureMockMvc
class SeguridadProductosTest {

    private static final String NUEVO_PRODUCTO =
        "{\"nombre\":\"Papas fritas\",\"descripcion\":\"Porción\",\"precio\":4500,\"categoriaIds\":[1],\"comercioId\":1}";
    private static final String NUEVO_PRECIO = "{\"nuevoPrecio\":9000}";

    @Autowired
    private MockMvc mockMvc;

    // Misma clave que usa JwtService (app.jwt.secret)
    @Value("${app.jwt.secret}")
    private String secreto;

    private String token(String rol) {
        return Jwts.builder()
                   .subject("usuario-" + rol.toLowerCase())
                   .claim("role", rol)
                   .signWith(Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8)))
                   .compact();
    }

    private MockHttpServletRequestBuilder publicar() {
        return post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(NUEVO_PRODUCTO);
    }

    private MockHttpServletRequestBuilder modificarPrecio() {
        return patch("/api/v1/productos/1/precio").contentType(MediaType.APPLICATION_JSON).content(NUEVO_PRECIO);
    }

    // ---- POST /api/v1/productos (CU-02) ----

    @Test
    void publicarSinTokenDa401() throws Exception {
        mockMvc.perform(publicar())
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.status").value(401))
               .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
               .andExpect(jsonPath("$.path").value("/api/v1/productos"));
    }

    @Test
    void publicarConRolClienteDa403() throws Exception {
        mockMvc.perform(publicar().header("Authorization", "Bearer " + token("CLIENTE")))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("$.status").value(403))
               .andExpect(jsonPath("$.error").value("FORBIDDEN"))
               .andExpect(jsonPath("$.path").value("/api/v1/productos"));
    }

    @Test
    void publicarConRolComercioDa201() throws Exception {
        mockMvc.perform(publicar().header("Authorization", "Bearer " + token("COMERCIO")))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void publicarConRolAdminDa201() throws Exception {
        mockMvc.perform(publicar().header("Authorization", "Bearer " + token("ADMIN")))
               .andExpect(status().isCreated());
    }

    // ---- PATCH /api/v1/productos/{id}/precio (CU-03) ----

    @Test
    void modificarPrecioSinTokenDa401() throws Exception {
        mockMvc.perform(modificarPrecio())
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void modificarPrecioConRolClienteDa403() throws Exception {
        mockMvc.perform(modificarPrecio().header("Authorization", "Bearer " + token("CLIENTE")))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("$.error").value("FORBIDDEN"))
               .andExpect(jsonPath("$.path").value("/api/v1/productos/1/precio"));
    }

    @Test
    void modificarPrecioConRolComercioDa200() throws Exception {
        mockMvc.perform(modificarPrecio().header("Authorization", "Bearer " + token("COMERCIO")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.precio").value(9000));
    }

    @Test
    void modificarPrecioConRolAdminDa200() throws Exception {
        mockMvc.perform(modificarPrecio().header("Authorization", "Bearer " + token("ADMIN")))
               .andExpect(status().isOk());
    }

    // ---- Otras reglas que no cambian ----

    @Test
    void cambiarDisponibilidadConRolClienteDa403() throws Exception {
        mockMvc.perform(patch("/api/v1/productos/1/disponibilidad")
                            .header("Authorization", "Bearer " + token("CLIENTE"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"disponible\":false}"))
               .andExpect(status().isForbidden());
    }

    @Test
    void unTokenConFirmaInvalidaDa401() throws Exception {
        mockMvc.perform(publicar().header("Authorization", "Bearer " + token("ADMIN") + "x"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void elDetalleDeProductoSigueSiendoPublico() throws Exception {
        mockMvc.perform(get("/api/v1/productos/2"))
               .andExpect(status().isOk());
    }

    // DM-19: los dos orígenes por defecto pasan el preflight, incluido PATCH
    @Test
    void corsPermiteLosOrigenesConfigurados() throws Exception {
        for (String origen : new String[] {"http://localhost:5173", "http://localhost:5174"}) {
            mockMvc.perform(options("/api/v1/productos/1/precio")
                                .header("Origin", origen)
                                .header("Access-Control-Request-Method", "PATCH"))
                   .andExpect(status().isOk())
                   .andExpect(header().string("Access-Control-Allow-Origin", origen));
        }
    }

    @Test
    void corsRechazaUnOrigenNoConfigurado() throws Exception {
        mockMvc.perform(options("/api/v1/productos/1/precio")
                            .header("Origin", "http://otro-sitio.com")
                            .header("Access-Control-Request-Method", "PATCH"))
               .andExpect(status().isForbidden());
    }
}
