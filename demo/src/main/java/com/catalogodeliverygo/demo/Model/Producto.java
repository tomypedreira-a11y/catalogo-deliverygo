package com.catalogodeliverygo.demo.Model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue (strategy=GenerationType.IDENTITY)
    private Long id;

    @Column (nullable=false)
    private String nombre;

    @Column (nullable=false)
    private String descripcion;

    @Column (nullable=false, precision=12, scale=2)
    private BigDecimal precio;

    @Column (nullable=false)
    private boolean disponible;

    @ManyToOne
    @JoinColumn(name = "comercio_id", nullable = false)
    private Comercio comercio;

    // DM-03 / REL-03: un producto puede tener varias categorías y una categoría varios productos
    @ManyToMany
    @JoinTable(
        name = "producto_categoria",
        joinColumns = @JoinColumn(name = "producto_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private List<Categoria> categorias = new ArrayList<>();

    public Producto() {
    }

    public Producto(String nombre, String descripcion, BigDecimal precio, boolean disponible, Comercio comercio, List<Categoria> categorias) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.disponible = disponible;
        this.comercio = comercio;
        this.categorias = categorias;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public Comercio getComercio() {
        return comercio;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public void setComercio(Comercio comercio) {
        this.comercio = comercio;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }
}
