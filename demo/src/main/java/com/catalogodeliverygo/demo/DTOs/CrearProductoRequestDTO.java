package com.catalogodeliverygo.demo.DTOs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CrearProductoRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero") // RN-04
    private BigDecimal precio;

    @NotEmpty(message = "El producto debe tener al menos una categoría") // RN-03
    private List<@NotNull Long> categoriaIds = new ArrayList<>();

    @NotNull(message = "El comercioId es obligatorio")
    private Long comercioId;


    public CrearProductoRequestDTO(String nombre, String descripcion, BigDecimal precio, List<Long> categoriaIds, Long comercioId) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoriaIds = categoriaIds;
        this.comercioId = comercioId;
    }

    public CrearProductoRequestDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public Long getComercioId() {
        return comercioId;
    }

    public List<Long> getCategoriaIds() {
        return categoriaIds;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setComercioId(Long comercioId) {
        this.comercioId = comercioId;
    }

    public void setCategoriaIds(List<Long> categoriaIds) {
        this.categoriaIds = categoriaIds;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

}
