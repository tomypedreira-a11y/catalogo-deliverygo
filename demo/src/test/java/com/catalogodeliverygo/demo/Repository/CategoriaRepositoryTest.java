package com.catalogodeliverygo.demo.Repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.catalogodeliverygo.demo.Model.Categoria;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

// DM-20: las consultas de unicidad de nombre, contra las categorías de data.sql en H2
@SpringBootTest
@Transactional
class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void encuentraElNombreSinDistinguirMayusculas() {
        assertTrue(categoriaRepository.existsByNombreIgnoreCase("bEBIDAS"));
        assertFalse(categoriaRepository.existsByNombreIgnoreCase("Wraps"));
    }

    @Test
    void alModificarNoCuentaLaPropiaCategoria() {
        Categoria bebidas = categoriaRepository.findAll(Sort.by("nombre")).stream()
                                               .filter(c -> "Bebidas".equals(c.getNombre()))
                                               .findFirst()
                                               .orElseThrow();

        assertFalse(categoriaRepository.existsByNombreIgnoreCaseAndIdNot("BEBIDAS", bebidas.getId()));
        assertTrue(categoriaRepository.existsByNombreIgnoreCaseAndIdNot("Postres", bebidas.getId()));
    }
}
