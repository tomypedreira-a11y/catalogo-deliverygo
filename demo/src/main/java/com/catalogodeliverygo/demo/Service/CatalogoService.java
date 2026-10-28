package com.catalogodeliverygo.demo.Service;

import com.catalogodeliverygo.demo.DTOs.CatalogoResponseDTO;
import com.catalogodeliverygo.demo.Exception.ReglaDeNegocioException;
import com.catalogodeliverygo.demo.Mapper.DtoMapper;
import com.catalogodeliverygo.demo.Model.Producto;
import com.catalogodeliverygo.demo.Repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CatalogoService {

    // DM-07: límites del tamaño de página
    private static final int SIZE_MINIMO = 1;
    private static final int SIZE_MAXIMO = 100;

    private final ProductoRepository productoRepository;
    private final DtoMapper mapper;

    public CatalogoService(ProductoRepository productoRepository, DtoMapper mapper) {
        this.productoRepository = productoRepository;
        this.mapper = mapper;
    }

    // SRV-05 / CU-05: valida criterios (DM-07) y busca solo productos ACTIVOS y disponibles de comercios activos
    // (RN-05, REP-04). Sin resultados no es un error: devuelve la página vacía
    @Transactional(readOnly = true)
    public CatalogoResponseDTO buscarCatalogo(String texto, List<Long> categoriaIds, Long comercioId,
                                              BigDecimal precioMin, BigDecimal precioMax, int page, int size) {
        validarCriterios(precioMin, precioMax, page, size);

        // Orden por nombre (y por id para desempatar) para que la paginación sea estable entre páginas
        PageRequest pagina = PageRequest.of(page, size, Sort.by("nombre", "id"));
        Page<Producto> resultado = productoRepository.findByCriterios(texto, categoriaIds, comercioId,
                                                                      precioMin, precioMax, pagina);

        return mapper.toCatalogoResponse(resultado);
    }

    // Flujo alternativo de CU-05: criterios inválidos -> 400
    private void validarCriterios(BigDecimal precioMin, BigDecimal precioMax, int page, int size) {
        if (precioMin != null && precioMin.signum() < 0) {
            throw new ReglaDeNegocioException("precioMin no puede ser negativo");
        }
        if (precioMax != null && precioMax.signum() < 0) {
            throw new ReglaDeNegocioException("precioMax no puede ser negativo");
        }
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            throw new ReglaDeNegocioException("precioMin no puede ser mayor que precioMax");
        }
        if (page < 0) {
            throw new ReglaDeNegocioException("page no puede ser negativo");
        }
        if (size < SIZE_MINIMO || size > SIZE_MAXIMO) {
            throw new ReglaDeNegocioException("size tiene que estar entre " + SIZE_MINIMO + " y " + SIZE_MAXIMO);
        }
    }
}
