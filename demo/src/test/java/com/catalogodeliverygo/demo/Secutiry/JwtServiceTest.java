package com.catalogodeliverygo.demo.Secutiry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

// DM-17: la clave JWT viene de configuración y tiene que tener al menos 32 bytes
class JwtServiceTest {

    private static final String SECRETO = "DeliveryGo-Clave-Secreta-JWT-2026-UADE-API";

    // Token ADMIN de prueba de doc/CAMBIOS.md: tiene que seguir siendo válido con la clave por defecto
    private static final String TOKEN_ADMIN_DE_PRUEBA = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
        + "eyJzdWIiOiJwcnVlYmEtYWRtaW4iLCJyb2xlIjoiQURNSU4ifQ.jXvvRRZMakLjcEvXkDGB8wnlzHTW7Y5171IWoIOKuFg";

    @Test
    void leeSubYRoleDelTokenDePruebaDeLaDocumentacion() {
        JwtService jwtService = new JwtService(SECRETO);

        assertEquals("prueba-admin", jwtService.obtenerUsername(TOKEN_ADMIN_DE_PRUEBA));
        assertEquals("ADMIN", jwtService.obtenerRol(TOKEN_ADMIN_DE_PRUEBA));
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() {
        JwtService jwtService = new JwtService("otra-clave-distinta-de-al-menos-32-bytes!!");

        assertThrows(JwtException.class, () -> jwtService.obtenerUsername(TOKEN_ADMIN_DE_PRUEBA));
    }

    @Test
    void usaLaClaveRecibidaPorConfiguracion() {
        String otraClave = "clave-de-otro-entorno-con-mas-de-32-bytes";
        String token = Jwts.builder()
                           .subject("comercio-1")
                           .claim("role", "COMERCIO")
                           .signWith(Keys.hmacShaKeyFor(otraClave.getBytes(StandardCharsets.UTF_8)))
                           .compact();

        assertEquals("COMERCIO", new JwtService(otraClave).obtenerRol(token));
    }

    @Test
    void noArrancaConUnaClaveDeMenosDe32Bytes() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> new JwtService("corta"));

        assertTrue(ex.getMessage().contains("JWT_SECRET"));
    }
}
