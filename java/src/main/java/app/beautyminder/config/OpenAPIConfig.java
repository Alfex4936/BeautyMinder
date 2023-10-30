package app.beautyminder.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BeautyMinder")
                        .version("v" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        .license(new License().name("MIT License"))
                        .contact(new Contact().email("ikr@kakao.com"))
                        .description("2023-2 Capstone Design")
                )
                .addServersItem(new Server().url("http://localhost:8080").description("AWS EC2"));
    }
}
