package com.catalogodeliverygo.demo.Secutiry;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/*
 * DM-21: tener rol COMERCIO no alcanza para modificar cualquier comercio. En data.sql, prueba-comercio es
 * responsable de La Esquina (1) y Sushi Cerrado (4); prueba-comercio-2, de Don Pietro (2) y Helados del Sur (3).
 * Productos 1 a 6 son de La Esquina, 7 a 10 de Don Pietro y 13 de Sushi Cerrado; sucursales 1 a 3, de La Esquina.
 * Base propia, porque algunos tests escriben.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:propiedad-test")
@AutoConfigureMockMvc
class PropiedadDelComercioTest {

    private static final String DUENIO_LA_ESQUINA = "prueba-comercio";
    private static final String DUENIO_DON_PIETRO = "prueba-comercio-2";

    @Autowired
    private MockMvc mockMvc;

    @Value("${app.jwt.secret}")
    private String secreto;

    private String token(String usuario, String rol) {
        return "Bearer " + Jwts.builder()
                               .subject(usuario)
                               .claim("role", rol)
                               .signWith(Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8)))
                               .compact();
    }

    private ResultActions como(String usuario, String rol, MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.header("Authorization", token(usuario, rol)));
    }

    private ResultActions comoDuenioDeDonPietro(MockHttpServletRequestBuilder request) throws Exception {
        return como(DUENIO_DON_PIETRO, "COMERCIO", request);
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String cuerpo) {
        return request.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }

    private static void esProhibido(ResultActions resultado) throws Exception {
        resultado.andExpect(status().isForbidden())
                 .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                 .andExpect(jsonPath("$.message").value(containsString("no pertenece al usuario autenticado")));
    }

    // ---- Productos de otro comercio: 403 y no cambia nada ----

    @Test
    void noPuedeModificarElPrecioDeUnProductoAjeno() throws Exception {
        esProhibido(comoDuenioDeDonPietro(json(patch("/api/v1/productos/1/precio"), "{\"nuevoPrecio\":1}")));

        mockMvc.perform(get("/api/v1/productos/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.precio").value(8500.00));
    }

    @Test
    void noPuedeCambiarLaDisponibilidadDeUnProductoAjeno() throws Exception {
        esProhibido(comoDuenioDeDonPietro(json(patch("/api/v1/productos/1/disponibilidad"), "{\"disponible\":false}")));

        mockMvc.perform(get("/api/v1/productos/1")).andExpect(status().isOk());
    }

    @Test
    void noPuedeDesactivarUnProductoAjeno() throws Exception {
        esProhibido(comoDuenioDeDonPietro(json(patch("/api/v1/productos/1/estado"), "{\"activo\":false}")));

        mockMvc.perform(get("/api/v1/productos/1")).andExpect(status().isOk());
    }

    @Test
    void noPuedePublicarEnUnComercioAjeno() throws Exception {
        esProhibido(comoDuenioDeDonPietro(json(post("/api/v1/productos"),
            "{\"nombre\":\"Intruso\",\"descripcion\":\"X\",\"precio\":100,\"categoriaIds\":[1],\"comercioId\":1}")));
    }

    // La propiedad se revisa antes que el estado: sobre un comercio inactivo ajeno da 403, no 409
    @Test
    void unComercioInactivoAjenoTambienDa403() throws Exception {
        esProhibido(comoDuenioDeDonPietro(json(patch("/api/v1/productos/13/precio"), "{\"nuevoPrecio\":1}")));
    }

    // ---- Comercio y sucursales de otro ----

    @Test
    void noPuedeModificarLosDatosDeUnComercioAjeno() throws Exception {
        esProhibido(comoDuenioDeDonPietro(json(put("/api/v1/comercios/1"),
            "{\"razonSocial\":\"Hackeado\",\"direccion\":\"X\",\"rubro\":\"Y\"}")));

        mockMvc.perform(get("/api/v1/comercios/1"))
               .andExpect(jsonPath("$.razonSocial").value("La Esquina SRL"));
    }

    @Test
    void noPuedeAdministrarSucursalesAjenas() throws Exception {
        esProhibido(comoDuenioDeDonPietro(json(post("/api/v1/comercios/1/sucursales"), "{\"direccion\":\"Calle 1\"}")));
        esProhibido(comoDuenioDeDonPietro(json(put("/api/v1/comercios/1/sucursales/1"), "{\"direccion\":\"Calle 1\"}")));
        esProhibido(comoDuenioDeDonPietro(delete("/api/v1/comercios/1/sucursales/1")));

        mockMvc.perform(get("/api/v1/comercios/1/sucursales"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(3)));
    }

    // ---- Lo propio sí, y ADMIN puede todo ----

    @Test
    void puedeModificarSusPropiosProductos() throws Exception {
        comoDuenioDeDonPietro(json(patch("/api/v1/productos/7/precio"), "{\"nuevoPrecio\":3000}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.precio").value(3000));
    }

    @Test
    void unAdminPuedeModificarProductosDeCualquierComercio() throws Exception {
        como("prueba-admin", "ADMIN", json(patch("/api/v1/productos/3/precio"), "{\"nuevoPrecio\":8100}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.precio").value(8100));
    }

    // ---- API-15: mis comercios, y el alta deja al usuario como responsable ----

    @Test
    void listaSoloLosComerciosDelUsuario() throws Exception {
        comoDuenioDeDonPietro(get("/api/v1/comercios/mios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id").value(2))
            .andExpect(jsonPath("$[1].id").value(3));
    }

    @Test
    void misComerciosExigeToken() throws Exception {
        mockMvc.perform(get("/api/v1/comercios/mios"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void quienRegistraUnComercioEsSuResponsable() throws Exception {
        String nuevo = "{\"cuit\":\"30700000099\",\"razonSocial\":\"Nuevo SRL\",\"direccion\":\"X 1\",\"rubro\":\"Café\"}";
        String id = como("nuevo-comerciante", "COMERCIO", json(post("/api/v1/comercios"), nuevo))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString()
            .replaceAll(".*\"id\":(\\d+).*", "$1");

        como("nuevo-comerciante", "COMERCIO", get("/api/v1/comercios/mios"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].cuit").value("30700000099"));

        String cambio = "{\"razonSocial\":\"Nuevo Café SRL\",\"direccion\":\"X 2\",\"rubro\":\"Café\"}";
        como("nuevo-comerciante", "COMERCIO", json(put("/api/v1/comercios/" + id), cambio))
            .andExpect(status().isOk());
        esProhibido(como(DUENIO_LA_ESQUINA, "COMERCIO", json(put("/api/v1/comercios/" + id), cambio)));
    }
}
