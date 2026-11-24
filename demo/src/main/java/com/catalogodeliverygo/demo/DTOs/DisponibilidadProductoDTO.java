package com.catalogodeliverygo.demo.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

// DTO-08 / DM-22: resultado de verificar un producto para un pedido (INT-02). Pedidos lo consulta al crear
// y al confirmar el pedido, con los datos vigentes del catálogo
public class DisponibilidadProductoDTO {

    public static final String MOTIVO_DISPONIBLE = "DISPONIBLE";
    public static final String MOTIVO_NO_DISPONIBLE = "NO_DISPONIBLE";
    public static final String MOTIVO_PRODUCTO_INACTIVO = "PRODUCTO_INACTIVO";
    public static final String MOTIVO_COMERCIO_INACTIVO = "COMERCIO_INACTIVO";
    public static final String MOTIVO_INEXISTENTE = "INEXISTENTE";

    @Schema(description = "Id del producto consultado", example = "1")
    private Long productoId;

    @Schema(description = "Nombre vigente; null si el producto no existe", example = "Hamburguesa clásica")
    private String nombre;

    @Schema(description = "Precio vigente; null si el producto no existe. Pedidos debe usar este precio, no el que tenía guardado",
            example = "8500.00")
    private BigDecimal precio;

    @Schema(description = "Comercio del producto; null si el producto no existe", example = "1")
    private Long comercioId;

    @Schema(description = "true solo si el producto se puede pedir ahora (motivo DISPONIBLE)", example = "true")
    private boolean disponible;

    @Schema(description = "Por qué se puede o no se puede pedir",
            allowableValues = {MOTIVO_DISPONIBLE, MOTIVO_NO_DISPONIBLE, MOTIVO_PRODUCTO_INACTIVO,
                               MOTIVO_COMERCIO_INACTIVO, MOTIVO_INEXISTENTE},
            example = MOTIVO_DISPONIBLE)
    private String motivo;

    public DisponibilidadProductoDTO(Long productoId, String nombre, BigDecimal precio, Long comercioId,
                                     boolean disponible, String motivo) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.precio = precio;
        this.comercioId = comercioId;
        this.disponible = disponible;
        this.motivo = motivo;
    }

    public DisponibilidadProductoDTO() {
    }

    public Long getProductoId() {
        return productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public Long getComercioId() {
        return comercioId;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public void setComercioId(Long comercioId) {
        this.comercioId = comercioId;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
