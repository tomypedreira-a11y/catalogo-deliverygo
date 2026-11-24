package com.catalogodeliverygo.demo.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.catalogodeliverygo.demo.Model.Comercio;

public interface ComercioRepository extends JpaRepository<Comercio, Long>{

    public Optional<Comercio> findByCuit(String cuit);
    public Optional<Comercio> findByIdAndEstado(Long id, String estado);

    // API-15 / DM-21: comercios de los que el usuario es responsable
    public List<Comercio> findByUsuarioResponsableOrderById(String usuarioResponsable);
    
} 
