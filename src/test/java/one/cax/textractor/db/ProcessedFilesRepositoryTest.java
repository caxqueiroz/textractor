package one.cax.textractor.db;

import one.cax.textractor.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"  // Disable automatic schema initialization
})
class ProcessedFilesRepositoryTest {

    @Autowired
    private ProcessedFilesRepository processedFilesRepository;

    @Test
    @Sql("/sql/create-processed-files-h2.sql")
    void testFindByFileHash() {
        // Arrange
        String fileHash = "test-hash-123";
        
        // Act
        Optional<ProcessedFiles> result = processedFilesRepository.findByFileHash(fileHash);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(fileHash, result.get().getFileHash());
        assertEquals("test-file.pdf", result.get().getFileName());
    }

    @Test
    @Sql("/sql/create-processed-files-h2.sql")
    void testFindByFileHashNotFound() {
        // Arrange
        String fileHash = "non-existent-hash";
        
        // Act
        Optional<ProcessedFiles> result = processedFilesRepository.findByFileHash(fileHash);
        
        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @Sql("/sql/create-processed-files-h2.sql")
    void testFindByAppId() {
        // Arrange
        UUID appId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        
        // Act
        Iterable<ProcessedFiles> results = processedFilesRepository.findByAppId(appId);
        
        // Assert
        assertNotNull(results);
        assertTrue(results.iterator().hasNext());
        
        // Verify all returned files have the correct appId
        for (ProcessedFiles file : results) {
            assertEquals(appId, file.getAppId());
        }
    }

    @Test
    @Sql("/sql/create-processed-files-h2.sql")
    void testSaveAndFindById() {
        // Arrange
        String newFileName = "updated-test-file.pdf";
        String newFilePath = "/path/to/updated-file.pdf";
        
        // Find an existing file to modify
        Optional<ProcessedFiles> existingFile = processedFilesRepository.findByFileHash("test-hash-123");
        assertTrue(existingFile.isPresent(), "Test file should exist");
        
        ProcessedFiles fileToUpdate = existingFile.get();
        fileToUpdate.setFileName(newFileName);
        fileToUpdate.setFilePath(newFilePath);
        
        // Act
        ProcessedFiles savedFile = processedFilesRepository.save(fileToUpdate);
        Optional<ProcessedFiles> retrievedFile = processedFilesRepository.findById(savedFile.getFileId());
        
        // Assert
        assertNotNull(savedFile);
        assertTrue(retrievedFile.isPresent());
        assertEquals(newFileName, retrievedFile.get().getFileName());
        assertEquals(newFilePath, retrievedFile.get().getFilePath());
        assertEquals("test-hash-123", retrievedFile.get().getFileHash());
    }

    @Test
    @Sql("/sql/create-processed-files-h2.sql")
    void testDeleteById() {
        // Arrange
        UUID fileId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        
        // Act
        processedFilesRepository.deleteById(fileId);
        Optional<ProcessedFiles> result = processedFilesRepository.findById(fileId);
        
        // Assert
        assertFalse(result.isPresent());
    }
}
