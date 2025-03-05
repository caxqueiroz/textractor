package one.cax.textractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import one.cax.textractor.config.JacksonConfig;
import one.cax.textractor.datamodel.XDoc;
import one.cax.textractor.datamodel.XDocAttributeConverter;
import one.cax.textractor.datamodel.XPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test to verify character encoding issues are resolved.
 */
@SpringBootTest
@Import(JacksonConfig.class)
public class CharacterEncodingTest {

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private XDocAttributeConverter xDocAttributeConverter;

    @Test
    void testParagraphSeparatorCharacter() {
        // Create a test XDoc with problematic characters
        XDoc xDoc = new XDoc();
        xDoc.setId(UUID.randomUUID());
        xDoc.setDocTitle("Test Document with Special Characters");
        xDoc.setFilename("test-special-chars.pdf");
        
        List<XPage> pages = new ArrayList<>();
        XPage page = new XPage();
        page.setPageNumber(1);
        
        // Add text with paragraph separator character (U+2029)
        String textWithParagraphSeparator = "This is a test with paragraph separator.\u2029Next paragraph.";
        page.setText(textWithParagraphSeparator);
        pages.add(page);
        
        xDoc.setPages(pages);
        
        // Test conversion - should not throw an exception
        String json = assertDoesNotThrow(() -> xDocAttributeConverter.convertToDatabaseColumn(xDoc));
        
        // Verify the JSON contains the escaped character
        assertTrue(json.contains("\\u2029") || json.contains("paragraph separator"));
        
        // Print the JSON for debugging
        System.out.println("JSON with special character: " + json);
    }
}
