package com.catalogodeliverygo.demo.DTOs;

import jakarta.validation.constraints.NotBlank;

// RN-01: el CUIT es inmutable, por eso no forma parte de este DTO
public class ModificarComercioRequestDTO {

    @NotBlank(message = "La razón social es obligatoria")
    private String razonSocial;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El rubro es obligatorio")
    private String rubro;

    public ModificarComercioRequestDTO(String razonSocial, String direccion, String rubro) {
        this.razonSocial = razonSocial;
        this.direccion = direccion;
        this.rubro = rubro;
    }

    public ModificarComercioRequestDTO() {
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

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setRubro(String rubro) {
        this.rubro = rubro;
    }
}
