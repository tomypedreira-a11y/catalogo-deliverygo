package com.catalogodeliverygo.demo.Integracion;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/*
 * DM-22 / INT-02: qué pasa con un pedido cuando un producto deja de estar disponible.
 * El test hace de Pedidos (verifica con API-16 al crear y al confirmar el pedido) y de Portal (catálogo y
 * detalle), con los datos de data.sql. El comerciante es prueba-comercio, dueño de La Esquina (productos 1 a 6).
 * Base propia, porque cambia la disponibilidad y el precio de productos.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:flujo-pedido-test")
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class FlujoPedidoDisponibilidadTest {

    private static final String VERIFICAR = "/api/v1/productos/disponibilidad";

    @Autowired
    private MockMvc mockMvc;

    @Value("${app.jwt.secret}")
    private String secreto;

    private String tokenDelComerciante() {
        return "Bearer " + Jwts.builder()
                               .subject("prueba-comercio")
                               .claim("role", "COMERCIO")
                               .signWith(Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8)))
                               .compact();
    }

    // Lo que hace Pedidos: GET /api/v1/productos/disponibilidad?ids=...
    private ResultActions pedidosVerifica(String ids) throws Exception {
        // Los ids van en la URL, como los manda Pedidos: con .param() MockMvc no arma el query string
        return mockMvc.perform(get(VERIFICAR + "?ids=" + ids).header("X-Modulo", "pedidos"));
    }

    private void elComercianteCambiaLaDisponibilidad(long productoId, boolean disponible) throws Exception {
        mockMvc.perform(patch("/api/v1/productos/" + productoId + "/disponibilidad")
                            .header("Authorization", tokenDelComerciante())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"disponible\":" + disponible + "}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.disponible").value(disponible));
    }

    @Test
    void unProductoPausadoEntreLaCreacionYLaConfirmacionFrenaElPedido() throws Exception {
        // 1. Pedidos crea el pedido con Hamburguesa clásica (1) y Doble cheddar (2): todo disponible
        pedidosVerifica("1,2")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.todosDisponibles").value(true))
            .andExpect(jsonPath("$.productos", hasSize(2)))
            .andExpect(jsonPath("$.productos[1].nombre").value("Doble cheddar"))
            .andExpect(jsonPath("$.productos[1].precio").value(10500.00));

        // 2. Antes de confirmar, el comercio pausa Doble cheddar (CU-04)
        elComercianteCambiaLaDisponibilidad(2, false);

        // 3. Pedidos vuelve a verificar al confirmar: el pedido ya no se puede confirmar
        pedidosVerifica("1,2")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.todosDisponibles").value(false))
            .andExpect(jsonPath("$.productos[0].disponible").value(true))
            .andExpect(jsonPath("$.productos[1].disponible").value(false))
            .andExpect(jsonPath("$.productos[1].motivo").value("NO_DISPONIBLE"));

        // 4. En el Portal desaparece del catálogo y del detalle (RN-05)
        mockMvc.perform(get("/api/v1/catalogo").param("comercioId", "1").header("X-Modulo", "portal"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.items[?(@.id == 2)]", hasSize(0)))
               .andExpect(jsonPath("$.items[?(@.id == 1)]", hasSize(1)));
        mockMvc.perform(get("/api/v1/productos/2").header("X-Modulo", "portal"))
               .andExpect(status().isNotFound());

        // 5. El comercio lo vuelve a habilitar: el pedido se puede confirmar otra vez
        elComercianteCambiaLaDisponibilidad(2, true);
        pedidosVerifica("1,2")
            .andExpect(jsonPath("$.todosDisponibles").value(true));
    }

    // Pedidos tiene que cobrar el precio vigente, no el que vio el cliente al armar el carrito
    @Test
    void laVerificacionDevuelveElPrecioVigente() throws Exception {
        mockMvc.perform(patch("/api/v1/productos/4/precio")
                            .header("Authorization", tokenDelComerciante())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nuevoPrecio\":3500}"))
               .andExpect(status().isOk());

        pedidosVerifica("4")
            .andExpect(jsonPath("$.todosDisponibles").value(true))
            .andExpect(jsonPath("$.productos[0].precio").value(3500));
    }

    // Brownie (6) no disponible, Fainá (10) INACTIVO, Roll California (13) de un comercio INACTIVO, 999 no existe
    @Test
    void informaElMotivoDeCadaProductoQueNoSePuedePedir() throws Exception {
        pedidosVerifica("6,10,13,999")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.todosDisponibles").value(false))
            .andExpect(jsonPath("$.productos[0].motivo").value("NO_DISPONIBLE"))
            .andExpect(jsonPath("$.productos[1].motivo").value("PRODUCTO_INACTIVO"))
            .andExpect(jsonPath("$.productos[2].motivo").value("COMERCIO_INACTIVO"))
            .andExpect(jsonPath("$.productos[3].motivo").value("INEXISTENTE"))
            .andExpect(jsonPath("$.productos[3].nombre").doesNotExist());
    }

    @Test
    void esPublicoYNoExigeToken() throws Exception {
        mockMvc.perform(get(VERIFICAR).param("ids", "1"))
               .andExpect(status().isOk());
    }

    @Test
    void sinIdsOConIdsInvalidosDa400() throws Exception {
        mockMvc.perform(get(VERIFICAR))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value(containsString("ids es obligatorio")));
        mockMvc.perform(get(VERIFICAR).param("ids", "1,abc"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    // DM-23: cada consulta deja una línea en la consola con el módulo que llamó; es la evidencia de integración
    @Test
    void cadaConsultaQuedaRegistradaConElModuloQueLlamo(CapturedOutput salida) throws Exception {
        pedidosVerifica("1");

        assertTrue(salida.getOut().contains("GET /api/v1/productos/disponibilidad?ids=1 -> 200"),
                   "No se registró la consulta de Pedidos");
        assertTrue(salida.getOut().contains("modulo=pedidos"), "No se registró el módulo que llamó");
    }
}
