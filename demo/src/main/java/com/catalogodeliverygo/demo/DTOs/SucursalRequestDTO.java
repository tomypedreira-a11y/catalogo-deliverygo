package com.catalogodeliverygo.demo.DTOs;

import jakarta.validation.constraints.NotBlank;

public class SucursalRequestDTO {

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    public SucursalRequestDTO(String direccion) {
        this.direccion = direccion;
    }

    public SucursalRequestDTO() {
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
