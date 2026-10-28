package com.catalogodeliverygo.demo.Secutiry;

import com.catalogodeliverygo.demo.DTOs.ErrorResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

// DM-15: 403 cuando el token es válido pero el rol no alcanza para la ruta.
// Responde con ErrorResponseDTO, igual que GlobalExceptionHandler (DM-08)
@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JsonAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
    throws IOException {
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(),
                                                      HttpStatus.FORBIDDEN.value(),
                                                      "FORBIDDEN",
                                                      "El rol del usuario no permite esta operación",
                                                      request.getRequestURI()
                                                );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), error);
    }
}
