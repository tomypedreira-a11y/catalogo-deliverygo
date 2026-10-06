package com.catalogodeliverygo.demo.Model;

public class Sucursal {
    private int id;
    private int comercioId;
    private String direccion;

    public Sucursal(int id, int comercioId, String direccion) {
        this.id = id;
        this.comercioId = comercioId;
        this.direccion = direccion;
    }

    public int getId() {
        return id;
    }

    public int getComercioId() {
        return comercioId;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setComercioId(int comercioId) {
        this.comercioId = comercioId;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
