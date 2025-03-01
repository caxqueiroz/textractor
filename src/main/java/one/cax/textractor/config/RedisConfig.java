package one.cax.textractor.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import one.cax.textractor.datamodel.FileProcessing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfig {

    /**
     * Redis template for FileProcessing objects.
     * @param connectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<String, FileProcessing> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, FileProcessing> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        template.setHashKeySerializer(new StringRedisSerializer());
        ObjectMapper objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        Jackson2JsonRedisSerializer<FileProcessing> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, FileProcessing.class);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        return template;
    }

    /**
     * Redis container for listening to ocr topic.
     * @param connectionFactory
     * @return
     */
    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}