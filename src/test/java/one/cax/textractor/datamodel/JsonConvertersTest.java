package one.cax.textractor.datamodel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the JPA attribute converter to ensure it works correctly with different databases.
 */
public class JsonConvertersTest {

    private XDocAttributeConverter xDocAttributeConverter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        xDocAttributeConverter = new XDocAttributeConverter(objectMapper);
    }

    @Test
    void testRoundTripConversion() throws Exception {
        // Create a test XDoc
        XDoc originalXDoc = createTestXDoc();
        
        // Convert XDoc to JSON string
        String jsonString = xDocAttributeConverter.convertToDatabaseColumn(originalXDoc);
        
        // Verify the JSON string is valid
        assertNotNull(jsonString);
        assertTrue(jsonString.contains("\"id\":"));
        assertTrue(jsonString.contains("\"pages\":"));
        
        // Convert JSON string back to XDoc
        XDoc convertedXDoc = xDocAttributeConverter.convertToEntityAttribute(jsonString);
        
        // Verify the converted XDoc matches the original
        assertNotNull(convertedXDoc);
        assertEquals(originalXDoc.getId(), convertedXDoc.getId());
        assertEquals(originalXDoc.getDocTitle(), convertedXDoc.getDocTitle());
        assertEquals(originalXDoc.getFilename(), convertedXDoc.getFilename());
        
        // Verify pages
        assertNotNull(convertedXDoc.getPages());
        assertEquals(originalXDoc.getPages().size(), convertedXDoc.getPages().size());
        
        XPage originalPage = originalXDoc.getPages().get(0);
        XPage convertedPage = convertedXDoc.getPages().get(0);
        
        assertEquals(originalPage.getPageNumber(), convertedPage.getPageNumber());
        assertEquals(originalPage.getText(), convertedPage.getText());
    }
    
    @Test
    void testSpecialCharacters() {
        // Create a test XDoc with special characters
        XDoc xDoc = createTestXDoc();
        
        // Add text with special characters
        XPage page = xDoc.getPages().get(0);
        page.setText("Special characters: \u2019\u2018\u201C\u201D\u2013\u2014\u00A9\u00AE\u2122");
        
        // Test conversion
        AtomicReference<String> jsonString = new AtomicReference<>();
        
        // Should not throw an exception
        assertDoesNotThrow(() -> {
            jsonString.set(xDocAttributeConverter.convertToDatabaseColumn(xDoc));
        });
        
        // Convert back to XDoc
        XDoc convertedXDoc = xDocAttributeConverter.convertToEntityAttribute(jsonString.get());
        
        // Verify the text with special characters was preserved
        assertEquals(
            "Special characters: \u2019\u2018\u201C\u201D\u2013\u2014\u00A9\u00AE\u2122",
            convertedXDoc.getPages().get(0).getText()
        );
    }
    
    @Test
    void testNullHandling() {
        // Test null XDoc
        assertNull(xDocAttributeConverter.convertToDatabaseColumn(null));
        
        // Test null JSON string
        assertNull(xDocAttributeConverter.convertToEntityAttribute(null));
        
        // Test empty JSON string
        assertNull(xDocAttributeConverter.convertToEntityAttribute(""));
    }
    
    private XDoc createTestXDoc() {
        XDoc xDoc = new XDoc();
        UUID id = UUID.randomUUID();
        xDoc.setId(id);
        xDoc.setDocTitle("Test Document");
        xDoc.setFilename("test.pdf");
        
        List<XPage> pages = new ArrayList<>();
        XPage page = new XPage();
        page.setPageNumber(1);
        page.setText("This is test content for page 1");
        pages.add(page);
        
        xDoc.setPages(pages);
        return xDoc;
    }
}
