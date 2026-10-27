package com.catalogodeliverygo.demo.Controller;

import com.catalogodeliverygo.demo.DTOs.ComercioResponseDTO;
import com.catalogodeliverygo.demo.DTOs.CrearComercioRequestDTO;
import com.catalogodeliverygo.demo.DTOs.ErrorResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Comercios", description = "Alta de comercios (CU-01)")
@RequestMapping("/api/v1/comercios")
public interface ComercioApi {

    // API-01 / CU-01 / SRV-01
    @Operation(
        summary = "Registrar comercio",
        description = "Da de alta un comercio validando que el CUIT no esté registrado (RN-01) y que no falten datos "
                    + "obligatorios (RN-06). Requiere el token del comerciante: Catálogo valida identidad y rol "
                    + "Comerciante contra el módulo Usuarios (INT-01). El comercio se crea con estado ACTIVO.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Comercio creado con estado ACTIVO")
    @ApiResponse(responseCode = "400", description = "Dato obligatorio faltante o CUIT con formato inválido (RN-06)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Token inválido o usuario sin rol Comerciante (INT-01)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "Ya existe un comercio con ese CUIT (RN-01)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PostMapping
    ResponseEntity<ComercioResponseDTO> registrarComercio(
        // Opcional para que el Service responda 403 (y no Spring un 400) cuando falta el token
        @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String token,
        @Valid @RequestBody CrearComercioRequestDTO request);
}
