package com.catalogodeliverygo.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.catalogodeliverygo.demo.Model.Sucursal;

public interface SucursalRepository extends JpaRepository<Sucursal, Integer> {
    
}
