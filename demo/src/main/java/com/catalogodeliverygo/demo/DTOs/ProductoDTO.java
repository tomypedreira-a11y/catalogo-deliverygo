package com.catalogodeliverygo.demo.DTOs;

public class ProductoDTO {
    private int id;
    private String nombre;
    private String descripcion;
    private double precio;
    private String cuilComercio;

    public ProductoDTO(int id, String nombre, String descripcion, double precio, String cuilComercio) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.cuilComercio = cuilComercio;
    }

    public int getId() {
        return id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public double getPrecio() {
        return precio;
    }
    
    public String getCuilComercio() {
        return cuilComercio;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public void setPrecio(double precio) {
        this.precio = precio;
    }
    
    public void setCuilComercio(String cuilComercio) {
        this.cuilComercio = cuilComercio;
    }
}
