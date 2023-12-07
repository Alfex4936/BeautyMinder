package app.beautyminder.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
public class OpenAPIConfig {

    @Value("${server.https-text}")
    private String server;

    @Bean
    public OpenAPI customOpenAPI() {
        String jwtSchemeName = "jwtAuth";
        // API 요청헤더에 인증정보 포함
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        // SecuritySchemes 등록
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer")
                        .bearerFormat("JWT")); // 토큰 형식을 지정하는 임의의 문자(Optional)

        return new OpenAPI()
                .info(new Info()
                        .title("BeautyMinder")
                        .version("v" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        .license(new License().name("MIT License"))
                        .contact(new Contact().email("ikr@kakao.com"))
                        .description("2023-2 Capstone Design")
                )
                .addSecurityItem(securityRequirement)
                .components(components)
                .addServersItem(new Server().url(server).description("AWS EC2"));
    }
}
