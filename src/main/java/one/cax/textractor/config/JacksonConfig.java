package one.cax.textractor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.nio.charset.StandardCharsets;

/**
 * Configuration for Jackson ObjectMapper to ensure proper handling of UTF-8 encoding.
 * This is particularly important for handling JSON data with special characters.
 */
@Configuration
public class JacksonConfig {

    /**
     * Creates a custom ObjectMapper with UTF-8 encoding configuration.
     * 
     * @return A configured ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        
        ObjectMapper objectMapper = builder.build();
        
        // Configure for UTF-8 encoding
        objectMapper.getFactory().setCharacterEscapes(new Utf8CharacterEscapes());
        
        return objectMapper;
    }
}
