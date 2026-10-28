package com.catalogodeliverygo.demo.DTOs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductoResponseDTO {
    private Long id;
    private String nombre;
    private BigDecimal precio;
    private boolean disponible;
    private String estado;
    private Long comercioId;
    private List<CategoriaDTO> categorias = new ArrayList<>();

    public ProductoResponseDTO(Long id, String nombre, BigDecimal precio, boolean disponible, String estado, Long comercioId, List<CategoriaDTO> categorias) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.disponible = disponible;
        this.estado = estado;
        this.comercioId = comercioId;
        this.categorias = categorias;
    }

    public ProductoResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public String getEstado() {
        return estado;
    }

    public Long getComercioId() {
        return comercioId;
    }

    public List<CategoriaDTO> getCategorias() {
        return categorias;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setComercioId(Long comercioId) {
        this.comercioId = comercioId;
    }

    public void setCategorias(List<CategoriaDTO> categorias) {
        this.categorias = categorias;
    }
}
