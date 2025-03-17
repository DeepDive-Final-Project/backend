package com.goorm.team9.icontact.config.swagger;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local 개발 서버"),
                        new Server().url("https://www.i-contacts.link").description("배포 서버"),
                        new Server().url("http://3.34.165.63:8080").description("배포 서버 A"),
                        new Server().url("http://43.201.245.222:8080").description("배포 서버 B")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("Team-9 / I-Contact")
                .description("9팀 I-Contact API 명세서")
                .version("1.0.0");
    }
}
