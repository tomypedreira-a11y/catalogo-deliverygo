package com.catalogodeliverygo.demo.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.catalogodeliverygo.demo.DTOs.CategoriaDTO;
import com.catalogodeliverygo.demo.DTOs.CategoriaRequestDTO;
import com.catalogodeliverygo.demo.Exception.CategoriaEnUsoException;
import com.catalogodeliverygo.demo.Exception.RecursoNoEncontradoException;
import com.catalogodeliverygo.demo.Mapper.DtoMapper;
import com.catalogodeliverygo.demo.Model.Categoria;
import com.catalogodeliverygo.demo.Repository.CategoriaRepository;
import com.catalogodeliverygo.demo.Repository.ProductoRepository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

// CU-08: un test por flujo de cada operación del ABM de categorías
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        categoriaService = new CategoriaService(categoriaRepository, productoRepository, new DtoMapper());
    }

    @Test
    void listaLasCategorias() {
        when(categoriaRepository.findAll(Sort.by("nombre")))
            .thenReturn(List.of(new Categoria("Bebidas"), new Categoria("Postres")));

        List<CategoriaDTO> categorias = categoriaService.listarCategorias();

        assertEquals(2, categorias.size());
        assertEquals("Bebidas", categorias.get(0).getNombre());
    }

    @Test
    void creaLaCategoria() {
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoriaDTO creada = categoriaService.crearCategoria(new CategoriaRequestDTO("Empanadas"));

        assertEquals("Empanadas", creada.getNombre());
    }

    @Test
    void modificaElNombre() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(new Categoria("Hamburguesas")));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoriaDTO modificada = categoriaService.modificarCategoria(1L, new CategoriaRequestDTO("Burgers"));

        assertEquals("Burgers", modificada.getNombre());
    }

    @Test
    void modificarRechazaUnaCategoriaInexistente() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                     () -> categoriaService.modificarCategoria(99L, new CategoriaRequestDTO("X")));
    }

    @Test
    void eliminaUnaCategoriaSinProductos() {
        Categoria categoria = new Categoria("Empanadas");
        when(categoriaRepository.findById(7L)).thenReturn(Optional.of(categoria));
        when(productoRepository.existsByCategoriasId(7L)).thenReturn(false);

        categoriaService.eliminarCategoria(7L);

        verify(categoriaRepository).delete(categoria);
    }

    // DM-13: con productos asociados -> 409 (RN-03)
    @Test
    void eliminarRechazaUnaCategoriaConProductos() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(new Categoria("Hamburguesas")));
        when(productoRepository.existsByCategoriasId(1L)).thenReturn(true);

        assertThrows(CategoriaEnUsoException.class, () -> categoriaService.eliminarCategoria(1L));
        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    void eliminarRechazaUnaCategoriaInexistente() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> categoriaService.eliminarCategoria(99L));
    }
}
