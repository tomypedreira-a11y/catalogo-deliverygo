package com.catalogodeliverygo.demo.DTOs;

public class SucursalDTO {
    private int id;
    private String direccion;
    private String cuilComercio;

    public SucursalDTO(int id, String direccion, String cuilComercio) {
        this.id = id;
        this.direccion = direccion;
        this.cuilComercio = cuilComercio;
    }

    public int getId() {
        return id;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getCuilComercio() {
        return cuilComercio;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setCuilComercio(String cuilComercio) {
        this.cuilComercio = cuilComercio;
    }
}
