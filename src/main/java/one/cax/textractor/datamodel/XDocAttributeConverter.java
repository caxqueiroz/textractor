package one.cax.textractor.datamodel;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;

/**
 * JPA attribute converter for XDoc objects.
 * Converts XDoc objects to/from JSON strings for database storage.
 * This converter is database-agnostic and works with any database that supports JSON/JSONB types.
 */
@Converter(autoApply = false)
public class XDocAttributeConverter implements AttributeConverter<XDoc, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(XDocAttributeConverter.class);
    private final ObjectMapper objectMapper;
    
    @Autowired
    public XDocAttributeConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String convertToDatabaseColumn(XDoc attribute) {
        if (attribute == null) {
            return null;
        }
        
        try {
            // Serialize to JSON with proper character escaping
            String json = objectMapper.writeValueAsString(attribute);
            
            // Ensure the string is properly encoded as UTF-8
            byte[] utf8Bytes = json.getBytes(StandardCharsets.UTF_8);
            String utf8String = new String(utf8Bytes, StandardCharsets.UTF_8);
            
            // Log at debug level to avoid excessive logging
            logger.debug("Converting XDoc to JSON: {}", utf8String);
            
            return utf8String;
        } catch (Exception e) {
            logger.error("Error converting XDoc to JSON", e);
            throw new RuntimeException("Error converting XDoc to database column", e);
        }
    }
    
    @Override
    public XDoc convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        
        try {
            // Log at debug level to avoid excessive logging
            logger.debug("Converting JSON to XDoc, length: {}", dbData.length());
            
            // Ensure the input string is properly encoded as UTF-8
            byte[] utf8Bytes = dbData.getBytes(StandardCharsets.UTF_8);
            String utf8String = new String(utf8Bytes, StandardCharsets.UTF_8);
            
            // Use Jackson to deserialize with proper UTF-8 handling
            return objectMapper.readValue(utf8String, XDoc.class);
        } catch (Exception e) {
            logger.error("Error converting JSON to XDoc: {}", e.getMessage());
            throw new RuntimeException("Error converting database column to XDoc", e);
        }
    }
}
