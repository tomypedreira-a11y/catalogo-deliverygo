package com.catalogodeliverygo.demo.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.catalogodeliverygo.demo.Model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    // CU-08 / DM-13: una categoría con productos asociados no se puede borrar (RN-03)
    public boolean existsByCategoriasId(Long categoriaId);

    // REP-04 / CU-05: búsqueda del catálogo. Siempre filtra por publicados (RN-05) y suma un filtro por cada
    // criterio que venga informado; los null o vacíos se ignoran (DM-07)
    default Page<Producto> findByCriterios(String texto, List<Long> categoriaIds, Long comercioId,
                                           BigDecimal precioMin, BigDecimal precioMax, Pageable pageable) {
        List<Specification<Producto>> filtros = new ArrayList<>();
        filtros.add(ProductoSpecifications.publicados());

        if (texto != null && !texto.isBlank()) {
            filtros.add(ProductoSpecifications.conTexto(texto));
        }
        if (categoriaIds != null && !categoriaIds.isEmpty()) {
            filtros.add(ProductoSpecifications.conAlgunaCategoria(categoriaIds));
        }
        if (comercioId != null) {
            filtros.add(ProductoSpecifications.delComercio(comercioId));
        }
        if (precioMin != null) {
            filtros.add(ProductoSpecifications.precioDesde(precioMin));
        }
        if (precioMax != null) {
            filtros.add(ProductoSpecifications.precioHasta(precioMax));
        }

        return findAll(Specification.allOf(filtros), pageable);
    }
}
