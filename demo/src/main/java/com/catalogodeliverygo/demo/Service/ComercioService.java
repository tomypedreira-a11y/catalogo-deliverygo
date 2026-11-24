package com.catalogodeliverygo.demo.Service;

import com.catalogodeliverygo.demo.DTOs.ComercioResponseDTO;
import com.catalogodeliverygo.demo.DTOs.CrearComercioRequestDTO;
import com.catalogodeliverygo.demo.DTOs.ModificarComercioRequestDTO;
import com.catalogodeliverygo.demo.Exception.RecursoDuplicadoException;
import com.catalogodeliverygo.demo.Exception.RecursoNoEncontradoException;
import com.catalogodeliverygo.demo.Mapper.DtoMapper;
import com.catalogodeliverygo.demo.Model.Comercio;
import com.catalogodeliverygo.demo.Repository.ComercioRepository;
import com.catalogodeliverygo.demo.Secutiry.ControlDeAcceso;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ComercioService {

    private final ComercioRepository comercioRepository;
    private final DtoMapper mapper;
    private final ControlDeAcceso controlDeAcceso;

    public ComercioService(ComercioRepository comercioRepository, DtoMapper mapper, ControlDeAcceso controlDeAcceso) {
        this.comercioRepository = comercioRepository;
        this.mapper = mapper;
        this.controlDeAcceso = controlDeAcceso;
    }

    // SRV-01 / CU-01: verifica unicidad de CUIT (RN-01, REP-01) y persiste con estado ACTIVO.
    // El token y el rol ya los validó SecurityConfig antes de llegar acá (DM-06, INT-01).
    // Los datos obligatorios (RN-06) los valida Bean Validation en el DTO
    public ComercioResponseDTO registrarComercio(CrearComercioRequestDTO request) {
        if (comercioRepository.findByCuit(request.getCuit()).isPresent()) {
            throw new RecursoDuplicadoException("Ya existe un comercio con el CUIT " + request.getCuit());
        }

        Comercio comercio = new Comercio(request.getCuit(),
                                         request.getRazonSocial(),
                                         request.getDireccion(),
                                         request.getRubro(),
                                         Comercio.ESTADO_ACTIVO,
                                         responsableDelAlta(request));

        try {
            // saveAndFlush para que, si otro pedido registró el mismo CUIT entre la búsqueda y el guardado,
            // la restricción unique de la columna salte acá y se informe como 409, no como 500
            return mapper.toComercioResponse(comercioRepository.saveAndFlush(comercio));
        } catch (DataIntegrityViolationException ex) {
            throw new RecursoDuplicadoException("Ya existe un comercio con el CUIT " + request.getCuit());
        }
    }

    // DM-21: quien registra con rol COMERCIO queda como responsable. Un ADMIN puede indicar otro usuario
    // en usuarioResponsable; si no lo indica, el comercio queda sin responsable y solo lo modifica un ADMIN
    private String responsableDelAlta(CrearComercioRequestDTO request) {
        if (controlDeAcceso.esAdmin()) {
            String indicado = request.getUsuarioResponsable();
            return (indicado == null || indicado.isBlank()) ? null : indicado.trim();
        }
        return controlDeAcceso.usuarioActual();
    }

    // API-15 / DM-21: los comercios del usuario autenticado, para que el Portal sepa cuáles puede administrar
    @Transactional(readOnly = true)
    public List<ComercioResponseDTO> listarMisComercios() {
        return comercioRepository.findByUsuarioResponsableOrderById(controlDeAcceso.usuarioActual()).stream()
                                 .map(mapper::toComercioResponse)
                                 .toList();
    }

    // API-14 / INT-03: devuelve el comercio sin filtrar por estado. Los otros módulos lo usan para validarlo,
    // así que un comercio INACTIVO responde 200 con su estado, no 404 como en el catálogo (DM-18)
    @Transactional(readOnly = true)
    public ComercioResponseDTO obtenerComercio(Long comercioId) {
        Comercio comercio = comercioRepository.findById(comercioId)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el comercio con id " + comercioId));

        return mapper.toComercioResponse(comercio);
    }

    // CU-10: busca el comercio y actualiza razón social, dirección y rubro. El CUIT no cambia (RN-01): ni siquiera
    // está en el DTO. La Primera Entrega asocia CU-10 solo a RN-01, por eso no se exige comercio activo.
    // Solo el responsable del comercio o un ADMIN lo pueden modificar (DM-21): si no, 403
    public ComercioResponseDTO modificarComercio(Long comercioId, ModificarComercioRequestDTO request) {
        Comercio comercio = comercioRepository.findById(comercioId)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el comercio con id " + comercioId));
        controlDeAcceso.verificarPuedeAdministrar(comercio);

        comercio.setRazonSocial(request.getRazonSocial());
        comercio.setDireccion(request.getDireccion());
        comercio.setRubro(request.getRubro());

        return mapper.toComercioResponse(comercioRepository.save(comercio));
    }
}
