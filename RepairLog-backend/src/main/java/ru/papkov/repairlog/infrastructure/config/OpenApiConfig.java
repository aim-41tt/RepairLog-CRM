package ru.papkov.repairlog.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Swagger / OpenAPI для документирования REST API.
 *
 * @author aim-41tt
 */
@Configuration
public class OpenApiConfig {

    @Bean
    protected OpenAPI repairLogOpenApi() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("CRM RepairLog API")
                        .description("REST API для системы управления сервисным центром")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("aim-41tt")
                                .url("https://github.com/aim-41tt")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT токен авторизации. Получите через POST /api/auth/login")));
    }
}
