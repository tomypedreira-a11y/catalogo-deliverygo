package com.catalogodeliverygo.demo.Secutiry;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

  // HS256 exige una clave de al menos 256 bits
  static final int LARGO_MINIMO_BYTES = 32;

  private final SecretKey clave;

  /*
   * DM-17: la clave se toma de la propiedad app.jwt.secret (variable de entorno JWT_SECRET).
   * Tiene que ser la misma con la que el módulo Usuarios firma los tokens (INT-01).
   * Si es más corta que 32 bytes la aplicación no arranca.
   */
  public JwtService(@Value("${app.jwt.secret}") String secreto) {
    if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < LARGO_MINIMO_BYTES) {
      throw new IllegalStateException("app.jwt.secret (JWT_SECRET) debe tener al menos " + LARGO_MINIMO_BYTES
                                      + " bytes para firmar tokens HS256");
    }
    this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
  }

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

    return Jwts.parser().verifyWith(clave)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
  }
}