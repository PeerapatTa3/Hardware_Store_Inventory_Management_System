package com.hardwarestore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hardwareStoreOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Hardware Store Inventory API")
                .version("v1")
                .description("REST API for hardware store inventory management"));
    }
}