package com.catalogodeliverygo.demo.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.catalogodeliverygo.demo.DTOs.CatalogoResponseDTO;
import com.catalogodeliverygo.demo.DTOs.ProductoResponseDTO;
import com.catalogodeliverygo.demo.Exception.ReglaDeNegocioException;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

// CU-05 / REP-04: la búsqueda es una consulta a la base, así que se prueba contra los datos de data.sql.
// De sus 13 productos se publican 9: quedan afuera Brownie y Flan (no disponibles), Fainá (INACTIVO)
// y Roll California (comercio INACTIVO)
@SpringBootTest
@Transactional
class CatalogoServiceTest {

    // ids de data.sql: categorías en orden de inserción, comercios por orden de alta
    private static final long VEGANO = 5L;
    private static final long POSTRES = 4L;
    private static final long SIN_TACC = 6L;
    private static final long DON_PIETRO = 2L;
    private static final long SUSHI_CERRADO = 4L;

    @Autowired
    private CatalogoService catalogoService;

    private CatalogoResponseDTO buscar(String texto, List<Long> categorias, Long comercioId,
                                       String min, String max) {
        return catalogoService.buscarCatalogo(texto, categorias, comercioId,
                                              min == null ? null : new BigDecimal(min),
                                              max == null ? null : new BigDecimal(max), 0, 20);
    }

    private List<String> nombres(CatalogoResponseDTO respuesta) {
        return respuesta.getItems().stream().map(ProductoResponseDTO::getNombre).toList();
    }

    // RN-05: sin filtros devuelve solo los publicados, ordenados por nombre
    @Test
    void sinFiltrosDevuelveSoloLosPublicados() {
        CatalogoResponseDTO respuesta = buscar(null, null, null, null, null);

        assertEquals(9, respuesta.getTotal());
        assertEquals("Agua mineral", respuesta.getItems().get(0).getNombre());
        assertTrue(respuesta.getItems().stream()
                            .allMatch(p -> p.isDisponible() && "ACTIVO".equals(p.getEstado())));
    }

    @Test
    void filtraPorTextoEnElNombreSinDistinguirMayusculas() {
        assertEquals(List.of("Hamburguesa clásica", "Hamburguesa de lentejas"),
                     nombres(buscar("HAMBURGUESA", null, null, null, null)));
    }

    // CU-05 incluye consultar comercios: el texto también busca en la razón social
    @Test
    void filtraPorTextoEnLaRazonSocialDelComercio() {
        assertEquals(5, buscar("esquina", null, null, null, null).getTotal());
    }

    @Test
    void filtraPorUnaCategoria() {
        assertEquals(List.of("Hamburguesa de lentejas", "Helado de frutilla al agua", "Limonada de la casa"),
                     nombres(buscar(null, List.of(VEGANO), null, null, null)));
    }

    // Helado de chocolate es Postres y Sin TACC: tiene que aparecer una sola vez
    @Test
    void variasCategoriasNoRepitenProductos() {
        assertEquals(List.of("Agua mineral", "Helado de chocolate", "Helado de frutilla al agua", "Tiramisú"),
                     nombres(buscar(null, List.of(POSTRES, SIN_TACC), null, null, null)));
    }

    @Test
    void filtraPorComercio() {
        assertEquals(List.of("Gaseosa cola", "Tiramisú"), nombres(buscar(null, null, DON_PIETRO, null, null)));
    }

    // RN-05: los productos de un comercio inactivo no salen aunque se lo pida por id
    @Test
    void unComercioInactivoNoDevuelveProductos() {
        assertEquals(0, buscar(null, null, SUSHI_CERRADO, null, null).getTotal());
    }

    @Test
    void filtraPorRangoDePrecio() {
        assertEquals(List.of("Helado de chocolate", "Helado de frutilla al agua", "Limonada de la casa", "Tiramisú"),
                     nombres(buscar(null, null, null, "3000", "6000")));
    }

    @Test
    void combinaVariosFiltros() {
        assertEquals(List.of("Helado de frutilla al agua"),
                     nombres(buscar("helado", List.of(VEGANO), null, null, "6000")));
    }

    // Sin resultados no es error: lista vacía (flujo alternativo de CU-05)
    @Test
    void sinResultadosDevuelveListaVacia() {
        CatalogoResponseDTO respuesta = buscar("milanesa", null, null, null, null);

        assertEquals(0, respuesta.getTotal());
        assertTrue(respuesta.getItems().isEmpty());
    }

    @Test
    void pagina() {
        CatalogoResponseDTO primera = catalogoService.buscarCatalogo(null, null, null, null, null, 0, 4);
        CatalogoResponseDTO ultima = catalogoService.buscarCatalogo(null, null, null, null, null, 2, 4);

        assertEquals(4, primera.getItems().size());
        assertEquals(9, primera.getTotal());
        assertEquals(1, ultima.getItems().size());
        assertEquals(2, ultima.getPage());
    }

    // ---- Flujo alternativo: criterios inválidos -> 400 (DM-07) ----

    @Test
    void rechazaPrecioMinimoMayorAlMaximo() {
        assertThrows(ReglaDeNegocioException.class, () -> buscar(null, null, null, "5000", "1000"));
    }

    @Test
    void rechazaPreciosNegativos() {
        assertThrows(ReglaDeNegocioException.class, () -> buscar(null, null, null, "-1", null));
    }

    @Test
    void rechazaPaginaYTamanioInvalidos() {
        assertThrows(ReglaDeNegocioException.class,
                     () -> catalogoService.buscarCatalogo(null, null, null, null, null, -1, 20));
        assertThrows(ReglaDeNegocioException.class,
                     () -> catalogoService.buscarCatalogo(null, null, null, null, null, 0, 0));
        assertThrows(ReglaDeNegocioException.class,
                     () -> catalogoService.buscarCatalogo(null, null, null, null, null, 0, 101));
    }
}
