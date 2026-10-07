package com.catalogodeliverygo.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.catalogodeliverygo.demo.Model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    
}
