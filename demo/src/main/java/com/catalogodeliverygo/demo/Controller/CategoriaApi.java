package com.catalogodeliverygo.demo.Controller;

import com.catalogodeliverygo.demo.DTOs.CategoriaDTO;
import com.catalogodeliverygo.demo.DTOs.CategoriaRequestDTO;
import com.catalogodeliverygo.demo.DTOs.ErrorResponseDTO;

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

// DM-02: las categorías son un catálogo global, sin comercioId, administrado por el rol Administrador
@Tag(name = "Categorías", description = "ABM del catálogo global de categorías (CU-08)")
@RequestMapping("/api/v1/categorias")
public interface CategoriaApi {

    // API-10 / CU-08
    @Operation(
        summary = "Listar categorías",
        description = "Devuelve todas las categorías del catálogo global. Endpoint público, sin token: lo usa el "
                    + "formulario de alta de producto para elegir categorías (RN-03)."
    )
    @ApiResponse(responseCode = "200", description = "Lista de categorías")
    @GetMapping
    ResponseEntity<List<CategoriaDTO>> listarCategorias();

    // API-11 / CU-08
    @Operation(
        summary = "Crear categoría",
        description = "Agrega una categoría al catálogo global. Solo rol Administrador.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Categoría creada")
    @ApiResponse(responseCode = "400", description = "Nombre faltante (RN-06)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Falta el token o es inválido (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Usuario sin rol ADMIN (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PostMapping
    ResponseEntity<CategoriaDTO> crearCategoria(@Valid @RequestBody CategoriaRequestDTO request);

    // API-11 / CU-08
    @Operation(
        summary = "Modificar categoría",
        description = "Cambia el nombre de una categoría. Solo rol Administrador.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Categoría actualizada")
    @ApiResponse(responseCode = "400", description = "Nombre faltante (RN-06), o id no numérico",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Falta el token o es inválido (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Usuario sin rol ADMIN (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Categoría inexistente",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PutMapping("/{id}")
    ResponseEntity<CategoriaDTO> modificarCategoria(
        @Parameter(description = "Id de la categoría") @PathVariable Long id,
        @Valid @RequestBody CategoriaRequestDTO request);

    // API-11 / CU-08
    @Operation(
        summary = "Eliminar categoría",
        description = "Elimina una categoría del catálogo global. Solo rol Administrador. No se puede eliminar si "
                    + "tiene productos asociados, porque esos productos quedarían sin categoría (RN-03).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "204", description = "Categoría eliminada")
    @ApiResponse(responseCode = "401", description = "Falta el token o es inválido (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Usuario sin rol ADMIN (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Categoría inexistente",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "La categoría tiene productos asociados (RN-03)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @DeleteMapping("/{id}")
    ResponseEntity<Void> eliminarCategoria(
        @Parameter(description = "Id de la categoría") @PathVariable Long id);
}
