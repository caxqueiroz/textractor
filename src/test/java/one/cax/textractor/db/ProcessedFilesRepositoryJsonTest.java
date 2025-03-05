package one.cax.textractor.db;

import one.cax.textractor.datamodel.XDoc;
import one.cax.textractor.datamodel.XPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ProcessedFilesRepository focusing on JSON handling.
 * Uses H2 database in PostgreSQL mode for testing.
 */
@DataJpaTest
@ActiveProfiles("h2")
@ContextConfiguration(classes = {TestDatabaseConfig.class})
public class ProcessedFilesRepositoryJsonTest {

    @Autowired
    private ProcessedFilesRepository processedFilesRepository;

    @Test
    void testSaveAndRetrieveWithJsonContent() {
        // Create a test ProcessedFiles entity with XDoc content
        ProcessedFiles processedFile = new ProcessedFiles();
        UUID fileId = UUID.randomUUID();
        processedFile.setFileId(fileId);
        processedFile.setFileHash("test-hash-json");
        processedFile.setFileName("test-file-json.pdf");
        processedFile.setFilePath("/path/to/test-file-json.pdf");
        processedFile.setFileSize(1024);
        processedFile.setAppId(UUID.randomUUID());
        processedFile.setProcessingStatus("COMPLETED");
        
        // Create XDoc with pages
        XDoc xDoc = new XDoc();
        xDoc.setId(fileId);
        xDoc.setDocTitle("Test JSON Document");
        xDoc.setFilename("test-file-json.pdf");
        
        List<XPage> pages = new ArrayList<>();
        XPage page1 = new XPage();
        page1.setPageNumber(1);
        page1.setText("This is page 1 content");
        pages.add(page1);
        
        XPage page2 = new XPage();
        page2.setPageNumber(2);
        page2.setText("This is page 2 content");
        pages.add(page2);
        
        xDoc.setPages(pages);
        
        // Set the XDoc content
        processedFile.setOcrContent(xDoc);
        
        // Save the entity
        processedFilesRepository.save(processedFile);
        
        // Retrieve the entity
        Optional<ProcessedFiles> retrievedOpt = processedFilesRepository.findById(fileId);
        assertTrue(retrievedOpt.isPresent());
        
        ProcessedFiles retrieved = retrievedOpt.get();
        
        // Verify the XDoc content was correctly saved and retrieved
        XDoc retrievedXDoc = retrieved.getOcrContent();
        assertNotNull(retrievedXDoc);
        assertEquals(fileId, retrievedXDoc.getId());
        assertEquals("Test JSON Document", retrievedXDoc.getDocTitle());
        
        // Verify pages
        List<XPage> retrievedPages = retrievedXDoc.getPages();
        assertEquals(2, retrievedPages.size());
        
        XPage retrievedPage1 = retrievedPages.get(0);
        assertEquals(1, retrievedPage1.getPageNumber());
        assertEquals("This is page 1 content", retrievedPage1.getText());
        
        XPage retrievedPage2 = retrievedPages.get(1);
        assertEquals(2, retrievedPage2.getPageNumber());
        assertEquals("This is page 2 content", retrievedPage2.getText());
    }
}
