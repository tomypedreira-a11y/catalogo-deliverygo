package com.catalogodeliverygo.demo.DTOs;

import jakarta.validation.constraints.NotNull;

public class CambiarEstadoProductoRequestDTO {

    // Boolean (no boolean) para que un body sin el campo falle en vez de quedar en false
    @NotNull(message = "El campo activo es obligatorio")
    private Boolean activo;

    public CambiarEstadoProductoRequestDTO(Boolean activo) {
        this.activo = activo;
    }

    public CambiarEstadoProductoRequestDTO() {
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
