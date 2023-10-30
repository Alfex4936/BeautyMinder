package app.beautyminder;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDate;

// @OpenAPIDefinition()
@EnableScheduling
@EnableMongoAuditing
@SpringBootApplication(exclude = {ElasticsearchDataAutoConfiguration.class, ElasticsearchRestClientAutoConfiguration.class})
//@ComponentScan(excludeFilters =
//        {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = EsCosmeticRepository.class)})

public class BeautyMinderApplication {
    public static void main(String[] args) {
        SpringApplication.run(BeautyMinderApplication.class, args);
    }
}

