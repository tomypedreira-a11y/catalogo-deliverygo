package com.catalogodeliverygo.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.catalogodeliverygo.demo.Model.Sucursal;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {

    // CU-07 / REL-01: sucursales de un comercio
    public List<Sucursal> findByComercioId(Long comercioId);
}
