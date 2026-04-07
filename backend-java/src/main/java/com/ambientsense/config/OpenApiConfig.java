package com.ambientsense.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ambientSenseOpenApi() {
        return new OpenAPI()
                .servers(List.of(new Server().url("http://localhost:8080").description("Local")))
                .info(new Info()
                        .title("AmbientSense API")
                        .version("v1")
                        .description("Leituras, histórico e estado do mock JSONL (MVP)."));
    }
}
