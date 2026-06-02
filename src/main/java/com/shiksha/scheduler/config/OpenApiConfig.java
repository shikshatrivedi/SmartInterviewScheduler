package com.shiksha.scheduler.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartSchedulerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Interview Scheduler API")
                        .description("Enterprise-grade interview scheduling and candidate management REST API. " +
                                "Supports role-based access: HR, Interviewer, Candidate, Admin.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Shiksha — Developer")
                                .email("admin@smartscheduler.com"))
                        .license(new License().name("MIT License")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token obtained from /auth/login")));
    }
}
