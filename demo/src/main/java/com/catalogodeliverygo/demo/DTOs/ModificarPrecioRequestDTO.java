package com.catalogodeliverygo.demo.DTOs;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ModificarPrecioRequestDTO {

    @NotNull(message = "El nuevo precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero") // RN-04
    private BigDecimal nuevoPrecio;

    public ModificarPrecioRequestDTO(BigDecimal nuevoPrecio) {
        this.nuevoPrecio = nuevoPrecio;
    }

    public ModificarPrecioRequestDTO() {
    }

    public BigDecimal getNuevoPrecio() {
        return nuevoPrecio;
    }

    public void setNuevoPrecio(BigDecimal nuevoPrecio) {
        this.nuevoPrecio = nuevoPrecio;
    }
}
