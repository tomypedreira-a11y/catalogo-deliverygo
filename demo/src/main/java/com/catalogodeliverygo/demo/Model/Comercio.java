package com.catalogodeliverygo.demo.Model;

public class Comercio {
    private int id;
    private String cuit;
    private String razonSocial;
    private String direccion;
    private boolean activo;

    public Comercio(int id, String cuit, String razonSocial, String direccion, boolean activo) {
        this.id = id;
        this.cuit = cuit;
        this.razonSocial = razonSocial;
        this.direccion = direccion;
        this.activo = activo;
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

    public boolean isActivo() {
        return activo;
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

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
