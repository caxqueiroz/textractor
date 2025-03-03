package one.cax.textractor.text;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ExtractorEngine.
 * 
 * These tests focus on verifying that the ExtractorEngine correctly registers metrics
 * and handles exceptions properly.
 */
@ExtendWith(SpringExtension.class)
class ExtractorEngineIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    private MeterRegistry meterRegistry;
    private ExtractorEngine extractorEngine;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        extractorEngine = new ExtractorEngine(meterRegistry);
    }

    /**
     * Test that the extraction metrics are properly registered.
     */
    @Test
    void verifyMetricsAreRegistered() {
        // This test verifies that the metrics are properly registered with the MeterRegistry
        
        // Assert that the metrics are registered with the SimpleMeterRegistry
        assertNotNull(meterRegistry.find("ExtractText").timer());
        assertNotNull(meterRegistry.find("successfulExtracts").counter());
    }

    /**
     * Test that the DocumentExtractionException is properly thrown for invalid files.
     */
    @Test
    void extractTextFromPDF_WithInvalidFile() throws Exception {
        // Arrange - use a non-existent file path
        String invalidPath = "non-existent-file.pdf";
        
        // Act & Assert
        DocumentExtractionException exception = assertThrows(
            DocumentExtractionException.class,
            () -> extractorEngine.extractTextFromPDF(invalidPath)
        );
        
        assertTrue(exception.getMessage().contains("Error extracting text from PDF"));
    }

    /**
     * Test that the DocumentExtractionException is properly thrown for invalid office docs.
     */
    @Test
    void extractTextFromOfficeDocs_WithInvalidFile() throws Exception {
        // Arrange - use a non-existent file path
        String invalidPath = "non-existent-file.docx";
        
        // Act & Assert
        DocumentExtractionException exception = assertThrows(
            DocumentExtractionException.class,
            () -> extractorEngine.extractTextFromOfficeDocs(invalidPath)
        );
        
        assertTrue(exception.getMessage().contains("Failed to convert DOCX to PDF"));
    }
}
