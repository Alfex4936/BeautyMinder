package app.beautyminder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

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

