package com.catalogodeliverygo.demo.DTOs;

public class ComercioDTO {
    private int id;
    private String cuit;
    private String razonSocial;
    private String direccion;

    public ComercioDTO(int id, String cuit, String razonSocial, String direccion) {
        this.id = id;
        this.cuit = cuit;
        this.razonSocial = razonSocial;
        this.direccion = direccion;
    }

    public int getId() {
        return id;
    }

    public String getCuit() {
        return cuit;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
