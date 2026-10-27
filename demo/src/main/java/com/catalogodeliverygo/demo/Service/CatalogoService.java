package com.catalogodeliverygo.demo.Service;

import com.catalogodeliverygo.demo.DTOs.CatalogoResponseDTO;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CatalogoService {

    // SRV-05 / CU-05: valida criterios (DM-07) y busca solo productos disponibles de comercios activos (RN-05, REP-04)
    public CatalogoResponseDTO buscarCatalogo(String texto, List<Long> categoriaIds, Long comercioId,
                                              BigDecimal precioMin, BigDecimal precioMax, int page, int size) {
        throw new UnsupportedOperationException("SRV-05 buscarCatalogo pendiente de implementación");
    }
}
