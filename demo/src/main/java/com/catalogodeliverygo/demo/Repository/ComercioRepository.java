package com.catalogodeliverygo.demo.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.catalogodeliverygo.demo.Model.Comercio;

public interface ComercioRepository extends JpaRepository<Comercio, Long>{

    public Optional<Comercio> findByCuit(String cuit);
    public Optional<Comercio> findByIdAndEstado(Long id, String estado);
    
} 
