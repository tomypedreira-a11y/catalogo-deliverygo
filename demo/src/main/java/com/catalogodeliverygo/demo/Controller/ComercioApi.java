package com.catalogodeliverygo.demo.Controller;

import com.catalogodeliverygo.demo.DTOs.ComercioResponseDTO;
import com.catalogodeliverygo.demo.DTOs.CrearComercioRequestDTO;
import com.catalogodeliverygo.demo.DTOs.ErrorResponseDTO;
import com.catalogodeliverygo.demo.DTOs.ModificarComercioRequestDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Comercios", description = "Alta y modificación de comercios (CU-01, CU-10)")
@RequestMapping("/api/v1/comercios")
public interface ComercioApi {

    // API-01 / CU-01 / SRV-01
    @Operation(
        summary = "Registrar comercio",
        description = "Da de alta un comercio validando que el CUIT no esté registrado (RN-01) y que no falten datos "
                    + "obligatorios (RN-06). Requiere el token JWT emitido por el módulo Usuarios (INT-01) con rol "
                    + "COMERCIO o ADMIN. El comercio se crea con estado ACTIVO.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Comercio creado con estado ACTIVO")
    @ApiResponse(responseCode = "400", description = "Dato obligatorio faltante o CUIT con formato inválido (RN-06)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Falta el token o es inválido (INT-01, DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Usuario sin rol COMERCIO o ADMIN (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "Ya existe un comercio con ese CUIT (RN-01)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PostMapping
    // El token no es parámetro: lo valida SecurityConfig antes de llegar al controlador (DM-06)
    ResponseEntity<ComercioResponseDTO> registrarComercio(@Valid @RequestBody CrearComercioRequestDTO request);

    // API-13 / CU-10
    @Operation(
        summary = "Modificar información del comercio",
        description = "Edita razón social, dirección o rubro de un comercio. El CUIT no se puede modificar (RN-01), "
                    + "por eso no forma parte del cuerpo.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Comercio actualizado")
    @ApiResponse(responseCode = "400", description = "Dato obligatorio faltante (RN-06), o id no numérico",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Falta el token o es inválido (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Usuario sin rol COMERCIO o ADMIN (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Comercio inexistente",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PutMapping("/{id}")
    ResponseEntity<ComercioResponseDTO> modificarComercio(
        @Parameter(description = "Id del comercio") @PathVariable Long id,
        @Valid @RequestBody ModificarComercioRequestDTO request);
}
