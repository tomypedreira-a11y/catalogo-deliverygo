package com.catalogodeliverygo.demo.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

// DTO-09 / DM-22: respuesta de API-16. Un ítem por producto pedido, en el mismo orden y sin repetidos
public class VerificacionDisponibilidadResponseDTO {

    @Schema(description = "true si todos los productos se pueden pedir. Si es false, Pedidos no debe crear ni confirmar el pedido",
            example = "true")
    private boolean todosDisponibles;

    private List<DisponibilidadProductoDTO> productos = new ArrayList<>();

    public VerificacionDisponibilidadResponseDTO(boolean todosDisponibles, List<DisponibilidadProductoDTO> productos) {
        this.todosDisponibles = todosDisponibles;
        this.productos = productos;
    }

    public VerificacionDisponibilidadResponseDTO() {
    }

    public boolean isTodosDisponibles() {
        return todosDisponibles;
    }

    public List<DisponibilidadProductoDTO> getProductos() {
        return productos;
    }

    public void setTodosDisponibles(boolean todosDisponibles) {
        this.todosDisponibles = todosDisponibles;
    }

    public void setProductos(List<DisponibilidadProductoDTO> productos) {
        this.productos = productos;
    }
}
