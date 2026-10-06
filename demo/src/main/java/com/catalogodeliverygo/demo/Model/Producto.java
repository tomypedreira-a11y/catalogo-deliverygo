package com.catalogodeliverygo.demo.Model;

public class Producto {
    private int id;
    private String nombre;
    private String descripcion;
    private double precio;
    private int comercioId;

    public Producto(int id, String nombre, String descripcion, double precio, int comercioId) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.comercioId = comercioId;
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

    public int getComercioId() {
        return comercioId;
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

    public void setComercioId(int comercioId) {
        this.comercioId = comercioId;
    }
}
