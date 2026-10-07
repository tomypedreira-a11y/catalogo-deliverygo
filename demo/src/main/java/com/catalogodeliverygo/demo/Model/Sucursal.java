package com.catalogodeliverygo.demo.Model;

import jakarta.persistence.*;

@Entity 
@Table (name = "sucursales")
public class Sucursal {
    @Id 
    @GeneratedValue (strategy=GenerationType.IDENTITY)
    private int id;

    @Column (nullable=false)
    private int comercioId;

    @Column (nullable=false)
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
