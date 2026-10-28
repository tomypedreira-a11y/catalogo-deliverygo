package com.catalogodeliverygo.demo.Mapper;

import com.catalogodeliverygo.demo.DTOs.CatalogoResponseDTO;
import com.catalogodeliverygo.demo.DTOs.CategoriaDTO;
import com.catalogodeliverygo.demo.DTOs.ComercioResponseDTO;
import com.catalogodeliverygo.demo.DTOs.ProductoResponseDTO;
import com.catalogodeliverygo.demo.DTOs.SucursalDTO;
import com.catalogodeliverygo.demo.Model.Categoria;
import com.catalogodeliverygo.demo.Model.Comercio;
import com.catalogodeliverygo.demo.Model.Producto;
import com.catalogodeliverygo.demo.Model.Sucursal;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

// Convierte entidades en DTOs de salida, para no exponer el modelo en la API (capa DTO, sección 9 de la Primera Entrega)
@Component
public class DtoMapper {

    // DTO-02
    public ComercioResponseDTO toComercioResponse(Comercio comercio) {
        return new ComercioResponseDTO(comercio.getId(),
                                       comercio.getCuit(),
                                       comercio.getRazonSocial(),
                                       comercio.getEstado());
    }

    // DTO-04 (DM-01, DM-12)
    public ProductoResponseDTO toProductoResponse(Producto producto) {
        List<CategoriaDTO> categorias = producto.getCategorias().stream()
                                                .map(this::toCategoriaDTO)
                                                .toList();

        return new ProductoResponseDTO(producto.getId(),
                                       producto.getNombre(),
                                       producto.getPrecio(),
                                       producto.isDisponible(),
                                       producto.getEstado(),
                                       producto.getComercio().getId(),
                                       categorias);
    }

    // DTO-07: page es 0-based, igual que el parámetro de API-05
    public CatalogoResponseDTO toCatalogoResponse(Page<Producto> pagina) {
        List<ProductoResponseDTO> items = pagina.getContent().stream()
                                                .map(this::toProductoResponse)
                                                .toList();

        return new CatalogoResponseDTO(items,
                                       pagina.getNumber(),
                                       pagina.getSize(),
                                       pagina.getTotalElements());
    }

    public CategoriaDTO toCategoriaDTO(Categoria categoria) {
        return new CategoriaDTO(categoria.getId(), categoria.getNombre());
    }

    public SucursalDTO toSucursalDTO(Sucursal sucursal) {
        return new SucursalDTO(sucursal.getId(),
                               sucursal.getDireccion(),
                               sucursal.getComercio().getId());
    }
}
