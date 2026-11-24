package com.catalogodeliverygo.demo.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.catalogodeliverygo.demo.DTOs.CrearProductoRequestDTO;
import com.catalogodeliverygo.demo.DTOs.DisponibilidadProductoDTO;
import com.catalogodeliverygo.demo.DTOs.ProductoResponseDTO;
import com.catalogodeliverygo.demo.DTOs.VerificacionDisponibilidadResponseDTO;
import com.catalogodeliverygo.demo.Exception.AccesoNoAutorizadoException;
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
import com.catalogodeliverygo.demo.Secutiry.ControlDeAcceso;

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
import org.springframework.test.util.ReflectionTestUtils;

// Un test por flujo de cada caso de uso de ProductoService
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ComercioRepository comercioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    // DM-21: sin configurar no hace nada, como si el usuario fuera el dueño. Los tests de comercio ajeno lo hacen fallar
    @Mock
    private ControlDeAcceso controlDeAcceso;

    private ProductoService productoService;

    private final Comercio comercioActivo =
        new Comercio("30712345678", "La Esquina SRL", "Av. Corrientes 1450", "Hamburguesería", Comercio.ESTADO_ACTIVO);

    @BeforeEach
    void setUp() {
        productoService = new ProductoService(productoRepository, comercioRepository, categoriaRepository,
                                              new DtoMapper(), controlDeAcceso);
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

    // ---- DM-21: un comercio solo modifica lo suyo ----

    private void elUsuarioNoEsElDuenio() {
        doThrow(new AccesoNoAutorizadoException("El comercio no pertenece al usuario autenticado"))
            .when(controlDeAcceso).verificarPuedeAdministrar(any());
    }

    @Test
    void publicarRechazaUnComercioDeOtroUsuario() {
        when(comercioRepository.findByIdAndEstado(1L, Comercio.ESTADO_ACTIVO)).thenReturn(Optional.of(comercioActivo));
        elUsuarioNoEsElDuenio();

        assertThrows(AccesoNoAutorizadoException.class, () -> productoService.publicarProducto(pedido(List.of(1L))));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void modificarPrecioRechazaUnProductoDeOtroComercio() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(comercioActivo)));
        elUsuarioNoEsElDuenio();

        assertThrows(AccesoNoAutorizadoException.class,
                     () -> productoService.modificarPrecio(10L, new BigDecimal("1.00")));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void cambiarDisponibilidadRechazaUnProductoDeOtroComercio() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(comercioActivo)));
        elUsuarioNoEsElDuenio();

        assertThrows(AccesoNoAutorizadoException.class, () -> productoService.cambiarDisponibilidad(10L, false));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void cambiarEstadoRechazaUnProductoDeOtroComercio() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(comercioActivo)));
        elUsuarioNoEsElDuenio();

        assertThrows(AccesoNoAutorizadoException.class, () -> productoService.cambiarEstado(10L, false));
        verify(productoRepository, never()).save(any());
    }

    // ---- API-16 / DM-22: verificar disponibilidad para Pedidos ----

    private Producto productoConId(Long id, Comercio comercio, boolean disponible, String estado) {
        Producto producto = new Producto("Producto " + id, "Descripción", new BigDecimal("1000.00"),
                                         disponible, estado, comercio, new ArrayList<>());
        ReflectionTestUtils.setField(producto, "id", id);
        return producto;
    }

    @Test
    void informaElMotivoDeCadaProducto() {
        Comercio inactivo = new Comercio("30712345681", "Sushi Cerrado SRL", "Av. Callao 300", "Sushi",
                                         Comercio.ESTADO_INACTIVO);
        when(productoRepository.findAllById(any())).thenReturn(List.of(
            productoConId(1L, comercioActivo, true, Producto.ESTADO_ACTIVO),
            productoConId(6L, comercioActivo, false, Producto.ESTADO_ACTIVO),
            productoConId(10L, comercioActivo, true, Producto.ESTADO_INACTIVO),
            productoConId(13L, inactivo, true, Producto.ESTADO_ACTIVO)));

        VerificacionDisponibilidadResponseDTO respuesta =
            productoService.verificarDisponibilidad(List.of(1L, 6L, 10L, 13L, 999L));

        assertFalse(respuesta.isTodosDisponibles());
        List<String> motivos = respuesta.getProductos().stream().map(DisponibilidadProductoDTO::getMotivo).toList();
        assertEquals(List.of(DisponibilidadProductoDTO.MOTIVO_DISPONIBLE,
                             DisponibilidadProductoDTO.MOTIVO_NO_DISPONIBLE,
                             DisponibilidadProductoDTO.MOTIVO_PRODUCTO_INACTIVO,
                             DisponibilidadProductoDTO.MOTIVO_COMERCIO_INACTIVO,
                             DisponibilidadProductoDTO.MOTIVO_INEXISTENTE), motivos);
        assertTrue(respuesta.getProductos().get(0).isDisponible());
        assertFalse(respuesta.getProductos().get(1).isDisponible());
        assertEquals(new BigDecimal("1000.00"), respuesta.getProductos().get(1).getPrecio());
        assertEquals(null, respuesta.getProductos().get(4).getNombre());
    }

    @Test
    void todosDisponiblesSiNingunoFalla() {
        when(productoRepository.findAllById(any())).thenReturn(List.of(
            productoConId(1L, comercioActivo, true, Producto.ESTADO_ACTIVO),
            productoConId(2L, comercioActivo, true, Producto.ESTADO_ACTIVO)));

        assertTrue(productoService.verificarDisponibilidad(List.of(1L, 2L)).isTodosDisponibles());
    }

    @Test
    void verificarIgnoraIdsRepetidosYRespetaElOrden() {
        when(productoRepository.findAllById(any())).thenReturn(List.of(
            productoConId(1L, comercioActivo, true, Producto.ESTADO_ACTIVO),
            productoConId(2L, comercioActivo, true, Producto.ESTADO_ACTIVO)));

        List<Long> ids = productoService.verificarDisponibilidad(List.of(2L, 1L, 2L)).getProductos().stream()
                                        .map(DisponibilidadProductoDTO::getProductoId).toList();

        assertEquals(List.of(2L, 1L), ids);
    }

    @Test
    void verificarExigeAlMenosUnId() {
        assertThrows(ReglaDeNegocioException.class, () -> productoService.verificarDisponibilidad(null));
        assertThrows(ReglaDeNegocioException.class, () -> productoService.verificarDisponibilidad(List.of()));
    }

    @Test
    void verificarRechazaMasDeCienProductos() {
        List<Long> ids = java.util.stream.LongStream.rangeClosed(1, 101).boxed().toList();

        assertThrows(ReglaDeNegocioException.class, () -> productoService.verificarDisponibilidad(ids));
        verify(productoRepository, never()).findAllById(any());
    }
}
