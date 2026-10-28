package com.catalogodeliverygo.demo.Exception;

// DM-13: se intenta borrar una categoría que todavía tiene productos asociados (RN-03)
public class CategoriaEnUsoException extends RuntimeException {

    public CategoriaEnUsoException(String mensaje) {
        super(mensaje);
    }
}
