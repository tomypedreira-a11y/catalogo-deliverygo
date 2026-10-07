package com.catalogodeliverygo.demo.Model;

import jakarta.persistence.*;

@Entity 
@Table(name = "productos")
public class Producto {

    @Id 
    @GeneratedValue (strategy=GenerationType.IDENTITY)
    private int id;

    @Column (nullable=false)
    private String nombre;

    @Column (nullable=false)
    private String descripcion;

    @Column (nullable=false)
    private double precio;

    @Column (nullable=false)
    private Comercio comercio;

    public Producto(int id, String nombre, String descripcion, double precio, Comercio comercio) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.comercio = comercio;
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

    public Comercio getComercio() {
        return comercio;
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

    public void setComercio(Comercio comercio) {
        this.comercio = comercio;
    }
}
