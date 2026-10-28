package com.catalogodeliverygo.demo.Controller;

import com.catalogodeliverygo.demo.DTOs.ErrorResponseDTO;
import com.catalogodeliverygo.demo.DTOs.SucursalDTO;
import com.catalogodeliverygo.demo.DTOs.SucursalRequestDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

// REL-01: cada sucursal pertenece a un único comercio, por eso cuelga de su ruta
@Tag(name = "Sucursales", description = "ABM de sucursales de un comercio (CU-07)")
@RequestMapping("/api/v1/comercios/{comercioId}/sucursales")
public interface SucursalApi {

    // API-07 / CU-07
    @Operation(
        summary = "Listar sucursales del comercio",
        description = "Devuelve las sucursales de un comercio. Endpoint público, sin token."
    )
    @ApiResponse(responseCode = "200", description = "Lista de sucursales, puede estar vacía")
    @ApiResponse(responseCode = "404", description = "Comercio inexistente",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @GetMapping
    ResponseEntity<List<SucursalDTO>> listarSucursales(
        @Parameter(description = "Id del comercio") @PathVariable Long comercioId);

    // API-08 / CU-07
    @Operation(
        summary = "Crear sucursal",
        description = "Agrega una sucursal al comercio. El comercio debe estar activo (RN-02).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Sucursal creada")
    @ApiResponse(responseCode = "400", description = "Dirección faltante (RN-06)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Falta el token o es inválido (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Usuario sin rol COMERCIO o ADMIN (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Comercio inexistente",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "El comercio está inactivo (RN-02)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PostMapping
    ResponseEntity<SucursalDTO> crearSucursal(
        @Parameter(description = "Id del comercio") @PathVariable Long comercioId,
        @Valid @RequestBody SucursalRequestDTO request);

    // API-09 / CU-07
    @Operation(
        summary = "Modificar sucursal",
        description = "Cambia la dirección de una sucursal del comercio. El comercio debe estar activo (RN-02).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Sucursal actualizada")
    @ApiResponse(responseCode = "400", description = "Dirección faltante (RN-06)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Falta el token o es inválido (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Usuario sin rol COMERCIO o ADMIN (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Comercio o sucursal inexistente, o la sucursal no es de ese comercio",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "El comercio está inactivo (RN-02)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PutMapping("/{id}")
    ResponseEntity<SucursalDTO> modificarSucursal(
        @Parameter(description = "Id del comercio") @PathVariable Long comercioId,
        @Parameter(description = "Id de la sucursal") @PathVariable Long id,
        @Valid @RequestBody SucursalRequestDTO request);

    // API-09 / CU-07
    @Operation(
        summary = "Eliminar sucursal",
        description = "Elimina una sucursal del comercio. El comercio debe estar activo (RN-02).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "204", description = "Sucursal eliminada")
    @ApiResponse(responseCode = "401", description = "Falta el token o es inválido (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Usuario sin rol COMERCIO o ADMIN (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Comercio o sucursal inexistente, o la sucursal no es de ese comercio",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "El comercio está inactivo (RN-02)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @DeleteMapping("/{id}")
    ResponseEntity<Void> eliminarSucursal(
        @Parameter(description = "Id del comercio") @PathVariable Long comercioId,
        @Parameter(description = "Id de la sucursal") @PathVariable Long id);
}
