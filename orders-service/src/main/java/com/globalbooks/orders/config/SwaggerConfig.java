package com.globalbooks.orders.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GlobalBooks Orders Service API")
                        .version("1.0.0")
                        .description("REST API for managing orders in GlobalBooks e-commerce platform")
                        .contact(new Contact()
                                .name("GlobalBooks Team")
                                .email("api@globalbooks.com")));
    }
}