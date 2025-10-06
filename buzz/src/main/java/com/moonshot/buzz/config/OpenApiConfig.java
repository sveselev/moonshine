package com.moonshot.buzz.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    private final String moduleName;
    private final String apiVersion;
    private final String apiDescription;

    public OpenApiConfig(
            @Value("${module.name:Buzz}") String moduleName,
            @Value("${api.version:0.0.1}") String apiVersion,
            @Value("${api.description:Classify sentiment and emotion from text snippet}") String apiDescription) {
        this.moduleName = moduleName;
        this.apiVersion = apiVersion;
        this.apiDescription = apiDescription;
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title(moduleName + " API")
                .version(apiVersion)
                .description(apiDescription)
                .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0"))
                .contact(new Contact().name("Moonshine Team").url("https://github.com/sveselev/moonshine"))
            );
    }
}
