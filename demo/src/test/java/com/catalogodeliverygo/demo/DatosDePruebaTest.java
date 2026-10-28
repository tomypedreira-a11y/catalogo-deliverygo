package com.catalogodeliverygo.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.catalogodeliverygo.demo.Model.Producto;
import com.catalogodeliverygo.demo.Repository.CategoriaRepository;
import com.catalogodeliverygo.demo.Repository.ComercioRepository;
import com.catalogodeliverygo.demo.Repository.ProductoRepository;
import com.catalogodeliverygo.demo.Repository.SucursalRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

// Verifica que data.sql cargue completo y respete RN-03
@SpringBootTest
@Transactional
class DatosDePruebaTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ComercioRepository comercioRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Test
    void cargaTodosLosDatosDePrueba() {
        assertEquals(6, categoriaRepository.count());
        assertEquals(4, comercioRepository.count());
        assertEquals(4, sucursalRepository.count());
        assertEquals(13, productoRepository.count());
    }

    @Test
    void cadaProductoTieneAlMenosUnaCategoria() {
        for (Producto producto : productoRepository.findAll()) {
            assertFalse(producto.getCategorias().isEmpty(), producto.getNombre() + " no tiene categorías (RN-03)");
        }
    }

    @Test
    void incluyeUnComercioInactivoParaProbarRN02() {
        assertEquals(1, comercioRepository.findAll().stream()
                                         .filter(c -> "INACTIVO".equals(c.getEstado()))
                                         .count());
    }
}
