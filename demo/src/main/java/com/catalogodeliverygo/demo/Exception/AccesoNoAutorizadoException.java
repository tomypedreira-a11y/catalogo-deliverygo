package com.catalogodeliverygo.demo.Exception;

public class AccesoNoAutorizadoException extends RuntimeException {

    public AccesoNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}

