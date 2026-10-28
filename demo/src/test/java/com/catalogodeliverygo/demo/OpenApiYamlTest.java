package com.catalogodeliverygo.demo;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

// DM-11: mantiene docs/openapi.yml al día. Cada "./mvnw test" pide la especificación a springdoc y la escribe
// en el archivo, así no hay que exportarla a mano cuando cambia un endpoint. Si cambió, queda para commitear
// MockMvc arma un contexto de Spring distinto al de los otros tests. Con su propia base en memoria, el data.sql
// no se carga dos veces sobre la misma base (chocaría con el CUIT unique)
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:openapi-yaml-test")
@AutoConfigureMockMvc
class OpenApiYamlTest {

    // Maven corre los tests desde demo/, y docs/ está en la raíz del repositorio
    private static final Path ARCHIVO = Path.of("..", "docs", "openapi.yml");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void generaDocsOpenapiYml() throws Exception {
        String yaml = mockMvc.perform(get("/v3/api-docs.yaml"))
                             .andExpect(status().isOk())
                             .andReturn()
                             .getResponse()
                             .getContentAsString(StandardCharsets.UTF_8);

        // Control mínimo antes de pisar el archivo: están las rutas de los 10 CU
        for (String ruta : new String[] {"/api/v1/comercios", "/api/v1/productos", "/api/v1/catalogo",
                                         "/api/v1/categorias", "/sucursales", "/estado"}) {
            assertTrue(yaml.contains(ruta), "La especificación no tiene la ruta " + ruta);
        }

        Files.writeString(ARCHIVO, yaml, StandardCharsets.UTF_8);
    }
}
