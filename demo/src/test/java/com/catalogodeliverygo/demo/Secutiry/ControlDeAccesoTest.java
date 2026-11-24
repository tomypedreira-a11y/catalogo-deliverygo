package com.catalogodeliverygo.demo.Secutiry;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.catalogodeliverygo.demo.Exception.AccesoNoAutorizadoException;
import com.catalogodeliverygo.demo.Model.Comercio;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

// DM-21: el rol habilita la operación, pero solo el responsable del comercio (o un ADMIN) la puede hacer
class ControlDeAccesoTest {

    private final ControlDeAcceso controlDeAcceso = new ControlDeAcceso();

    private final Comercio deLaEsquina = new Comercio("30712345678", "La Esquina SRL", "Av. Corrientes 1450",
                                                      "Hamburguesería", Comercio.ESTADO_ACTIVO, "prueba-comercio");

    // Mismo armado que JwtAuthenticationFilter: el principal es el "sub" y la autoridad ROLE_<rol>
    private void autenticar(String usuario, String rol) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void elResponsablePuedeAdministrarSuComercio() {
        autenticar("prueba-comercio", "COMERCIO");

        assertDoesNotThrow(() -> controlDeAcceso.verificarPuedeAdministrar(deLaEsquina));
    }

    @Test
    void otroComercioNoPuedeAdministrarlo() {
        autenticar("prueba-comercio-2", "COMERCIO");

        assertThrows(AccesoNoAutorizadoException.class, () -> controlDeAcceso.verificarPuedeAdministrar(deLaEsquina));
    }

    @Test
    void unAdminPuedeAdministrarCualquierComercio() {
        autenticar("prueba-admin", "ADMIN");

        assertDoesNotThrow(() -> controlDeAcceso.verificarPuedeAdministrar(deLaEsquina));
    }

    @Test
    void unComercioSinResponsableSoloLoAdministraUnAdmin() {
        Comercio sinResponsable = new Comercio("30700000000", "Sin dueño", "X", "Y", Comercio.ESTADO_ACTIVO);
        autenticar("prueba-comercio", "COMERCIO");

        assertThrows(AccesoNoAutorizadoException.class,
                     () -> controlDeAcceso.verificarPuedeAdministrar(sinResponsable));
    }

    @Test
    void sinAutenticacionNoPuedeAdministrar() {
        assertNull(controlDeAcceso.usuarioActual());
        assertFalse(controlDeAcceso.esAdmin());
        assertThrows(AccesoNoAutorizadoException.class, () -> controlDeAcceso.verificarPuedeAdministrar(deLaEsquina));
    }

    @Test
    void leeElUsuarioYElRolDelToken() {
        autenticar("prueba-admin", "ADMIN");

        assertEquals("prueba-admin", controlDeAcceso.usuarioActual());
        assertTrue(controlDeAcceso.esAdmin());
    }
}
