package one.cax.textractor.ocr;

import one.cax.textractor.config.OcrConfig;
import one.cax.textractor.datamodel.FileProcessing;
import one.cax.textractor.datamodel.ProcessingStatus;
import one.cax.textractor.db.ProcessedFiles;
import one.cax.textractor.service.ProcessedFilesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AbbyyEnginePoolTest {

    @Mock
    private OcrConfig mockConfig;

    @Mock
    private ProcessedFilesService mockProcessedFilesService;

    private AbbyyEnginePool abbyyEnginePool;

    @BeforeEach
    void setUp() {
        // Mock the OcrConfig methods to avoid actual engine initialization
        // Using lenient() to avoid unnecessary stubbing warnings
        lenient().when(mockConfig.getLibFolder()).thenReturn("/mock/lib/folder");
        lenient().when(mockConfig.getCustomerProjectId()).thenReturn("mockProjectId");
        lenient().when(mockConfig.getLicensePath()).thenReturn("/mock/license/path");
        lenient().when(mockConfig.getLicensePassword()).thenReturn("mockPassword");

        abbyyEnginePool = new AbbyyEnginePool(mockConfig);
        abbyyEnginePool.setProcessedFilesService(mockProcessedFilesService);
    }

    @Test
    void testSetProcessedFilesService() {
        // Arrange
        AbbyyEnginePool pool = new AbbyyEnginePool(mockConfig);
        
        // Act
        pool.setProcessedFilesService(mockProcessedFilesService);
        
        // Assert - Using reflection to verify the service was set
        try {
            Field field = AbbyyEnginePool.class.getDeclaredField("processedFilesService");
            field.setAccessible(true);
            assertEquals(mockProcessedFilesService, field.get(pool));
        } catch (Exception e) {
            fail("Failed to access processedFilesService field: " + e.getMessage());
        }
    }

    @Test
    void testSubmitTask() throws Exception {
        // Arrange
        // Create a test instance with a mocked task queue
        AbbyyEnginePool testPool = new AbbyyEnginePool(mockConfig);
        
        // Create a mock BlockingQueue and inject it using reflection
        @SuppressWarnings("unchecked")
        BlockingQueue<FileProcessing> mockQueue = mock(BlockingQueue.class);
        Field taskQueueField = AbbyyEnginePool.class.getDeclaredField("taskQueue");
        taskQueueField.setAccessible(true);
        taskQueueField.set(testPool, mockQueue);
        
        // Create a test file processing task
        byte[] fileContent = "Test content".getBytes();
        FileProcessing testTask = new FileProcessing("test.pdf", fileContent.length, "application/pdf", fileContent, "app123");
        testTask.initialize();
        
        // Act
        testPool.submitTask(testTask);
        
        // Assert
        verify(mockQueue).offer(testTask);
    }

    @Test
    void testShutdown() throws Exception {
        // Arrange
        AbbyyEnginePool testPool = new AbbyyEnginePool(mockConfig);
        
        // Create a mock ExecutorService and inject it using reflection
        ExecutorService mockExecutor = mock(ExecutorService.class);
        when(mockExecutor.awaitTermination(anyLong(), any())).thenReturn(true);
        
        Field executorField = AbbyyEnginePool.class.getDeclaredField("executorService");
        executorField.setAccessible(true);
        executorField.set(testPool, mockExecutor);
        
        // Set running flag
        Field runningField = AbbyyEnginePool.class.getDeclaredField("running");
        runningField.setAccessible(true);
        runningField.set(testPool, true);
        
        // Act
        testPool.shutdown();
        
        // Assert
        verify(mockExecutor).shutdown();
        verify(mockExecutor).awaitTermination(anyLong(), any());
        assertFalse((Boolean) runningField.get(testPool));
    }
}
