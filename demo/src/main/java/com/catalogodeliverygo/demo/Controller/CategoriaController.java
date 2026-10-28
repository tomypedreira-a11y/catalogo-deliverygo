package com.catalogodeliverygo.demo.Controller;

import com.catalogodeliverygo.demo.DTOs.CategoriaDTO;
import com.catalogodeliverygo.demo.DTOs.CategoriaRequestDTO;
import com.catalogodeliverygo.demo.Service.CategoriaService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoriaController implements CategoriaApi {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @Override
    public ResponseEntity<List<CategoriaDTO>> listarCategorias() {
        return ResponseEntity.ok(categoriaService.listarCategorias());
    }

    @Override
    public ResponseEntity<CategoriaDTO> crearCategoria(CategoriaRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoriaService.crearCategoria(request));
    }

    @Override
    public ResponseEntity<CategoriaDTO> modificarCategoria(Long id, CategoriaRequestDTO request) {
        return ResponseEntity.ok(categoriaService.modificarCategoria(id, request));
    }

    @Override
    public ResponseEntity<Void> eliminarCategoria(Long id) {
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }
}
