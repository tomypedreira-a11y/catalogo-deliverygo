package com.catalogodeliverygo.demo.Repository;

import com.catalogodeliverygo.demo.Model.Categoria;
import com.catalogodeliverygo.demo.Model.Comercio;
import com.catalogodeliverygo.demo.Model.Producto;

import jakarta.persistence.criteria.Join;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

// REP-04 / CU-05: un filtro por criterio de búsqueda del catálogo (DM-07).
// Cada uno es una condición del WHERE; ProductoRepository.findByCriterios combina las que correspondan
public final class ProductoSpecifications {

    private ProductoSpecifications() {
    }

    // RN-05: siempre aplica. Solo productos ACTIVOS y disponibles de comercios ACTIVOS (DM-12)
    public static Specification<Producto> publicados() {
        return (producto, query, cb) -> cb.and(
            cb.equal(producto.get("estado"), Producto.ESTADO_ACTIVO),
            cb.isTrue(producto.get("disponible")),
            cb.equal(producto.get("comercio").get("estado"), Comercio.ESTADO_ACTIVO));
    }

    // Busca en nombre y descripción del producto y en la razón social del comercio, sin distinguir mayúsculas.
    // La razón social entra porque CU-05 incluye consultar comercios (DM-07)
    public static Specification<Producto> conTexto(String texto) {
        String patron = "%" + texto.trim().toLowerCase() + "%";
        return (producto, query, cb) -> cb.or(
            cb.like(cb.lower(producto.get("nombre")), patron),
            cb.like(cb.lower(producto.get("descripcion")), patron),
            cb.like(cb.lower(producto.get("comercio").get("razonSocial")), patron));
    }

    // Productos con al menos una de las categorías. El join puede repetir un producto que tenga varias de
    // ellas, por eso la consulta va con distinct
    public static Specification<Producto> conAlgunaCategoria(List<Long> categoriaIds) {
        return (producto, query, cb) -> {
            query.distinct(true);
            Join<Producto, Categoria> categoria = producto.join("categorias");
            return categoria.get("id").in(categoriaIds);
        };
    }

    public static Specification<Producto> delComercio(Long comercioId) {
        return (producto, query, cb) -> cb.equal(producto.get("comercio").get("id"), comercioId);
    }

    public static Specification<Producto> precioDesde(BigDecimal precioMin) {
        return (producto, query, cb) -> cb.greaterThanOrEqualTo(producto.get("precio"), precioMin);
    }

    public static Specification<Producto> precioHasta(BigDecimal precioMax) {
        return (producto, query, cb) -> cb.lessThanOrEqualTo(producto.get("precio"), precioMax);
    }
}
