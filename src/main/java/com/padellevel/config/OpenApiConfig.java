package com.padellevel.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación de la API REST.
 *
 * La documentación estará disponible en:
 * - Swagger UI: /swagger-ui/index.html
 * - OpenAPI JSON: /v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Padel Club API")
                .version("1.0.0")
                .description("API REST para la gestión de torneos de pádel, inscripciones, gamificación y estadísticas. " +
                           "Esta API permite a aplicaciones externas interactuar con la plataforma de gestión de torneos.")
                .contact(new Contact()
                    .name("Padel Club Support")
                    .email("support@padelclub.pro")
                    .url("https://padelclub.pro"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Servidor de desarrollo local"),
                new Server()
                    .url("https://api.padelclub.pro")
                    .description("Servidor de producción")
            ));
    }
}
