package com.catalogodeliverygo.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.catalogodeliverygo.demo.Model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
