package com.catalogodeliverygo.demo.Controller;

import com.catalogodeliverygo.demo.DTOs.CambiarDisponibilidadRequestDTO;
import com.catalogodeliverygo.demo.DTOs.CrearProductoRequestDTO;
import com.catalogodeliverygo.demo.DTOs.ModificarPrecioRequestDTO;
import com.catalogodeliverygo.demo.DTOs.ProductoResponseDTO;
import com.catalogodeliverygo.demo.Service.ProductoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductoController implements ProductoApi {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Override
    public ResponseEntity<ProductoResponseDTO> publicarProducto(CrearProductoRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productoService.publicarProducto(request));
    }

    @Override
    public ResponseEntity<ProductoResponseDTO> modificarPrecio(Long id, ModificarPrecioRequestDTO request) {
        return ResponseEntity.ok(productoService.modificarPrecio(id, request.getNuevoPrecio()));
    }

    @Override
    public ResponseEntity<ProductoResponseDTO> cambiarDisponibilidad(Long id, CambiarDisponibilidadRequestDTO request) {
        return ResponseEntity.ok(productoService.cambiarDisponibilidad(id, request.getDisponible()));
    }
}
