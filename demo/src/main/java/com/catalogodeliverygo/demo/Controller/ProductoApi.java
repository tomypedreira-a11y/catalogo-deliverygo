package com.catalogodeliverygo.demo.Controller;

import com.catalogodeliverygo.demo.DTOs.CambiarDisponibilidadRequestDTO;
import com.catalogodeliverygo.demo.DTOs.CambiarEstadoProductoRequestDTO;
import com.catalogodeliverygo.demo.DTOs.CrearProductoRequestDTO;
import com.catalogodeliverygo.demo.DTOs.ErrorResponseDTO;
import com.catalogodeliverygo.demo.DTOs.ModificarPrecioRequestDTO;
import com.catalogodeliverygo.demo.DTOs.ProductoResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

// DM-01: las tres operaciones devuelven ProductoResponseDTO
@Tag(name = "Productos", description = "Alta, consulta, precio, disponibilidad y estado de productos (CU-02, CU-03, CU-04, CU-06, CU-09)")
@RequestMapping("/api/v1/productos")
public interface ProductoApi {

    // API-02 / CU-02 / SRV-02
    @Operation(
        summary = "Publicar producto",
        description = "Da de alta un producto asociado a un comercio activo (RN-02) y a una o más categorías "
                    + "existentes (RN-03). El precio debe ser mayor a cero (RN-04). Se crea con estado ACTIVO y disponible = true.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Producto creado")
    @ApiResponse(responseCode = "400", description = "Dato faltante, precio <= 0, sin categorías o categoría inexistente (RN-03, RN-04, RN-06)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Falta el token o es inválido (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    // DM-09: findByIdAndEstado no distingue comercio inexistente de inactivo
    @ApiResponse(responseCode = "404", description = "Comercio inexistente o inactivo (RN-02)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PostMapping
    ResponseEntity<ProductoResponseDTO> publicarProducto(@Valid @RequestBody CrearProductoRequestDTO request);

    // API-03 / CU-03 / SRV-03
    @Operation(
        summary = "Modificar precio",
        description = "Actualiza el precio vigente de un producto. El comercio del producto debe estar activo (RN-02) "
                    + "y el nuevo precio debe ser mayor a cero (RN-04).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Precio actualizado")
    @ApiResponse(responseCode = "400", description = "Precio faltante o <= 0 (RN-04), o id no numérico",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Falta el token o es inválido (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Producto inexistente",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "El comercio del producto está inactivo (RN-02)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PatchMapping("/{id}/precio")
    ResponseEntity<ProductoResponseDTO> modificarPrecio(
        @Parameter(description = "Id del producto") @PathVariable Long id,
        @Valid @RequestBody ModificarPrecioRequestDTO request);

    // API-04 / CU-04 / SRV-04
    @Operation(
        summary = "Cambiar disponibilidad",
        description = "Habilita o deshabilita un producto. El comercio del producto debe estar activo (RN-02). "
                    + "Un producto no disponible deja de aparecer en el catálogo (RN-05).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada")
    // DM-10: disponible es Boolean obligatorio
    @ApiResponse(responseCode = "400", description = "Falta el campo disponible, o id no numérico",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Falta el token o es inválido (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Producto inexistente",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "El comercio del producto está inactivo (RN-02)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PatchMapping("/{id}/disponibilidad")
    ResponseEntity<ProductoResponseDTO> cambiarDisponibilidad(
        @Parameter(description = "Id del producto") @PathVariable Long id,
        @Valid @RequestBody CambiarDisponibilidadRequestDTO request);

    // API-06 / CU-06
    @Operation(
        summary = "Consultar detalle de producto",
        description = "Devuelve la ficha de un producto por id. Solo muestra productos activos y disponibles de "
                    + "comercios activos (RN-05). Endpoint público, sin token."
    )
    @ApiResponse(responseCode = "200", description = "Ficha del producto")
    @ApiResponse(responseCode = "400", description = "Id no numérico",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Producto inexistente, inactivo, no disponible o de un comercio inactivo (RN-05)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @GetMapping("/{id}")
    ResponseEntity<ProductoResponseDTO> obtenerProducto(
        @Parameter(description = "Id del producto") @PathVariable Long id);

    // API-12 / CU-09
    @Operation(
        summary = "Desactivar o reactivar producto",
        description = "Baja lógica (soft-delete) de un producto: activo = false pasa su estado a INACTIVO y lo saca "
                    + "del catálogo sin borrarlo; activo = true lo vuelve a ACTIVO. Es distinto de cambiar la "
                    + "disponibilidad (CU-04), que es una pausa temporal. El comercio del producto debe estar activo (RN-02).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Estado del producto actualizado")
    @ApiResponse(responseCode = "400", description = "Falta el campo activo, o id no numérico",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Falta el token o es inválido (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Producto inexistente",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "El comercio del producto está inactivo (RN-02)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PatchMapping("/{id}/estado")
    ResponseEntity<ProductoResponseDTO> cambiarEstado(
        @Parameter(description = "Id del producto") @PathVariable Long id,
        @Valid @RequestBody CambiarEstadoProductoRequestDTO request);
}
