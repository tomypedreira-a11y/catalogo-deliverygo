package com.catalogodeliverygo.demo.Controller;

import com.catalogodeliverygo.demo.DTOs.ComercioResponseDTO;
import com.catalogodeliverygo.demo.DTOs.CrearComercioRequestDTO;
import com.catalogodeliverygo.demo.Service.ComercioService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ComercioController implements ComercioApi {

    private final ComercioService comercioService;

    public ComercioController(ComercioService comercioService) {
        this.comercioService = comercioService;
    }

    @Override
    public ResponseEntity<ComercioResponseDTO> registrarComercio(String token, CrearComercioRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(comercioService.registrarComercio(request, token));
    }
}
