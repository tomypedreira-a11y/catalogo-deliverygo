package com.catalogodeliverygo.demo.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.catalogodeliverygo.demo.DTOs.CrearProductoRequestDTO;
import com.catalogodeliverygo.demo.DTOs.ProductoResponseDTO;
import com.catalogodeliverygo.demo.Exception.ComercioInactivoException;
import com.catalogodeliverygo.demo.Exception.RecursoNoEncontradoException;
import com.catalogodeliverygo.demo.Exception.ReglaDeNegocioException;
import com.catalogodeliverygo.demo.Mapper.DtoMapper;
import com.catalogodeliverygo.demo.Model.Categoria;
import com.catalogodeliverygo.demo.Model.Comercio;
import com.catalogodeliverygo.demo.Model.Producto;
import com.catalogodeliverygo.demo.Repository.CategoriaRepository;
import com.catalogodeliverygo.demo.Repository.ComercioRepository;
import com.catalogodeliverygo.demo.Repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Un test por flujo de cada caso de uso de ProductoService
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ComercioRepository comercioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    private ProductoService productoService;

    private final Comercio comercioActivo =
        new Comercio("30712345678", "La Esquina SRL", "Av. Corrientes 1450", "Hamburguesería", Comercio.ESTADO_ACTIVO);

    @BeforeEach
    void setUp() {
        productoService = new ProductoService(productoRepository, comercioRepository, categoriaRepository,
                                              new DtoMapper());
    }

    private CrearProductoRequestDTO pedido(List<Long> categoriaIds) {
        return new CrearProductoRequestDTO("Hamburguesa clásica", "Carne, queso, lechuga y tomate.",
                                           new BigDecimal("8500.00"), categoriaIds, 1L);
    }

    // ---- CU-02 / SRV-02: publicar producto ----

    @Test
    void publicaElProductoActivoYDisponible() {
        when(comercioRepository.findByIdAndEstado(1L, Comercio.ESTADO_ACTIVO)).thenReturn(Optional.of(comercioActivo));
        when(categoriaRepository.findAllById(Set.of(1L))).thenReturn(List.of(new Categoria("Hamburguesas")));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponseDTO respuesta = productoService.publicarProducto(pedido(List.of(1L)));

        assertEquals("Hamburguesa clásica", respuesta.getNombre());
        assertEquals(new BigDecimal("8500.00"), respuesta.getPrecio());
        assertTrue(respuesta.isDisponible());
        assertEquals(Producto.ESTADO_ACTIVO, respuesta.getEstado());
        assertEquals("Hamburguesas", respuesta.getCategorias().get(0).getNombre());
    }

    // Flujo alternativo: comercio inactivo o inexistente -> 404 (RN-02, DM-09)
    @Test
    void rechazaUnComercioInactivoOInexistente() {
        when(comercioRepository.findByIdAndEstado(1L, Comercio.ESTADO_ACTIVO)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.publicarProducto(pedido(List.of(1L))));
        verify(productoRepository, never()).save(any());
    }

    // Flujo alternativo: categoría inexistente -> 400 (RN-03)
    @Test
    void rechazaUnaCategoriaInexistente() {
        when(comercioRepository.findByIdAndEstado(1L, Comercio.ESTADO_ACTIVO)).thenReturn(Optional.of(comercioActivo));
        when(categoriaRepository.findAllById(Set.of(1L, 99L))).thenReturn(List.of(new Categoria("Hamburguesas")));

        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class,
                                                  () -> productoService.publicarProducto(pedido(List.of(1L, 99L))));
        assertTrue(ex.getMessage().contains("99"));
        verify(productoRepository, never()).save(any());
    }

    // Una categoría repetida en el pedido cuenta una sola vez
    @Test
    void ignoraCategoriasRepetidas() {
        when(comercioRepository.findByIdAndEstado(1L, Comercio.ESTADO_ACTIVO)).thenReturn(Optional.of(comercioActivo));
        when(categoriaRepository.findAllById(Set.of(1L))).thenReturn(List.of(new Categoria("Hamburguesas")));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponseDTO respuesta = productoService.publicarProducto(pedido(List.of(1L, 1L)));

        assertEquals(1, respuesta.getCategorias().size());
    }

    // ---- CU-03 / SRV-03: modificar precio ----

    private Producto productoDe(Comercio comercio) {
        return new Producto("Hamburguesa clásica", "Carne, queso, lechuga y tomate.", new BigDecimal("8500.00"),
                            true, Producto.ESTADO_ACTIVO, comercio, new ArrayList<>());
    }

    @Test
    void modificaElPrecio() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(comercioActivo)));
        when(comercioRepository.findByIdAndEstado(any(), eq(Comercio.ESTADO_ACTIVO))).thenReturn(Optional.of(comercioActivo));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponseDTO respuesta = productoService.modificarPrecio(10L, new BigDecimal("9200.00"));

        assertEquals(new BigDecimal("9200.00"), respuesta.getPrecio());
    }

    // Flujo alternativo: producto inexistente -> 404
    @Test
    void modificarPrecioRechazaUnProductoInexistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                     () -> productoService.modificarPrecio(99L, new BigDecimal("9200.00")));
    }

    // Flujo alternativo: comercio del producto inactivo -> 409 (RN-02)
    @Test
    void modificarPrecioRechazaSiElComercioEstaInactivo() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(comercioActivo)));
        when(comercioRepository.findByIdAndEstado(any(), eq(Comercio.ESTADO_ACTIVO))).thenReturn(Optional.empty());

        assertThrows(ComercioInactivoException.class,
                     () -> productoService.modificarPrecio(10L, new BigDecimal("9200.00")));
        verify(productoRepository, never()).save(any());
    }

    // ---- CU-04 / SRV-04: cambiar disponibilidad ----

    @Test
    void cambiaLaDisponibilidad() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(comercioActivo)));
        when(comercioRepository.findByIdAndEstado(any(), eq(Comercio.ESTADO_ACTIVO))).thenReturn(Optional.of(comercioActivo));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponseDTO respuesta = productoService.cambiarDisponibilidad(10L, false);

        assertFalse(respuesta.isDisponible());
        assertEquals(Producto.ESTADO_ACTIVO, respuesta.getEstado());
    }

    // Flujo alternativo: producto inexistente -> 404
    @Test
    void cambiarDisponibilidadRechazaUnProductoInexistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.cambiarDisponibilidad(99L, false));
    }

    // Flujo alternativo: comercio del producto inactivo -> 409 (RN-02)
    @Test
    void cambiarDisponibilidadRechazaSiElComercioEstaInactivo() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(comercioActivo)));
        when(comercioRepository.findByIdAndEstado(any(), eq(Comercio.ESTADO_ACTIVO))).thenReturn(Optional.empty());

        assertThrows(ComercioInactivoException.class, () -> productoService.cambiarDisponibilidad(10L, false));
        verify(productoRepository, never()).save(any());
    }

    // ---- CU-06: consultar detalle de producto (RN-05) ----

    @Test
    void devuelveElDetalleDeUnProductoPublicado() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(comercioActivo)));

        ProductoResponseDTO respuesta = productoService.obtenerProducto(10L);

        assertEquals("Hamburguesa clásica", respuesta.getNombre());
    }

    @Test
    void detalleRechazaUnProductoInexistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.obtenerProducto(99L));
    }

    // RN-05: no disponible, inactivo o de comercio inactivo responden 404, igual que inexistente
    @Test
    void detalleOcultaUnProductoNoDisponible() {
        Producto producto = productoDe(comercioActivo);
        producto.setDisponible(false);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.obtenerProducto(10L));
    }

    @Test
    void detalleOcultaUnProductoInactivo() {
        Producto producto = productoDe(comercioActivo);
        producto.setEstado(Producto.ESTADO_INACTIVO);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.obtenerProducto(10L));
    }

    @Test
    void detalleOcultaUnProductoDeComercioInactivo() {
        Comercio inactivo = new Comercio("30712345681", "Sushi Cerrado SRL", "Av. Callao 300", "Sushi",
                                         Comercio.ESTADO_INACTIVO);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(inactivo)));

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.obtenerProducto(10L));
    }

    // ---- CU-09: desactivar/reactivar producto (DM-12) ----

    @Test
    void desactivaElProducto() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(comercioActivo)));
        when(comercioRepository.findByIdAndEstado(any(), eq(Comercio.ESTADO_ACTIVO))).thenReturn(Optional.of(comercioActivo));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponseDTO respuesta = productoService.cambiarEstado(10L, false);

        assertEquals(Producto.ESTADO_INACTIVO, respuesta.getEstado());
        assertTrue(respuesta.isDisponible());
    }

    @Test
    void reactivaElProducto() {
        Producto producto = productoDe(comercioActivo);
        producto.setEstado(Producto.ESTADO_INACTIVO);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(comercioRepository.findByIdAndEstado(any(), eq(Comercio.ESTADO_ACTIVO))).thenReturn(Optional.of(comercioActivo));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponseDTO respuesta = productoService.cambiarEstado(10L, true);

        assertEquals(Producto.ESTADO_ACTIVO, respuesta.getEstado());
    }

    // Flujo alternativo: producto inexistente -> 404
    @Test
    void cambiarEstadoRechazaUnProductoInexistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.cambiarEstado(99L, false));
    }

    // Flujo alternativo: comercio del producto inactivo -> 409 (RN-02)
    @Test
    void cambiarEstadoRechazaSiElComercioEstaInactivo() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(comercioActivo)));
        when(comercioRepository.findByIdAndEstado(any(), eq(Comercio.ESTADO_ACTIVO))).thenReturn(Optional.empty());

        assertThrows(ComercioInactivoException.class, () -> productoService.cambiarEstado(10L, false));
        verify(productoRepository, never()).save(any());
    }
}
