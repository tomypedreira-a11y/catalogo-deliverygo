package com.catalogodeliverygo.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.catalogodeliverygo.demo.Model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    
}
