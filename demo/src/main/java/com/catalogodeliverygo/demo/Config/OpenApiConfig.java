package com.catalogodeliverygo.demo.Config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "DeliveryGo - Módulo Comercios y Catálogo",
        version = "v1",
        description = "API del módulo Comercios y Catálogo (Grupo 07). Administra comercios, productos, precios y "
                    + "disponibilidad, y expone el catálogo que consumen Pedidos y el Portal Web."
    ),
    // Fija la URL para que docs/openapi.yml salga igual sin importar el puerto con que se genere
    servers = @Server(url = "http://localhost:8080", description = "Entorno local")
)
// DM-06: el token lo emite el módulo Usuarios y Catálogo lo valida vía INT-01
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    description = "Token emitido por el módulo Usuarios al autenticarse el comerciante (INT-01)"
)
public class OpenApiConfig {
}
