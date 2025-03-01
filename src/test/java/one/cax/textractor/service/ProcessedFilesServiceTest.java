package one.cax.textractor.service;

import one.cax.textractor.db.ProcessedFiles;
import one.cax.textractor.db.ProcessedFilesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessedFilesServiceTest {

    @Mock
    private ProcessedFilesRepository processedFilesRepository;

    private ProcessedFilesService processedFilesService;

    @BeforeEach
    void setUp() {
        processedFilesService = new ProcessedFilesService(processedFilesRepository);
        // Set the fileStorePath field using reflection
        ReflectionTestUtils.setField(processedFilesService, "fileStorePath", System.getProperty("java.io.tmpdir"));
    }

    @Test
    void testAddProcessedFile() {
        // Arrange
        ProcessedFiles processedFile = new ProcessedFiles();
        UUID fileId = UUID.randomUUID();
        processedFile.setFileId(fileId);
        
        // Act
        processedFilesService.addProcessedFile(processedFile);
        
        // Assert
        ArgumentCaptor<ProcessedFiles> captor = ArgumentCaptor.forClass(ProcessedFiles.class);
        verify(processedFilesRepository, times(1)).save(captor.capture());
        assertEquals(fileId, captor.getValue().getFileId());
    }

    @Test
    void testSaveFile() throws Exception {
        // Arrange
        byte[] fileContent = "Test file content".getBytes();
        
        // Act
        String filePath = processedFilesService.saveFile(fileContent);
        
        // Assert
        assertNotNull(filePath);
        Path path = Paths.get(filePath);
        assertTrue(Files.exists(path));
        assertArrayEquals(fileContent, Files.readAllBytes(path));
        
        // Cleanup
        Files.deleteIfExists(path);
    }

    @Test
    void testSaveFileHandlesException() throws Exception {
        // Arrange
        byte[] fileContent = "Test file content".getBytes();
        
        // Create a mock service with an invalid path to trigger an exception
        ProcessedFilesService mockService = new ProcessedFilesService(processedFilesRepository);
        ReflectionTestUtils.setField(mockService, "fileStorePath", "/invalid/path/that/does/not/exist");
        
        // Act
        String filePath = mockService.saveFile(fileContent);
        
        // Assert
        assertNull(filePath);
    }
}
