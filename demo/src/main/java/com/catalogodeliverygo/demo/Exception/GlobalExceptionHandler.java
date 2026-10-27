package com.catalogodeliverygo.demo.Exception;

import com.catalogodeliverygo.demo.DTOs.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice 
public class GlobalExceptionHandler {
    
     @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request){
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(),
                                                      HttpStatus.NOT_FOUND.value(),
                                                      "NOT_FOUND",
                                                      ex.getMessage(),
                                                      request.getRequestURI()
                                                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarDuplicado(RecursoDuplicadoException ex, HttpServletRequest request){
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(),
                                                      HttpStatus.CONFLICT.value(),
                                                      "CONFLICT",
                                                      ex.getMessage(),
                                                      request.getRequestURI()
                                                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(ReglaDeNegocioException.class)
    public ResponseEntity<ErrorResponseDTO> manejarReglaDeNegocio(ReglaDeNegocioException ex, HttpServletRequest request){
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(),
                                                      HttpStatus.BAD_REQUEST.value(),
                                                      "BAD_REQUEST",
                                                      ex.getMessage(),
                                                      request.getRequestURI()
                                                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler (AccesoNoAutorizadoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarAccesoNoAutorizado(AccesoNoAutorizadoException ex, HttpServletRequest request){
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(),
                                                      HttpStatus.FORBIDDEN.value(),
                                                      "FORBIDDEN",
                                                      ex.getMessage(),
                                                      request.getRequestURI()
                                                );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    @ExceptionHandler (ComercioInactivoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarComercioInactivo(ComercioInactivoException ex, HttpServletRequest request){
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(),
                                                      HttpStatus.CONFLICT.value(),
                                                      "CONFLICT",
                                                      ex.getMessage(),
                                                      request.getRequestURI()
                                                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    // DM-08: los errores de Bean Validation (RN-03, RN-04, RN-06) también responden con ErrorResponseDTO
    @ExceptionHandler (MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest request){
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                           .map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
                           .collect(Collectors.joining("; "));

        ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(),
                                                      HttpStatus.BAD_REQUEST.value(),
                                                      "BAD_REQUEST",
                                                      mensaje,
                                                      request.getRequestURI()
                                                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler (HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> manejarJsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest request){
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(),
                                                      HttpStatus.BAD_REQUEST.value(),
                                                      "BAD_REQUEST",
                                                      "El cuerpo de la solicitud no es un JSON válido",
                                                      request.getRequestURI()
                                                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler (MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> manejarTipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest request){
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(),
                                                      HttpStatus.BAD_REQUEST.value(),
                                                      "BAD_REQUEST",
                                                      "El parámetro " + ex.getName() + " tiene un valor inválido",
                                                      request.getRequestURI()
                                                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }
}
