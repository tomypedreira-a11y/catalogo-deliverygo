package com.catalogodeliverygo.demo.Model;

import jakarta.persistence.*;

@Entity
@Table (name = "sucursales")
public class Sucursal {
    @Id
    @GeneratedValue (strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "comercio_id", nullable = false)
    private Comercio comercio;

    @Column (nullable=false)
    private String direccion;

    public Sucursal() {
    }

    public Sucursal(Comercio comercio, String direccion) {
        this.comercio = comercio;
        this.direccion = direccion;
    }

    public Long getId() {
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
