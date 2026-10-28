package com.catalogodeliverygo.demo.Service;

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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SucursalService {

    private final SucursalRepository sucursalRepository;
    private final ComercioRepository comercioRepository;
    private final DtoMapper mapper;

    public SucursalService(SucursalRepository sucursalRepository, ComercioRepository comercioRepository,
                           DtoMapper mapper) {
        this.sucursalRepository = sucursalRepository;
        this.comercioRepository = comercioRepository;
        this.mapper = mapper;
    }

    // CU-07: lista las sucursales del comercio (REL-01). Es una lectura pública: no exige comercio activo
    @Transactional(readOnly = true)
    public List<SucursalDTO> listarSucursales(Long comercioId) {
        buscarComercio(comercioId);
        return sucursalRepository.findByComercioId(comercioId).stream()
                                 .map(mapper::toSucursalDTO)
                                 .toList();
    }

    // CU-07: valida comercio activo (RN-02) y crea la sucursal
    public SucursalDTO crearSucursal(Long comercioId, SucursalRequestDTO request) {
        Comercio comercio = buscarComercioActivo(comercioId);
        Sucursal sucursal = new Sucursal(comercio, request.getDireccion());
        return mapper.toSucursalDTO(sucursalRepository.save(sucursal));
    }

    // CU-07: valida comercio activo (RN-02) y que la sucursal pertenezca al comercio
    public SucursalDTO modificarSucursal(Long comercioId, Long sucursalId, SucursalRequestDTO request) {
        buscarComercioActivo(comercioId);
        Sucursal sucursal = buscarSucursalDelComercio(comercioId, sucursalId);
        sucursal.setDireccion(request.getDireccion());
        return mapper.toSucursalDTO(sucursalRepository.save(sucursal));
    }

    // CU-07: valida comercio activo (RN-02) y que la sucursal pertenezca al comercio
    public void eliminarSucursal(Long comercioId, Long sucursalId) {
        buscarComercioActivo(comercioId);
        sucursalRepository.delete(buscarSucursalDelComercio(comercioId, sucursalId));
    }

    private Comercio buscarComercio(Long comercioId) {
        return comercioRepository.findById(comercioId)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el comercio con id " + comercioId));
    }

    // RN-02: comercio inexistente -> 404; existente pero inactivo -> 409
    private Comercio buscarComercioActivo(Long comercioId) {
        Comercio comercio = buscarComercio(comercioId);
        if (!Comercio.ESTADO_ACTIVO.equals(comercio.getEstado())) {
            throw new ComercioInactivoException("El comercio " + comercioId + " está inactivo");
        }
        return comercio;
    }

    // REL-01: la sucursal tiene que ser de ese comercio; si es de otro responde 404, igual que si no existiera,
    // para que no se pueda tocar la sucursal de otro comercio cambiando el id de la URL
    private Sucursal buscarSucursalDelComercio(Long comercioId, Long sucursalId) {
        return sucursalRepository.findById(sucursalId)
            .filter(s -> comercioId.equals(s.getComercio().getId()))
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No existe la sucursal " + sucursalId + " en el comercio " + comercioId));
    }
}
