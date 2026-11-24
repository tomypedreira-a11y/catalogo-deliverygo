package com.catalogodeliverygo.demo.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "comercios")
public class Comercio {

    // DM-04: estado es un String controlado, no un boolean ni un enum
    public static final String ESTADO_ACTIVO = "ACTIVO";
    public static final String ESTADO_INACTIVO = "INACTIVO";

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    // RN-01: el CUIT se fija en el alta y no cambia; por eso no tiene setter
    @Column(nullable=false, unique=true)
    private String cuit;

    @Column(nullable=false)
    private String razonSocial;

    @Column(nullable=false)
    private String direccion;

    @Column(nullable=false)
    private String rubro;

    @Column(nullable=false)
    private String estado;

    // DM-21: "sub" del token del usuario dueño del comercio. Solo él (o un ADMIN) puede modificar el comercio,
    // sus productos y sus sucursales. Puede ser null si lo dio de alta un ADMIN sin indicar responsable
    @Column(name = "usuario_responsable")
    private String usuarioResponsable;

    @OneToMany(mappedBy = "comercio", fetch = FetchType.LAZY)
    private List<Sucursal> sucursales = new ArrayList<>();

    @OneToMany (mappedBy = "comercio", fetch = FetchType.LAZY)
    private List<Producto> productos = new ArrayList<>();

    public Comercio() {
    }

    public Comercio(String cuit, String razonSocial, String direccion, String rubro, String estado) {
        this.cuit = cuit;
        this.razonSocial = razonSocial;
        this.direccion = direccion;
        this.rubro = rubro;
        this.estado = estado;
    }

    public Comercio(String cuit, String razonSocial, String direccion, String rubro, String estado,
                    String usuarioResponsable) {
        this(cuit, razonSocial, direccion, rubro, estado);
        this.usuarioResponsable = usuarioResponsable;
    }

    public Long getId() {
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

    public String getRubro() {
        return rubro;
    }

    public String getEstado() {
        return estado;
    }

    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }

    public List<Sucursal> getSucursales() {
        return sucursales;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setRubro(String rubro) {
        this.rubro = rubro;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setUsuarioResponsable(String usuarioResponsable) {
        this.usuarioResponsable = usuarioResponsable;
    }
}
