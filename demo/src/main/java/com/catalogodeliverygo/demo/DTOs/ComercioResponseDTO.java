package com.catalogodeliverygo.demo.DTOs;

public class ComercioResponseDTO {
    private Long id;
    private String cuit;
    private String razonSocial;
    private String estado;

    public ComercioResponseDTO(Long id, String cuit, String razonSocial, String estado) {
        this.id = id;
        this.cuit = cuit;
        this.razonSocial = razonSocial;
        this.estado = estado;
    }

    public ComercioResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public String getCuit() {
        return cuit;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getEstado() {
        return estado;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
