package com.catalogodeliverygo.demo.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.catalogodeliverygo.demo.DTOs.SucursalDTO;
import com.catalogodeliverygo.demo.DTOs.SucursalRequestDTO;
import com.catalogodeliverygo.demo.Exception.ComercioInactivoException;
import com.catalogodeliverygo.demo.Exception.RecursoNoEncontradoException;
import com.catalogodeliverygo.demo.Mapper.DtoMapper;
import com.catalogodeliverygo.demo.Model.Comercio;
import com.catalogodeliverygo.demo.Model.Sucursal;
import com.catalogodeliverygo.demo.Repository.ComercioRepository;
import com.catalogodeliverygo.demo.Repository.SucursalRepository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

// CU-07: un test por flujo de cada operación del ABM de sucursales
@ExtendWith(MockitoExtension.class)
class SucursalServiceTest {

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private ComercioRepository comercioRepository;

    private SucursalService sucursalService;

    private Comercio activo;
    private Comercio inactivo;
    private Comercio otro;

    @BeforeEach
    void setUp() {
        sucursalService = new SucursalService(sucursalRepository, comercioRepository, new DtoMapper());
        activo = comercio(1L, Comercio.ESTADO_ACTIVO);
        inactivo = comercio(4L, Comercio.ESTADO_INACTIVO);
        otro = comercio(2L, Comercio.ESTADO_ACTIVO);
    }

    // El id lo asigna la base; en un test unitario se fija a mano
    private Comercio comercio(Long id, String estado) {
        Comercio comercio = new Comercio("3071234567" + id, "Comercio " + id, "Dirección", "Rubro", estado);
        ReflectionTestUtils.setField(comercio, "id", id);
        return comercio;
    }

    private final SucursalRequestDTO pedido = new SucursalRequestDTO("Av. Santa Fe 2800, CABA");

    // ---- Listar ----

    @Test
    void listaLasSucursalesDelComercio() {
        when(comercioRepository.findById(1L)).thenReturn(Optional.of(activo));
        when(sucursalRepository.findByComercioId(1L)).thenReturn(List.of(new Sucursal(activo, "Av. Corrientes 1450")));

        List<SucursalDTO> sucursales = sucursalService.listarSucursales(1L);

        assertEquals(1, sucursales.size());
        assertEquals(1L, sucursales.get(0).getComercioId());
    }

    @Test
    void listarRechazaUnComercioInexistente() {
        when(comercioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> sucursalService.listarSucursales(99L));
    }

    // ---- Crear ----

    @Test
    void creaLaSucursal() {
        when(comercioRepository.findById(1L)).thenReturn(Optional.of(activo));
        when(sucursalRepository.save(any(Sucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        SucursalDTO creada = sucursalService.crearSucursal(1L, pedido);

        assertEquals("Av. Santa Fe 2800, CABA", creada.getDireccion());
        assertEquals(1L, creada.getComercioId());
    }

    // RN-02: comercio inactivo -> 409
    @Test
    void crearRechazaUnComercioInactivo() {
        when(comercioRepository.findById(4L)).thenReturn(Optional.of(inactivo));

        assertThrows(ComercioInactivoException.class, () -> sucursalService.crearSucursal(4L, pedido));
        verify(sucursalRepository, never()).save(any());
    }

    @Test
    void crearRechazaUnComercioInexistente() {
        when(comercioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> sucursalService.crearSucursal(99L, pedido));
    }

    // ---- Modificar ----

    @Test
    void modificaLaDireccion() {
        when(comercioRepository.findById(1L)).thenReturn(Optional.of(activo));
        when(sucursalRepository.findById(7L)).thenReturn(Optional.of(new Sucursal(activo, "Vieja 123")));
        when(sucursalRepository.save(any(Sucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        SucursalDTO modificada = sucursalService.modificarSucursal(1L, 7L, pedido);

        assertEquals("Av. Santa Fe 2800, CABA", modificada.getDireccion());
    }

    // REL-01: la sucursal de otro comercio responde 404
    @Test
    void modificarRechazaUnaSucursalDeOtroComercio() {
        when(comercioRepository.findById(1L)).thenReturn(Optional.of(activo));
        when(sucursalRepository.findById(7L)).thenReturn(Optional.of(new Sucursal(otro, "Ajena 456")));

        assertThrows(RecursoNoEncontradoException.class, () -> sucursalService.modificarSucursal(1L, 7L, pedido));
        verify(sucursalRepository, never()).save(any());
    }

    // ---- Eliminar ----

    @Test
    void eliminaLaSucursal() {
        Sucursal sucursal = new Sucursal(activo, "Av. Corrientes 1450");
        when(comercioRepository.findById(1L)).thenReturn(Optional.of(activo));
        when(sucursalRepository.findById(7L)).thenReturn(Optional.of(sucursal));

        sucursalService.eliminarSucursal(1L, 7L);

        verify(sucursalRepository).delete(sucursal);
    }

    @Test
    void eliminarRechazaUnComercioInactivo() {
        when(comercioRepository.findById(4L)).thenReturn(Optional.of(inactivo));

        assertThrows(ComercioInactivoException.class, () -> sucursalService.eliminarSucursal(4L, 7L));
        verify(sucursalRepository, never()).delete(any());
    }
}
