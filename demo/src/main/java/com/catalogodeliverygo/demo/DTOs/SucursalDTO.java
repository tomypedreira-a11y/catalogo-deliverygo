package com.catalogodeliverygo.demo.DTOs;

public class SucursalDTO {
    private Long id;
    private String direccion;
    private Long comercioId;

    public SucursalDTO(Long id, String direccion, Long comercioId) {
        this.id = id;
        this.direccion = direccion;
        this.comercioId = comercioId;
    }

    public SucursalDTO() {
    }

    public Long getId() {
        return id;
    }

    public String getDireccion() {
        return direccion;
    }

    public Long getComercioId() {
        return comercioId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setComercioId(Long comercioId) {
        this.comercioId = comercioId;
    }
}
