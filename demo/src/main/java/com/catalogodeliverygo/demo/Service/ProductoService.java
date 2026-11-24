package com.catalogodeliverygo.demo.Service;

import com.catalogodeliverygo.demo.DTOs.CrearProductoRequestDTO;
import com.catalogodeliverygo.demo.DTOs.DisponibilidadProductoDTO;
import com.catalogodeliverygo.demo.DTOs.ProductoResponseDTO;
import com.catalogodeliverygo.demo.DTOs.VerificacionDisponibilidadResponseDTO;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductoService {

    // DM-22: tope de productos por verificación, el mismo que el tamaño máximo de página del catálogo (DM-07)
    static final int MAXIMO_PRODUCTOS_A_VERIFICAR = 100;

    private final ProductoRepository productoRepository;
    private final ComercioRepository comercioRepository;
    private final CategoriaRepository categoriaRepository;
    private final DtoMapper mapper;
    private final ControlDeAcceso controlDeAcceso;

    public ProductoService(ProductoRepository productoRepository, ComercioRepository comercioRepository,
                           CategoriaRepository categoriaRepository, DtoMapper mapper,
                           ControlDeAcceso controlDeAcceso) {
        this.productoRepository = productoRepository;
        this.comercioRepository = comercioRepository;
        this.categoriaRepository = categoriaRepository;
        this.mapper = mapper;
        this.controlDeAcceso = controlDeAcceso;
    }

    // SRV-02 / CU-02: valida comercio activo (RN-02, REP-02), categorías existentes (RN-03, REP-06) y persiste
    // con estado ACTIVO y disponible = true (REP-05). Datos obligatorios, precio > 0 y al menos una categoría
    // (RN-06, RN-04, RN-03) ya los valida Bean Validation en el DTO. Solo se publica en un comercio propio (DM-21)
    public ProductoResponseDTO publicarProducto(CrearProductoRequestDTO request) {
        // DM-09: findByIdAndEstado no distingue inexistente de inactivo, por eso ambos casos son 404
        Comercio comercio = comercioRepository.findByIdAndEstado(request.getComercioId(), Comercio.ESTADO_ACTIVO)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No existe un comercio activo con id " + request.getComercioId()));
        controlDeAcceso.verificarPuedeAdministrar(comercio);

        List<Categoria> categorias = buscarCategorias(request.getCategoriaIds());

        Producto producto = new Producto(request.getNombre(),
                                         request.getDescripcion(),
                                         request.getPrecio(),
                                         true,
                                         Producto.ESTADO_ACTIVO,
                                         comercio,
                                         categorias);

        return mapper.toProductoResponse(productoRepository.save(producto));
    }

    // SRV-03 / CU-03: busca el producto (REP-03), valida comercio activo (RN-02) y actualiza el precio (REP-05).
    // El precio > 0 (RN-04) ya lo valida Bean Validation en el DTO
    public ProductoResponseDTO modificarPrecio(Long productoId, BigDecimal nuevoPrecio) {
        Producto producto = buscarProductoDeComercioActivo(productoId);
        producto.setPrecio(nuevoPrecio);
        return mapper.toProductoResponse(productoRepository.save(producto));
    }

    // SRV-04 / CU-04: busca el producto (REP-03), valida comercio activo (RN-02) y actualiza el flag (REP-05).
    // Un producto no disponible deja de salir en el catálogo (RN-05). nuevoEstado no llega null: lo valida el DTO (DM-10)
    public ProductoResponseDTO cambiarDisponibilidad(Long productoId, Boolean nuevoEstado) {
        Producto producto = buscarProductoDeComercioActivo(productoId);
        producto.setDisponible(nuevoEstado);
        return mapper.toProductoResponse(productoRepository.save(producto));
    }

    // CU-06: busca el producto y lo devuelve solo si está ACTIVO, disponible y su comercio activo (RN-05)
    @Transactional(readOnly = true)
    // Un producto oculto por RN-05 responde igual que uno inexistente, para no revelar que existe
    public ProductoResponseDTO obtenerProducto(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
            .filter(p -> Producto.ESTADO_ACTIVO.equals(p.getEstado()))
            .filter(Producto::isDisponible)
            .filter(p -> Comercio.ESTADO_ACTIVO.equals(p.getComercio().getEstado()))
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto disponible con id " + productoId));

        return mapper.toProductoResponse(producto);
    }

    // CU-09: baja lógica; pasa Producto.estado a INACTIVO o ACTIVO según activo (DM-12). Valida comercio activo (RN-02)
    // Un producto INACTIVO sale del catálogo y del detalle (RN-05) pero no se borra: se puede reactivar.
    // activo no llega null: lo valida el DTO
    public ProductoResponseDTO cambiarEstado(Long productoId, Boolean activo) {
        Producto producto = buscarProductoDeComercioActivo(productoId);
        producto.setEstado(activo ? Producto.ESTADO_ACTIVO : Producto.ESTADO_INACTIVO);
        return mapper.toProductoResponse(productoRepository.save(producto));
    }

    // API-16 / DM-22 / INT-02: Pedidos verifica los productos de un pedido al crearlo y otra vez al confirmarlo.
    // A diferencia de CU-06, no responde 404 por un producto oculto: devuelve cada producto con su motivo,
    // para que Pedidos pueda explicarle al cliente qué producto dejó de estar disponible y por qué
    @Transactional(readOnly = true)
    public VerificacionDisponibilidadResponseDTO verificarDisponibilidad(List<Long> productoIds) {
        if (productoIds == null || productoIds.isEmpty()) {
            throw new ReglaDeNegocioException("ids es obligatorio: indicar al menos un producto");
        }
        if (productoIds.stream().anyMatch(id -> id == null)) {
            throw new ReglaDeNegocioException("ids no puede tener valores vacíos");
        }
        // Sin repetidos y en el orden pedido
        List<Long> pedidos = new ArrayList<>(new LinkedHashSet<>(productoIds));
        if (pedidos.size() > MAXIMO_PRODUCTOS_A_VERIFICAR) {
            throw new ReglaDeNegocioException("Se pueden verificar hasta " + MAXIMO_PRODUCTOS_A_VERIFICAR
                                              + " productos por consulta");
        }

        Map<Long, Producto> encontrados = productoRepository.findAllById(pedidos).stream()
            .collect(Collectors.toMap(Producto::getId, Function.identity()));

        List<DisponibilidadProductoDTO> resultado = pedidos.stream()
            .map(id -> disponibilidadDe(id, encontrados.get(id)))
            .toList();

        boolean todos = resultado.stream().allMatch(DisponibilidadProductoDTO::isDisponible);
        return new VerificacionDisponibilidadResponseDTO(todos, resultado);
    }

    // Mismo criterio que RN-05 (catálogo y CU-06), pero informando cuál condición falla
    private DisponibilidadProductoDTO disponibilidadDe(Long id, Producto producto) {
        if (producto == null) {
            return new DisponibilidadProductoDTO(id, null, null, null, false,
                                                 DisponibilidadProductoDTO.MOTIVO_INEXISTENTE);
        }
        String motivo;
        if (!Comercio.ESTADO_ACTIVO.equals(producto.getComercio().getEstado())) {
            motivo = DisponibilidadProductoDTO.MOTIVO_COMERCIO_INACTIVO;
        } else if (!Producto.ESTADO_ACTIVO.equals(producto.getEstado())) {
            motivo = DisponibilidadProductoDTO.MOTIVO_PRODUCTO_INACTIVO;
        } else if (!producto.isDisponible()) {
            motivo = DisponibilidadProductoDTO.MOTIVO_NO_DISPONIBLE;
        } else {
            motivo = DisponibilidadProductoDTO.MOTIVO_DISPONIBLE;
        }
        return new DisponibilidadProductoDTO(id, producto.getNombre(), producto.getPrecio(),
                                             producto.getComercio().getId(),
                                             DisponibilidadProductoDTO.MOTIVO_DISPONIBLE.equals(motivo), motivo);
    }

    // Paso común de CU-03, CU-04 y CU-09: el producto tiene que existir (REP-03, 404), ser de un comercio del
    // usuario (DM-21, 403) y su comercio estar activo (REP-02, RN-02). Como el producto existe, su comercio
    // también: si no está activo es un conflicto de estado, 409 (DM-09)
    private Producto buscarProductoDeComercioActivo(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con id " + productoId));
        controlDeAcceso.verificarPuedeAdministrar(producto.getComercio());

        Long comercioId = producto.getComercio().getId();
        if (comercioRepository.findByIdAndEstado(comercioId, Comercio.ESTADO_ACTIVO).isEmpty()) {
            throw new ComercioInactivoException("El comercio " + comercioId + " del producto está inactivo");
        }

        return producto;
    }

    // RN-03 / REP-06: todas las categorías informadas tienen que existir en el catálogo global.
    // Los ids repetidos se ignoran, para no exigir dos veces la misma categoría
    private List<Categoria> buscarCategorias(List<Long> categoriaIds) {
        Set<Long> pedidas = Set.copyOf(categoriaIds);
        List<Categoria> encontradas = categoriaRepository.findAllById(pedidas);

        if (encontradas.size() < pedidas.size()) {
            Set<Long> existentes = encontradas.stream().map(Categoria::getId).collect(Collectors.toSet());
            String faltantes = pedidas.stream()
                                      .filter(id -> !existentes.contains(id))
                                      .sorted()
                                      .map(String::valueOf)
                                      .collect(Collectors.joining(", "));
            throw new ReglaDeNegocioException("No existen las categorías con id: " + faltantes);
        }

        return new ArrayList<>(encontradas);
    }
}
