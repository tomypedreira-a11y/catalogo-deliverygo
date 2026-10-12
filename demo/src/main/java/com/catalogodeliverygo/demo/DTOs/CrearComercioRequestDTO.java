package com.catalogodeliverygo.demo.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CrearComercioRequestDTO {

    @NotBlank(message = "El CUIT es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "El CUIT debe tener 11 dígitos, sin guiones")
    private String cuit;

    @NotBlank(message = "La razón social es obligatoria")
    private String razonSocial;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El rubro es obligatorio")
    private String rubro;

    public CrearComercioRequestDTO(String cuit, String razonSocial, String direccion, String rubro) {
        this.cuit = cuit;
        this.razonSocial = razonSocial;
        this.direccion = direccion;
        this.rubro = rubro;
    }

    public CrearComercioRequestDTO() {
    }

    public String getRubro() {
        return rubro;
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

    public void setRubro(String rubro) {
        this.rubro = rubro;
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
}
