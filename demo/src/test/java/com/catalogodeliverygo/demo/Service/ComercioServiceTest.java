package com.catalogodeliverygo.demo.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.catalogodeliverygo.demo.DTOs.ComercioResponseDTO;
import com.catalogodeliverygo.demo.DTOs.CrearComercioRequestDTO;
import com.catalogodeliverygo.demo.DTOs.ModificarComercioRequestDTO;
import com.catalogodeliverygo.demo.Exception.AccesoNoAutorizadoException;
import com.catalogodeliverygo.demo.Exception.RecursoDuplicadoException;
import com.catalogodeliverygo.demo.Exception.RecursoNoEncontradoException;
import com.catalogodeliverygo.demo.Mapper.DtoMapper;
import com.catalogodeliverygo.demo.Model.Comercio;
import com.catalogodeliverygo.demo.Repository.ComercioRepository;
import com.catalogodeliverygo.demo.Secutiry.ControlDeAcceso;

import java.util.List;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

// CU-01 / SRV-01, API-14 y CU-10: un test por flujo del caso de uso
@ExtendWith(MockitoExtension.class)
class ComercioServiceTest {

    @Mock
    private ComercioRepository comercioRepository;

    // DM-21: sin configurar no hace nada, como si el usuario fuera el dueño
    @Mock
    private ControlDeAcceso controlDeAcceso;

    private ComercioService comercioService;

    private final CrearComercioRequestDTO request =
        new CrearComercioRequestDTO("30799999999", "Nuevo Comercio SRL", "Av. Siempre Viva 742", "Pizzería");

    @BeforeEach
    void setUp() {
        comercioService = new ComercioService(comercioRepository, new DtoMapper(), controlDeAcceso);
    }

    @Test
    void registraElComercioConEstadoActivo() {
        when(comercioRepository.findByCuit("30799999999")).thenReturn(Optional.empty());
        when(comercioRepository.saveAndFlush(any(Comercio.class))).thenAnswer(inv -> inv.getArgument(0));

        ComercioResponseDTO respuesta = comercioService.registrarComercio(request);

        assertEquals("30799999999", respuesta.getCuit());
        assertEquals("Nuevo Comercio SRL", respuesta.getRazonSocial());
        assertEquals(Comercio.ESTADO_ACTIVO, respuesta.getEstado());
    }

    // Flujo alternativo: CUIT ya registrado -> 409 (RN-01)
    @Test
    void rechazaUnCuitYaRegistrado() {
        when(comercioRepository.findByCuit("30799999999")).thenReturn(Optional.of(new Comercio()));

        assertThrows(RecursoDuplicadoException.class, () -> comercioService.registrarComercio(request));
        verify(comercioRepository, never()).saveAndFlush(any());
    }

    // Dos altas simultáneas con el mismo CUIT: la restricción unique se informa como 409, no como 500 (RN-01)
    @Test
    void informaComoDuplicadoSiLaBaseRechazaElCuit() {
        when(comercioRepository.findByCuit("30799999999")).thenReturn(Optional.empty());
        when(comercioRepository.saveAndFlush(any(Comercio.class)))
            .thenThrow(new DataIntegrityViolationException("unique cuit"));

        assertThrows(RecursoDuplicadoException.class, () -> comercioService.registrarComercio(request));
    }

    // ---- API-14 / INT-03: consultar comercio ----

    @Test
    void obtieneUnComercioActivo() {
        when(comercioRepository.findById(1L)).thenReturn(Optional.of(
            new Comercio("30712345678", "La Esquina SRL", "Av. Corrientes 1450", "Hamburguesería", Comercio.ESTADO_ACTIVO)));

        ComercioResponseDTO respuesta = comercioService.obtenerComercio(1L);

        assertEquals("30712345678", respuesta.getCuit());
        assertEquals("La Esquina SRL", respuesta.getRazonSocial());
        assertEquals(Comercio.ESTADO_ACTIVO, respuesta.getEstado());
    }

    // DM-18: un comercio INACTIVO se devuelve igual, con su estado, para que el otro módulo lo vea
    @Test
    void obtieneTambienUnComercioInactivo() {
        when(comercioRepository.findById(4L)).thenReturn(Optional.of(
            new Comercio("30712345681", "Sushi Cerrado SRL", "Av. Callao 300", "Sushi", Comercio.ESTADO_INACTIVO)));

        assertEquals(Comercio.ESTADO_INACTIVO, comercioService.obtenerComercio(4L).getEstado());
    }

