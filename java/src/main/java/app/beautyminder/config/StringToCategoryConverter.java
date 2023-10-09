package app.beautyminder.config;

import app.beautyminder.domain.Cosmetic;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class StringToCategoryConverter implements Converter<String, Cosmetic.Category> {

    @Override
    public Cosmetic.Category convert(@NotNull String source) {
        return Cosmetic.Category.fromDisplayName(source);
    }
}