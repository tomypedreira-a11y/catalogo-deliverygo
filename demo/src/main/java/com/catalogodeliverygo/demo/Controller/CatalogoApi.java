package com.catalogodeliverygo.demo.Controller;

import com.catalogodeliverygo.demo.DTOs.CatalogoResponseDTO;
import com.catalogodeliverygo.demo.DTOs.ErrorResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Catálogo", description = "Búsqueda pública del catálogo (CU-05), consumida por Portal Web y Pedidos (INT-03)")
@RequestMapping("/api/v1/catalogo")
public interface CatalogoApi {

    // API-05 / CU-05 / SRV-05 / DM-07
    @Operation(
        summary = "Buscar catálogo",
        description = "Devuelve una página de productos activos y disponibles de comercios activos (RN-05) que cumplen los "
                    + "criterios. Todos los filtros son opcionales. Sin resultados responde 200 con la lista vacía."
    )
    @ApiResponse(responseCode = "200", description = "Página de resultados, puede estar vacía")
    @ApiResponse(responseCode = "400", description = "Criterios inválidos: precioMin > precioMax, valores negativos, "
                                                   + "page < 0, size fuera de 1 a 100 o parámetro con tipo incorrecto",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @GetMapping
    ResponseEntity<CatalogoResponseDTO> buscarCatalogo(
        @Parameter(description = "Busca en nombre y descripción del producto y en la razón social del comercio")
        @RequestParam(required = false) String texto,
        @Parameter(description = "Una o más categorías: ?categoriaId=1&categoriaId=3")
        @RequestParam(required = false) List<Long> categoriaId,
        @Parameter(description = "Solo productos de este comercio")
        @RequestParam(required = false) Long comercioId,
        @Parameter(description = "Precio mínimo, mayor o igual a 0")
        @RequestParam(required = false) BigDecimal precioMin,
        @Parameter(description = "Precio máximo, mayor o igual a precioMin")
        @RequestParam(required = false) BigDecimal precioMax,
        @Parameter(description = "Número de página, desde 0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Tamaño de página, entre 1 y 100")
        @RequestParam(defaultValue = "20") int size);
}
