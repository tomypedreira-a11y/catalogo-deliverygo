package com.catalogodeliverygo.demo.Model;

import java.util.List;

import jakarta.persistence.*;

@Entity 
@Table(name = "comercios")
public class Comercio {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    @Column(nullable=false, unique=true)
    private String cuit;

    @Column(nullable=false)
    private String razonSocial;

    @Column(nullable=false)
    private String direccion;

    @Column(nullable=false)
    private boolean activo;

    @OneToMany(mappedBy = "comercio", fetch = FetchType.LAZY)
    private List<Sucursal> sucursales;

    @OneToMany (mappedBy = "comercio", fetch = FetchType.LAZY)
    private List<Producto> productos;

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
