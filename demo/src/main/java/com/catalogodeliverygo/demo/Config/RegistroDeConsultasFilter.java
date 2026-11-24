package com.catalogodeliverygo.demo.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/*
 * DM-23: deja en la consola una línea por cada request a la API, con la IP de quien llama, el módulo que se
 * identifica en el header X-Modulo (por ejemplo "pedidos" o "portal") y el status de la respuesta.
 * Sirve como evidencia de que Portal y Pedidos consultan los datos reales del catálogo, en especial cuando
 * cada módulo corre en otra máquina virtual. Se apaga con LOG_CONSULTAS=false.
 */
@Component
public class RegistroDeConsultasFilter extends OncePerRequestFilter {

    static final String HEADER_MODULO = "X-Modulo";
    private static final Logger log = LoggerFactory.getLogger("INTEGRACION");

    private final boolean activo;

    public RegistroDeConsultasFilter(@Value("${app.integracion.log-consultas:true}") boolean activo) {
        this.activo = activo;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !activo || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        long inicio = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            String query = request.getQueryString();
            log.info("{} {}{} -> {} ({} ms) desde {} modulo={}",
                     request.getMethod(),
                     limpiar(request.getRequestURI(), 200),
                     query == null ? "" : "?" + limpiar(query, 200),
                     response.getStatus(),
                     System.currentTimeMillis() - inicio,
                     request.getRemoteAddr(),
                     modulo(request));
        }
    }

    // El header lo escribe el cliente: se limita a letras, números, guion y guion bajo para que no
    // pueda meter saltos de línea ni texto falso en el log
    static String modulo(HttpServletRequest request) {
        String modulo = request.getHeader(HEADER_MODULO);
        if (modulo == null) {
            return "-";
        }
        String limpio = modulo.replaceAll("[^A-Za-z0-9_-]", "");
        return limpio.isEmpty() ? "-" : limpio.substring(0, Math.min(limpio.length(), 30));
    }

    private static String limpiar(String texto, int largoMaximo) {
        String limpio = texto.replaceAll("[\\r\\n\\t]", "_");
        return limpio.length() > largoMaximo ? limpio.substring(0, largoMaximo) + "..." : limpio;
    }
}
