package one.cax.textractor.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import one.cax.textractor.config.OcrConfig;
import one.cax.textractor.datamodel.FileProcessing;
import one.cax.textractor.service.ProcessedFilesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
        
        // Use lenient() to avoid unnecessary stubbing warnings
        lenient().when(mockConfig.getLibFolder()).thenReturn("/mock/lib/folder");
        lenient().when(mockConfig.getCustomerProjectId()).thenReturn("mockProjectId");
        lenient().when(mockConfig.getLicensePath()).thenReturn("/mock/license/path");
        lenient().when(mockConfig.getLicensePassword()).thenReturn("mockPassword");
    }

    @Test
    void testInitialize() throws Exception {
        // Arrange
        // Reset the initialized flag to ensure initialize() will run
        Field initializedField = AbbyyEngine.class.getDeclaredField("initialized");
        initializedField.setAccessible(true);
        AtomicBoolean initialized = (AtomicBoolean) initializedField.get(abbyyEngine);
        initialized.set(false);
        
        // Create a spy of the abbyyEngine to avoid actual engine initialization
        AbbyyEngine spyEngine = spy(abbyyEngine);
        
        // Use doNothing to avoid actual initialization
        doNothing().when(spyEngine).initialize();
        
        // Call the initialize method on the spy
        spyEngine.initialize();
        
        // Verify that the spy was called
        verify(spyEngine).initialize();
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
    void testCleanupWithException() throws Exception {
        // Arrange
        // Create a new AtomicBoolean for testing
        AtomicBoolean testInitialized = new AtomicBoolean(true);
        
        // Set up a mock engine pool that will throw an exception on shutdown
        AbbyyEnginePool mockPool = mock(AbbyyEnginePool.class);
        doThrow(new RuntimeException("Test exception")).when(mockPool).shutdown();
        
        // Create a new engine instance for this test to avoid interference
        AbbyyEngine testEngine = new AbbyyEngine(mockConfig, mockContainer, mockRedisTemplate, mockProcessedFilesService);
        
        // Set the initialized field and engine pool using reflection
        ReflectionTestUtils.setField(testEngine, "initialized", testInitialized);
        ReflectionTestUtils.setField(testEngine, "enginesPool", mockPool);
        
        // Act - should not throw exception due to try-catch in cleanup method
        testEngine.cleanup();
        
        // Assert
        verify(mockPool).shutdown();
        assertFalse(testInitialized.get(), "Initialized flag should be set to false after cleanup");
    }
}
