package com.catalogodeliverygo.demo.Secutiry;

import com.catalogodeliverygo.demo.Exception.AccesoNoAutorizadoException;
import com.catalogodeliverygo.demo.Model.Comercio;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/*
 * DM-21: tener rol COMERCIO no alcanza para modificar cualquier comercio. SecurityConfig solo mira el rol;
 * esta clase mira de quién es el recurso. Un COMERCIO solo puede escribir sobre los comercios cuyo
 * usuarioResponsable es el "sub" de su token, y sobre los productos y sucursales de esos comercios.
 * ADMIN puede escribir sobre cualquiera.
 */
@Component
public class ControlDeAcceso {

    static final String ROL_ADMIN = "ROLE_ADMIN";

    // El "sub" del token que validó JwtAuthenticationFilter; null si la request no trae token
    public String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof String usuario)) {
            return null;
        }
        return usuario;
    }

    public boolean esAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                                   .anyMatch(a -> ROL_ADMIN.equals(a.getAuthority()));
    }

    // 403 si el usuario no es ADMIN ni el responsable del comercio. Un comercio sin responsable
    // (por ejemplo, uno que dio de alta un ADMIN sin indicarlo) solo lo puede modificar un ADMIN
    public void verificarPuedeAdministrar(Comercio comercio) {
        if (esAdmin()) {
            return;
        }
        String usuario = usuarioActual();
        if (usuario == null || !usuario.equals(comercio.getUsuarioResponsable())) {
            throw new AccesoNoAutorizadoException("El comercio " + comercio.getId()
                                                  + " no pertenece al usuario autenticado");
        }
    }
}
