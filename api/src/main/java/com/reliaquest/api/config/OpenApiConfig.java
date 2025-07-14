package com.reliaquest.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for the Employee API.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8111}")
    private String serverPort;

    @Bean
    public OpenAPI employeeOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:" + serverPort);
        devServer.setDescription("Development server");

        Contact contact = new Contact();
        contact.setName("ReliaQuest API Team");
        contact.setEmail("api-team@reliaquest.com");

        License license = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Employee Management API")
                .version("1.0.0")
                .contact(contact)
                .description("A comprehensive API for managing employee data with full CRUD operations, "
                        + "search capabilities, and advanced analytics features.")
                .license(license);

        return new OpenAPI().info(info).servers(List.of(devServer));
    }
}
