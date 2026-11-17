package com.catalogodeliverygo.demo.Controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

// Health check público: GET /api/v1/status responde 200 sin token.
// Base en memoria propia, igual que OpenApiYamlTest, para no cargar data.sql dos veces sobre la misma base
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:status-test")
@AutoConfigureMockMvc
class StatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void statusSinTokenDevuelve200ConOk() throws Exception {
        mockMvc.perform(get("/api/v1/status"))
               .andExpect(status().isOk())
               .andExpect(content().json("{\"status\":\"ok\"}", true));
    }
}
