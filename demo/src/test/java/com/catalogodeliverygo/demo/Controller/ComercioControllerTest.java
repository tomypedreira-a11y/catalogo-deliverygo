package com.catalogodeliverygo.demo.Controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

// API-14 / INT-03: GET /api/v1/comercios/{id} es público y devuelve también comercios INACTIVOS (DM-18).
// Comparte contexto y base con SeguridadProductosTest (mismas propiedades); los ids salen de data.sql
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:seguridad-test")
@AutoConfigureMockMvc
class ComercioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void devuelveUnComercioActivoSinToken() throws Exception {
        mockMvc.perform(get("/api/v1/comercios/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.cuit").value("30712345678"))
               .andExpect(jsonPath("$.razonSocial").exists())
               .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void devuelveTambienUnComercioInactivo() throws Exception {
        mockMvc.perform(get("/api/v1/comercios/4"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.cuit").value("30712345681"))
               .andExpect(jsonPath("$.estado").value("INACTIVO"));
    }

    @Test
    void unComercioInexistenteDa404() throws Exception {
        mockMvc.perform(get("/api/v1/comercios/999"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.status").value(404))
               .andExpect(jsonPath("$.error").value("NOT_FOUND"))
               .andExpect(jsonPath("$.path").value("/api/v1/comercios/999"));
    }

    @Test
    void unIdNoNumericoDa400() throws Exception {
        mockMvc.perform(get("/api/v1/comercios/abc"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }
}
