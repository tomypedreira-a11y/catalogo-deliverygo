package com.catalogodeliverygo.demo.DTOs;

import jakarta.validation.constraints.NotBlank;

public class CategoriaRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    public CategoriaRequestDTO(String nombre) {
        this.nombre = nombre;
    }

    public CategoriaRequestDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
