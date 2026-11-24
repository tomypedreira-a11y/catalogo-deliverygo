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

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Comercios", description = "Alta, consulta y modificación de comercios (CU-01, CU-10, API-14)")
@RequestMapping("/api/v1/comercios")
public interface ComercioApi {

    // API-01 / CU-01 / SRV-01
    @Operation(
        summary = "Registrar comercio",
        description = "Da de alta un comercio validando que el CUIT no esté registrado (RN-01) y que no falten datos "
                    + "obligatorios (RN-06). Requiere el token JWT emitido por el módulo Usuarios (INT-01) con rol "
                    + "COMERCIO o ADMIN. El comercio se crea con estado ACTIVO. Si lo registra un COMERCIO, queda "
                    + "como su responsable (el sub del token) y es el único, junto con ADMIN, que puede modificarlo, "
                    + "publicar productos y administrar sus sucursales (DM-21). Un ADMIN puede indicar el "
                    + "responsable en usuarioResponsable.",
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

    // API-15 / DM-21: el Portal lo usa para mostrarle al comerciante los comercios que puede administrar
    @Operation(
        summary = "Listar mis comercios",
        description = "Devuelve los comercios cuyo responsable es el usuario del token (claim sub), activos o "
                    + "inactivos, ordenados por id. Si no tiene ninguno, la lista vacía.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Comercios del usuario autenticado")
    @ApiResponse(responseCode = "401", description = "Falta el token o es inválido (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Usuario sin rol COMERCIO o ADMIN (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @GetMapping("/mios")
    ResponseEntity<List<ComercioResponseDTO>> listarMisComercios();

    // API-14 / INT-03: lo usan otros módulos (Opiniones, Analítica) para validar el comercio
    @Operation(
        summary = "Consultar comercio",
        description = "Devuelve id, CUIT, razón social y estado de un comercio. A diferencia del catálogo, también "
                    + "devuelve comercios INACTIVOS, para que el consumidor pueda ver el estado (DM-18). "
                    + "Endpoint público, sin token."
    )
    @ApiResponse(responseCode = "200", description = "Datos del comercio, activo o inactivo")
    @ApiResponse(responseCode = "404", description = "Comercio inexistente",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @GetMapping("/{id}")
    ResponseEntity<ComercioResponseDTO> obtenerComercio(
        @Parameter(description = "Id del comercio") @PathVariable Long id);

    // API-13 / CU-10
    @Operation(
        summary = "Modificar información del comercio",
        description = "Edita razón social, dirección o rubro de un comercio. El CUIT no se puede modificar (RN-01), "
                    + "por eso no forma parte del cuerpo. Solo lo puede modificar su responsable o un ADMIN (DM-21).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Comercio actualizado")
    @ApiResponse(responseCode = "400", description = "Dato obligatorio faltante (RN-06), o id no numérico",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Falta el token o es inválido (DM-15)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Usuario sin rol COMERCIO o ADMIN (DM-15), o el comercio es de otro usuario (DM-21)",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Comercio inexistente",
                 content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PutMapping("/{id}")
    ResponseEntity<ComercioResponseDTO> modificarComercio(
        @Parameter(description = "Id del comercio") @PathVariable Long id,
        @Valid @RequestBody ModificarComercioRequestDTO request);
}
