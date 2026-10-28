package com.catalogodeliverygo.demo.Secutiry;

import com.catalogodeliverygo.demo.DTOs.ErrorResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

// DM-15: 401 cuando el endpoint requiere token y no llegó uno válido (falta, firma inválida o vencido).
// Responde con ErrorResponseDTO, igual que GlobalExceptionHandler (DM-08)
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
    throws IOException {
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(),
                                                      HttpStatus.UNAUTHORIZED.value(),
                                                      "UNAUTHORIZED",
                                                      "Se requiere un token válido emitido por el módulo Usuarios",
                                                      request.getRequestURI()
                                                );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), error);
    }
}
