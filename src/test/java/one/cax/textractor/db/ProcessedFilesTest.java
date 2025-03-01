package one.cax.textractor.db;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProcessedFilesTest {

    @Test
    void testGettersAndSetters() {
        // Arrange
        ProcessedFiles processedFiles = new ProcessedFiles();
        UUID fileId = UUID.randomUUID();
        String fileHash = "abc123hash";
        String fileName = "test.pdf";
        String filePath = "/path/to/file.pdf";
        long fileSize = 1024L;
        UUID appId = UUID.randomUUID();
        String extractedContent = "{\"text\":\"extracted content\"}";
        
        // Act
        processedFiles.setFileId(fileId);
        processedFiles.setFileHash(fileHash);
        processedFiles.setFileName(fileName);
        processedFiles.setFilePath(filePath);
        processedFiles.setFileSize(fileSize);
        processedFiles.setAppId(appId);
        processedFiles.setExtractedContent(extractedContent);
        
        // Assert
        assertEquals(fileId, processedFiles.getFileId());
        assertEquals(fileHash, processedFiles.getFileHash());
        assertEquals(fileName, processedFiles.getFileName());
        assertEquals(filePath, processedFiles.getFilePath());
        assertEquals(fileSize, processedFiles.getFileSize());
        assertEquals(appId, processedFiles.getAppId());
        assertEquals(extractedContent, processedFiles.getExtractedContent());
    }

    @Test
    void testDefaultConstructor() {
        // Arrange & Act
        ProcessedFiles processedFiles = new ProcessedFiles();
        
        // Assert
        assertNull(processedFiles.getFileId());
        assertNull(processedFiles.getFileHash());
        assertNull(processedFiles.getFileName());
        assertNull(processedFiles.getFilePath());
        assertEquals(0L, processedFiles.getFileSize());
        assertNull(processedFiles.getAppId());
        assertNull(processedFiles.getExtractedContent());
    }
}
