package com.catalogodeliverygo.demo.Controller;

import com.catalogodeliverygo.demo.DTOs.SucursalDTO;
import com.catalogodeliverygo.demo.DTOs.SucursalRequestDTO;
import com.catalogodeliverygo.demo.Service.SucursalService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SucursalController implements SucursalApi {

    private final SucursalService sucursalService;

    public SucursalController(SucursalService sucursalService) {
        this.sucursalService = sucursalService;
    }

    @Override
    public ResponseEntity<List<SucursalDTO>> listarSucursales(Long comercioId) {
        return ResponseEntity.ok(sucursalService.listarSucursales(comercioId));
    }

    @Override
    public ResponseEntity<SucursalDTO> crearSucursal(Long comercioId, SucursalRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(sucursalService.crearSucursal(comercioId, request));
    }

    @Override
    public ResponseEntity<SucursalDTO> modificarSucursal(Long comercioId, Long id, SucursalRequestDTO request) {
        return ResponseEntity.ok(sucursalService.modificarSucursal(comercioId, id, request));
    }

    @Override
    public ResponseEntity<Void> eliminarSucursal(Long comercioId, Long id) {
        sucursalService.eliminarSucursal(comercioId, id);
        return ResponseEntity.noContent().build();
    }
}
