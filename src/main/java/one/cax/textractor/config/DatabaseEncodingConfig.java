package one.cax.textractor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Configuration class to ensure proper character encoding for database connections.
 * This is particularly important for handling JSON data with special characters.
 */
@Configuration
public class DatabaseEncodingConfig {

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    /**
     * Customizes the data source properties to ensure UTF-8 encoding.
     * 
     * @param properties The original data source properties
     * @return The customized data source with proper encoding
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.datasource.url")
    public DataSource dataSource(DataSourceProperties properties) {
        // Ensure the connection uses UTF-8 encoding
        if (properties.getUrl() != null && !properties.getUrl().contains("characterEncoding")) {
            String url = properties.getUrl();
            if (url.contains("?")) {
                properties.setUrl(url + "&characterEncoding=UTF-8");
            } else {
                properties.setUrl(url + "?characterEncoding=UTF-8");
            }
        }
        
        // Create the data source
        HikariDataSource dataSource = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        
        // Set additional connection properties for proper encoding
        dataSource.addDataSourceProperty("characterEncoding", StandardCharsets.UTF_8.name());
        dataSource.addDataSourceProperty("useUnicode", "true");
        
        return dataSource;
    }
}
