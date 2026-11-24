package com.catalogodeliverygo.demo.Config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "DeliveryGo - Módulo Comercios y Catálogo",
        version = "v1",
        description = "API del módulo Comercios y Catálogo (Grupo 07). Administra comercios, productos, precios y "
                    + "disponibilidad, y expone el catálogo que consumen Pedidos y el Portal Web."
    )
)
// DM-06: el token lo emite el módulo Usuarios y Catálogo lo valida vía INT-01
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    description = "Token emitido por el módulo Usuarios al autenticarse el comerciante (INT-01)"
)
public class OpenApiConfig {

    /*
     * DM-24: la URL del servidor que muestra Swagger sale de app.openapi.server-url (OPENAPI_SERVER_URL).
     * Por defecto es http://localhost:8082, así doc/openapi.yaml sale igual en cada corrida. En una máquina
     * virtual se define con su IP, por ejemplo http://192.168.56.10:8082, para que "Try it out" desde otra
     * máquina llame al catálogo y no a su propio localhost.
     */
    @Bean
    OpenApiCustomizer servidorConfigurable(@Value("${app.openapi.server-url}") String url) {
        return openApi -> openApi.setServers(List.of(new Server().url(url).description("Servidor del catálogo")));
    }
}
