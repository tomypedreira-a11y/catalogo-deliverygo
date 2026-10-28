package com.catalogodeliverygo.demo.Service;

import com.catalogodeliverygo.demo.DTOs.CategoriaDTO;
import com.catalogodeliverygo.demo.DTOs.CategoriaRequestDTO;
import com.catalogodeliverygo.demo.Exception.CategoriaEnUsoException;
import com.catalogodeliverygo.demo.Exception.RecursoNoEncontradoException;
import com.catalogodeliverygo.demo.Mapper.DtoMapper;
import com.catalogodeliverygo.demo.Model.Categoria;
import com.catalogodeliverygo.demo.Repository.CategoriaRepository;
import com.catalogodeliverygo.demo.Repository.ProductoRepository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// CU-08: el rol ADMIN lo exige SecurityConfig antes de llegar acá (DM-02, DM-06)
@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final DtoMapper mapper;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository,
                            DtoMapper mapper) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.mapper = mapper;
    }

    // CU-08: lista el catálogo global de categorías (DM-02), por nombre para mostrarlas ordenadas
    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarCategorias() {
        return categoriaRepository.findAll(Sort.by("nombre")).stream()
                                  .map(mapper::toCategoriaDTO)
                                  .toList();
    }

    // CU-08
    public CategoriaDTO crearCategoria(CategoriaRequestDTO request) {
        return mapper.toCategoriaDTO(categoriaRepository.save(new Categoria(request.getNombre())));
    }

    // CU-08
    public CategoriaDTO modificarCategoria(Long categoriaId, CategoriaRequestDTO request) {
        Categoria categoria = buscarCategoria(categoriaId);
        categoria.setNombre(request.getNombre());
        return mapper.toCategoriaDTO(categoriaRepository.save(categoria));
    }

    // CU-08: rechaza la baja si la categoría tiene productos asociados (DM-13, RN-03).
    // Cuenta también los productos INACTIVOS, porque siguen existiendo y se pueden reactivar
    public void eliminarCategoria(Long categoriaId) {
        Categoria categoria = buscarCategoria(categoriaId);
        if (productoRepository.existsByCategoriasId(categoriaId)) {
            throw new CategoriaEnUsoException("La categoría " + categoria.getNombre()
                                              + " tiene productos asociados y no se puede eliminar");
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria buscarCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoría con id " + categoriaId));
    }
}
