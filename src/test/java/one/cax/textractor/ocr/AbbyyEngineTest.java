package one.cax.textractor.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import one.cax.textractor.config.OcrConfig;
import one.cax.textractor.datamodel.FileProcessing;
import one.cax.textractor.service.ProcessedFilesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AbbyyEngineTest {

    @Mock
    private OcrConfig mockConfig;

    @Mock
    private RedisMessageListenerContainer mockContainer;

    @Mock
    private RedisTemplate<String, FileProcessing> mockRedisTemplate;

    @Mock
    private ProcessedFilesService mockProcessedFilesService;

    @Mock
    private AbbyyEnginePool mockEnginePool;

    private AbbyyEngine abbyyEngine;

    @BeforeEach
    void setUp() throws Exception {
        // Create the AbbyyEngine instance with mocked dependencies
        abbyyEngine = new AbbyyEngine(mockConfig, mockContainer, mockRedisTemplate, mockProcessedFilesService);
        
        // Set the mocked engine pool using reflection
        ReflectionTestUtils.setField(abbyyEngine, "enginesPool", mockEnginePool);
        
        // Set the topic field
        ReflectionTestUtils.setField(abbyyEngine, "ocrTopic", "test-ocr-topic");
    }

    @Test
    void testInitialize() throws Exception {
        // Arrange
        // Reset the initialized flag to ensure initialize() will run
        Field initializedField = AbbyyEngine.class.getDeclaredField("initialized");
        initializedField.setAccessible(true);
        AtomicBoolean initialized = (AtomicBoolean) initializedField.get(abbyyEngine);
        initialized.set(false);
        
        // Act
        abbyyEngine.initialize();
        
        // Assert
        verify(mockConfig).getLibFolder();
        verify(mockEnginePool).setProcessedFilesService(mockProcessedFilesService);
        verify(mockEnginePool).initialize();
        verify(mockContainer).addMessageListener(any(), any(Topic.class));
        assertTrue(abbyyEngine.isInitialized());
    }

    @Test
    void testInitializeOnlyOnce() throws Exception {
        // Arrange
        // Set the initialized flag to true
        Field initializedField = AbbyyEngine.class.getDeclaredField("initialized");
        initializedField.setAccessible(true);
        AtomicBoolean initialized = (AtomicBoolean) initializedField.get(abbyyEngine);
        initialized.set(true);
        
        // Act
        abbyyEngine.initialize();
        
        // Assert - verify that the engine pool was not initialized again
        verify(mockEnginePool, never()).initialize();
    }

    @Test
    void testHandleMessage() {
        // Arrange
        FileProcessing testFileProcessing = mock(FileProcessing.class);
        
        // Act
        abbyyEngine.handleMessage(testFileProcessing);
        
        // Assert
        verify(mockEnginePool).submitTask(testFileProcessing);
    }

    @Test
    void testHandleMessageWithException() {
        // Arrange
        FileProcessing testFileProcessing = mock(FileProcessing.class);
        doThrow(new RuntimeException("Test exception")).when(mockEnginePool).submitTask(any());
        
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> abbyyEngine.handleMessage(testFileProcessing));
    }

    @Test
    void testCleanup() {
        // Arrange
        // Set initialized to true
        ReflectionTestUtils.setField(abbyyEngine, "initialized", new AtomicBoolean(true));
        
        // Act
        abbyyEngine.cleanup();
        
        // Assert
        verify(mockEnginePool).shutdown();
        assertFalse(abbyyEngine.isInitialized());
    }

    @Test
    void testCleanupWhenNotInitialized() {
        // Arrange
        // Set initialized to false
        ReflectionTestUtils.setField(abbyyEngine, "initialized", new AtomicBoolean(false));
        
        // Act
        abbyyEngine.cleanup();
        
        // Assert
        verify(mockEnginePool, never()).shutdown();
        assertFalse(abbyyEngine.isInitialized());
    }

    @Test
    void testCleanupWithException() {
        // Arrange
        // Set initialized to true
        ReflectionTestUtils.setField(abbyyEngine, "initialized", new AtomicBoolean(true));
        
        // Make shutdown throw an exception
        doThrow(new RuntimeException("Test exception")).when(mockEnginePool).shutdown();
        
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> abbyyEngine.cleanup());
        
        // Initialized should still be false after cleanup
        assertFalse(abbyyEngine.isInitialized());
    }
}
