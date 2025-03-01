package one.cax.textractor.datamodel;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FileProcessingTest {

    @Test
    void testInitialize() {
        // Arrange
        byte[] fileContent = "Test file content".getBytes();
        String fileName = "test.txt";
        long fileSize = fileContent.length;
        String contentType = "text/plain";
        String appId = "app123";
        FileProcessing fileProcessing = new FileProcessing(fileName, fileSize, contentType, fileContent, appId);
        
        // Act
        UUID fileId = fileProcessing.initialize();
        
        // Assert
        assertNotNull(fileId);
        assertEquals(ProcessingStatus.PROCESSING, fileProcessing.getStatus());
        assertNotNull(fileProcessing.getFileHash());
    }

    @Test
    void testGetters() {
        // Arrange
        byte[] fileContent = "Test file content".getBytes();
        String fileName = "test.txt";
        long fileSize = fileContent.length;
        String contentType = "text/plain";
        String appId = "app123";
        FileProcessing fileProcessing = new FileProcessing(fileName, fileSize, contentType, fileContent, appId);
        fileProcessing.initialize();
        
        // Act & Assert
        assertEquals(fileName, fileProcessing.getFileName());
        assertEquals(appId, fileProcessing.getAppId());
        assertEquals(fileSize, fileProcessing.getFileSize());
        assertNotNull(fileProcessing.getFileId());
        assertNotNull(fileProcessing.getFileHash());
        assertArrayEquals(fileContent, fileProcessing.getFileContent());
    }

    @Test
    void testSetStatus() {
        // Arrange
        byte[] fileContent = "Test file content".getBytes();
        String fileName = "test.txt";
        long fileSize = fileContent.length;
        String contentType = "text/plain";
        String appId = "app123";
        FileProcessing fileProcessing = new FileProcessing(fileName, fileSize, contentType, fileContent, appId);
        fileProcessing.initialize();
        
        // Act
        fileProcessing.setStatus(ProcessingStatus.PROCESSING);
        
        // Assert
        assertEquals(ProcessingStatus.PROCESSING, fileProcessing.getStatus());
        
        // Act
        fileProcessing.setStatus(ProcessingStatus.PROCESSED);
        
        // Assert
        assertEquals(ProcessingStatus.PROCESSED, fileProcessing.getStatus());
    }

    @Test
    void testSetMessage() {
        // Arrange
        byte[] fileContent = "Test file content".getBytes();
        String fileName = "test.txt";
        long fileSize = fileContent.length;
        String contentType = "text/plain";
        String appId = "app123";
        FileProcessing fileProcessing = new FileProcessing(fileName, fileSize, contentType, fileContent, appId);
        
        // Act
        fileProcessing.setMessage("Processing completed successfully");
        
        // Assert
        assertEquals("Processing completed successfully", fileProcessing.getMessage());
    }

    @Test
    void testCalculateHash() {
        // Arrange
        byte[] fileContent1 = "Test file content".getBytes();
        byte[] fileContent2 = "Different content".getBytes();
        
        FileProcessing fileProcessing1 = new FileProcessing("test1.txt", fileContent1.length, "text/plain", fileContent1, "app123");
        FileProcessing fileProcessing2 = new FileProcessing("test2.txt", fileContent1.length, "text/plain", fileContent1, "app456"); // Same content, different name/appId
        FileProcessing fileProcessing3 = new FileProcessing("test3.txt", fileContent2.length, "text/plain", fileContent2, "app123"); // Different content
        
        // Act
        fileProcessing1.initialize();
        fileProcessing2.initialize();
        fileProcessing3.initialize();
        
        // Assert
        // Same content should produce same hash regardless of filename or appId
        assertEquals(fileProcessing1.getFileHash(), fileProcessing2.getFileHash());
        
        // Different content should produce different hash
        assertNotEquals(fileProcessing1.getFileHash(), fileProcessing3.getFileHash());
    }

    @Test
    void testToString() {
        // Arrange
        byte[] fileContent = "Test file content".getBytes();
        String fileName = "test.txt";
        long fileSize = fileContent.length;
        String contentType = "text/plain";
        String appId = "app123";
        FileProcessing fileProcessing = new FileProcessing(fileName, fileSize, contentType, fileContent, appId);
        UUID fileId = fileProcessing.initialize();
        fileProcessing.setStatus(ProcessingStatus.PROCESSED);
        fileProcessing.setMessage("Success");
        
        // Act
        String toString = fileProcessing.toString();
        
        // Assert
        assertTrue(toString.contains(fileId.toString()));
        assertTrue(toString.contains(fileName));
        assertTrue(toString.contains(appId));
        assertTrue(toString.contains("PROCESSED"));
        assertTrue(toString.contains("Success"));
    }
}
