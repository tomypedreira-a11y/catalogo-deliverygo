package com.catalogodeliverygo.demo.DTOs;

import jakarta.validation.constraints.NotNull;

public class CambiarDisponibilidadRequestDTO {

    // Boolean (no boolean) para que un body sin el campo falle en vez de quedar en false
    @NotNull(message = "El campo disponible es obligatorio")
    private Boolean disponible;

    public CambiarDisponibilidadRequestDTO(Boolean disponible) {
        this.disponible = disponible;
    }

    public CambiarDisponibilidadRequestDTO() {
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }
}
