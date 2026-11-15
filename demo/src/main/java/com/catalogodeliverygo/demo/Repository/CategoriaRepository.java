package com.catalogodeliverygo.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.catalogodeliverygo.demo.Model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // DM-20: unicidad del nombre sin distinguir mayúsculas. El Service recorta los espacios antes de consultar
    boolean existsByNombreIgnoreCase(String nombre);

    // DM-20: igual que el anterior, pero excluye la propia categoría al modificarla
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
