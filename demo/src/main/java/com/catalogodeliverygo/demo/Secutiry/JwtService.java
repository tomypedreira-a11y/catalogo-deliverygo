package com.catalogodeliverygo.demo.Secutiry;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

  /*
   * IMPORTANTE:
   * Esta clave está hardcodeada únicamente con fines didácticos.
   *
   * En un ambiente real debería obtenerse desde:
   * - variable de entorno
   * - application.properties / application.yml
   * - Secret Manager / Vault
   */
  private static final String SECRET_KEY = "DeliveryGo-Clave-Secreta-JWT-2026-UADE-API";

 
  /**
   * Obtiene el username almacenado
   * en el campo "sub" del JWT.
   */
  public String obtenerUsername(String token) {
    return obtenerClaims(token).getSubject();
  }

  /**
   * Obtiene el rol almacenado
   * dentro del JWT.
   */
  public String obtenerRol(String token) {
    return obtenerClaims(token).get("role",String.class);
  }

  /**
   * Obtiene todos los Claims del JWT.
   *
   * Además verifica automáticamente
   * la firma y la expiración.
   */
  private Claims obtenerClaims(String token) {

    return Jwts.parser().verifyWith(obtenerClave())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
  }

  /**
   * Construye la clave criptográfica
   * utilizada para firmar y verificar
   * el JWT.
   */
  private SecretKey obtenerClave() {
    return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
  }
}