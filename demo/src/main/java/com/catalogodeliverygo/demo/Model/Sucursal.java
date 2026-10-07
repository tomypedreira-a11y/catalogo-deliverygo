package com.catalogodeliverygo.demo.Model;

import jakarta.persistence.*;

@Entity 
@Table (name = "sucursales")
public class Sucursal {
    @Id 
    @GeneratedValue (strategy=GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "comercio_id")
    private Comercio comercio;

    @Column (nullable=false)
    private String direccion;

    public Sucursal(int id, Comercio comercio, String direccion) {
        this.id = id;
        this.comercio = comercio;
        this.direccion = direccion;
    }

    public int getId() {
        return id;
    }

    public Comercio getComercio() {
        return comercio;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setComercio(Comercio comercio) {
        this.comercio = comercio;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