    // Flujo alternativo: comercio inexistente -> 404
    @Test
    void obtenerRechazaUnComercioInexistente() {
        when(comercioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> comercioService.obtenerComercio(999L));
    }

    // ---- CU-10: modificar información del comercio ----

    @Test
    void modificaLosDatosSinTocarElCuit() {
        Comercio existente = new Comercio("30712345678", "La Esquina SRL", "Av. Corrientes 1450", "Hamburguesería",
                                          Comercio.ESTADO_ACTIVO);
        when(comercioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(comercioRepository.save(any(Comercio.class))).thenAnswer(inv -> inv.getArgument(0));

        ComercioResponseDTO respuesta = comercioService.modificarComercio(1L,
            new ModificarComercioRequestDTO("La Esquina Burgers SRL", "Av. Corrientes 1500", "Hamburguesería"));

        assertEquals("La Esquina Burgers SRL", respuesta.getRazonSocial());
        assertEquals("30712345678", respuesta.getCuit());
        assertEquals("Av. Corrientes 1500", existente.getDireccion());
    }

    // Flujo alternativo: comercio inexistente -> 404
    @Test
    void modificarRechazaUnComercioInexistente() {
        when(comercioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> comercioService.modificarComercio(99L,
            new ModificarComercioRequestDTO("X", "Y", "Z")));
    }

    // ---- DM-21: responsable del comercio ----

    private Comercio guardado() {
        ArgumentCaptor<Comercio> captor = ArgumentCaptor.forClass(Comercio.class);
        verify(comercioRepository).saveAndFlush(captor.capture());
        return captor.getValue();
    }

    @Test
    void quienRegistraConRolComercioQuedaComoResponsable() {
        when(controlDeAcceso.usuarioActual()).thenReturn("prueba-comercio");
        when(comercioRepository.findByCuit("30799999999")).thenReturn(Optional.empty());
        when(comercioRepository.saveAndFlush(any(Comercio.class))).thenAnswer(inv -> inv.getArgument(0));
        // Un COMERCIO no puede registrar a nombre de otro: el campo se ignora
        request.setUsuarioResponsable("otro-usuario");

        comercioService.registrarComercio(request);

        assertEquals("prueba-comercio", guardado().getUsuarioResponsable());
    }

    @Test
    void unAdminPuedeIndicarElResponsable() {
        when(controlDeAcceso.esAdmin()).thenReturn(true);
        when(comercioRepository.findByCuit("30799999999")).thenReturn(Optional.empty());
        when(comercioRepository.saveAndFlush(any(Comercio.class))).thenAnswer(inv -> inv.getArgument(0));
        request.setUsuarioResponsable("  nuevo-comerciante ");

        comercioService.registrarComercio(request);

        assertEquals("nuevo-comerciante", guardado().getUsuarioResponsable());
    }

    @Test
    void modificarRechazaUnComercioDeOtroUsuario() {
        Comercio ajeno = new Comercio("30712345679", "Don Pietro SRL", "Av. Cabildo 2100", "Pizzería",
                                      Comercio.ESTADO_ACTIVO, "prueba-comercio-2");
        when(comercioRepository.findById(2L)).thenReturn(Optional.of(ajeno));
        doThrow(new AccesoNoAutorizadoException("El comercio 2 no pertenece al usuario autenticado"))
            .when(controlDeAcceso).verificarPuedeAdministrar(ajeno);

        assertThrows(AccesoNoAutorizadoException.class, () -> comercioService.modificarComercio(2L,
            new ModificarComercioRequestDTO("Otro nombre", "Otra dirección", "Otro rubro")));
        verify(comercioRepository, never()).save(any());
        assertEquals("Don Pietro SRL", ajeno.getRazonSocial());
    }

    @Test
    void listaLosComerciosDelUsuario() {
        when(controlDeAcceso.usuarioActual()).thenReturn("prueba-comercio");
        when(comercioRepository.findByUsuarioResponsableOrderById("prueba-comercio")).thenReturn(List.of(
            new Comercio("30712345678", "La Esquina SRL", "Av. Corrientes 1450", "Hamburguesería",
                         Comercio.ESTADO_ACTIVO, "prueba-comercio")));

        List<ComercioResponseDTO> mios = comercioService.listarMisComercios();

        assertEquals(1, mios.size());
        assertEquals("30712345678", mios.get(0).getCuit());
    }
}
