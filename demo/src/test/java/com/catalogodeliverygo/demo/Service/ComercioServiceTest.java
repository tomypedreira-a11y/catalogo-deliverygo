package com.catalogodeliverygo.demo.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.catalogodeliverygo.demo.DTOs.ComercioResponseDTO;
import com.catalogodeliverygo.demo.DTOs.CrearComercioRequestDTO;
import com.catalogodeliverygo.demo.DTOs.ModificarComercioRequestDTO;
import com.catalogodeliverygo.demo.Exception.RecursoDuplicadoException;
import com.catalogodeliverygo.demo.Exception.RecursoNoEncontradoException;
import com.catalogodeliverygo.demo.Mapper.DtoMapper;
import com.catalogodeliverygo.demo.Model.Comercio;
import com.catalogodeliverygo.demo.Repository.ComercioRepository;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

// CU-01 / SRV-01: un test por flujo del caso de uso
@ExtendWith(MockitoExtension.class)
class ComercioServiceTest {

    @Mock
    private ComercioRepository comercioRepository;

    private ComercioService comercioService;

    private final CrearComercioRequestDTO request =
        new CrearComercioRequestDTO("30799999999", "Nuevo Comercio SRL", "Av. Siempre Viva 742", "Pizzería");

    @BeforeEach
    void setUp() {
        comercioService = new ComercioService(comercioRepository, new DtoMapper());
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
}
