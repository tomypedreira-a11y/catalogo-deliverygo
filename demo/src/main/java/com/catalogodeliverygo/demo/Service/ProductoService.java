package com.catalogodeliverygo.demo.Service;

import com.catalogodeliverygo.demo.DTOs.CrearProductoRequestDTO;
import com.catalogodeliverygo.demo.DTOs.ProductoResponseDTO;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public class ProductoService {

    // SRV-02 / CU-02: valida comercio activo (RN-02, REP-02), categorías existentes (RN-03, REP-06) y persiste (REP-05)
    public ProductoResponseDTO publicarProducto(CrearProductoRequestDTO request) {
        throw new UnsupportedOperationException("SRV-02 publicarProducto pendiente de implementación");
    }

    // SRV-03 / CU-03: busca el producto (REP-03), valida comercio activo (RN-02) y precio > 0 (RN-04)
    public ProductoResponseDTO modificarPrecio(Long productoId, BigDecimal nuevoPrecio) {
        throw new UnsupportedOperationException("SRV-03 modificarPrecio pendiente de implementación");
    }

    // SRV-04 / CU-04: busca el producto (REP-03), valida comercio activo (RN-02) y actualiza el flag
    public ProductoResponseDTO cambiarDisponibilidad(Long productoId, Boolean nuevoEstado) {
        throw new UnsupportedOperationException("SRV-04 cambiarDisponibilidad pendiente de implementación");
    }
}
