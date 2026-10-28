package com.catalogodeliverygo.demo.Controller;

import com.catalogodeliverygo.demo.DTOs.CatalogoResponseDTO;
import com.catalogodeliverygo.demo.Service.CatalogoService;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CatalogoController implements CatalogoApi {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @Override
    public ResponseEntity<CatalogoResponseDTO> buscarCatalogo(String texto, List<Long> categoriaId, Long comercioId,
                                                              BigDecimal precioMin, BigDecimal precioMax,
                                                              int page, int size) {
        return ResponseEntity.ok(catalogoService.buscarCatalogo(texto, categoriaId, comercioId,
                                                                precioMin, precioMax, page, size));
    }
}
