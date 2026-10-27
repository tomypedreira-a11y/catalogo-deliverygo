package com.catalogodeliverygo.demo.Service;

import com.catalogodeliverygo.demo.DTOs.ComercioResponseDTO;
import com.catalogodeliverygo.demo.DTOs.CrearComercioRequestDTO;

import org.springframework.stereotype.Service;

@Service
public class ComercioService {

    // SRV-01 / CU-01: valida rol vía Usuarios (INT-01), unicidad de CUIT (RN-01, REP-01) y persiste con estado ACTIVO
    public ComercioResponseDTO registrarComercio(CrearComercioRequestDTO request, String token) {
        throw new UnsupportedOperationException("SRV-01 registrarComercio pendiente de implementación");
    }
}
