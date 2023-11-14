package app.beautyminder.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

// Custom Deserializer
public class AuthoritiesDeserializer extends JsonDeserializer<Set<String>> {
    @Override
    public Set<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Set<String> authorities = new HashSet<>();
        JsonNode node = p.getCodec().readTree(p);
        if (node.isArray()) {
            for (JsonNode element : node) {
                JsonNode authorityNode = element.get("authority");
                if (authorityNode != null) {
                    authorities.add(authorityNode.asText());
                }
            }
        }
        return authorities;
    }
}