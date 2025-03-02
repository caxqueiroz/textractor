package one.cax.textractor.llm;

import one.cax.textractor.datamodel.FileProcessing;
import one.cax.textractor.datamodel.XDoc;
import one.cax.textractor.db.ProcessedFiles;
import one.cax.textractor.db.ProcessedFilesRepository;
import one.cax.textractor.service.ProcessedFilesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
public class OpenAIServiceIntegrationTest {

    @Autowired
    private ProcessedFilesService processedFilesService;

    @Autowired
    private RedisTemplate<String, FileProcessing> redisTemplate;

    @Autowired
    private RedisMessageListenerContainer container;

    @Autowired
    private ChatModel chatModel;

    private OpenAIService openAIService;
    private UUID testFileId;

    @BeforeEach
    public void setUp() {
        testFileId = UUID.randomUUID();
        
        // Initialize the service with real dependencies
        openAIService = new OpenAIService(chatModel, container, redisTemplate, processedFilesService);
        
        // Initialize the service
        try {
            Method initializeMethod = OpenAIService.class.getDeclaredMethod("initialize");
            initializeMethod.setAccessible(true);
            initializeMethod.invoke(openAIService);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing service", e);
        }
    }

    @Test
    void testProcessFileTask() throws Exception {
        // Create a sample file processing request
        FileProcessing fileProcessing = createSampleFileProcessing();
        
        // Process the file
        invokeProcessFileTask(openAIService, fileProcessing);
        
        // Wait for processing to complete
        TimeUnit.SECONDS.sleep(2);

        // Assert
        Optional<ProcessedFiles> processedFilesOpt = processedFilesService.findById(testFileId.toString());
        assertTrue(processedFilesOpt.isPresent(), "ProcessedFiles should be saved in the database");
        
        ProcessedFiles processedFiles = processedFilesOpt.get();
        assertEquals(testFileId, processedFiles.getFileId());
        
        // Check that LLM content was saved
        assertNotNull(processedFiles.getLlmContent());
        assertTrue(processedFiles.getLlmContent().contains("content"));
    }

    private FileProcessing createSampleFileProcessing() {
        String fileName = "integration_test.pdf";
        long fileSize = 2048L;
        String contentType = "application/pdf";
        byte[] fileContent = "integration test content".getBytes(StandardCharsets.UTF_8);
        String appId = UUID.randomUUID().toString(); // Use a valid UUID string for appId
        
        FileProcessing fileProcessing = new FileProcessing(fileName, fileSize, contentType, fileContent, appId);
        // Override the auto-generated fileId with our test fileId
        setField(fileProcessing, "fileId", testFileId);
        // Initialize the file hash
        fileProcessing.initialize();
        return fileProcessing;
    }

    // Helper method to set private fields via reflection
    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Error setting field: " + fieldName, e);
        }
    }

    // Helper method to invoke private methods via reflection
    private void invokeProcessFileTask(OpenAIService service, FileProcessing fileProcessing) {
        try {
            // Get the method
            Method processFileTaskMethod = OpenAIService.class.getDeclaredMethod("processFileTask", FileProcessing.class);
            processFileTaskMethod.setAccessible(true);
            
            // Get the actual implementation from the lambda in processFileTask
            Field executorServiceField = OpenAIService.class.getDeclaredField("executorService");
            executorServiceField.setAccessible(true);
            
            // Create a direct executor that runs tasks immediately in the current thread
            ExecutorService directExecutor = new ExecutorService() {
                @Override
                public void shutdown() {}
                
                @Override
                public List<Runnable> shutdownNow() { return Collections.emptyList(); }
                
                @Override
                public boolean isShutdown() { return false; }
                
                @Override
                public boolean isTerminated() { return false; }
                
                @Override
                public boolean awaitTermination(long timeout, TimeUnit unit) { return true; }
                
                @Override
                public <T> Future<T> submit(Callable<T> task) {
                    try {
                        T result = task.call();
                        return CompletableFuture.completedFuture(result);
                    } catch (Exception e) {
                        CompletableFuture<T> future = new CompletableFuture<>();
                        future.completeExceptionally(e);
                        return future;
                    }
                }
                
                @Override
                public <T> Future<T> submit(Runnable task, T result) {
                    try {
                        task.run();
                        return CompletableFuture.completedFuture(result);
                    } catch (Exception e) {
                        CompletableFuture<T> future = new CompletableFuture<>();
                        future.completeExceptionally(e);
                        return future;
                    }
                }
                
                @Override
                public Future<?> submit(Runnable task) {
                    try {
                        task.run();
                        return CompletableFuture.completedFuture(null);
                    } catch (Exception e) {
                        CompletableFuture<?> future = new CompletableFuture<>();
                        future.completeExceptionally(e);
                        return future;
                    }
                }
                
                @Override
                public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
                    return tasks.stream().map(this::submit).collect(Collectors.toList());
                }
                
                @Override
                public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
                    return invokeAll(tasks);
                }
                
                @Override
                public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
                    try {
                        return tasks.iterator().next().call();
                    } catch (Exception e) {
                        throw new ExecutionException(e);
                    }
                }
                
                @Override
                public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    return invokeAny(tasks);
                }
                
                @Override
                public void execute(Runnable command) {
                    command.run();
                }
            };
            
            // Temporarily replace the executor service with our direct executor
            ExecutorService originalExecutor = (ExecutorService) executorServiceField.get(service);
            executorServiceField.set(service, directExecutor);
            
            try {
                // Now invoke the method - it will use our direct executor
                processFileTaskMethod.invoke(service, fileProcessing);
            } finally {
                // Restore the original executor
                executorServiceField.set(service, originalExecutor);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error invoking processFileTask", e);
        }
    }
}
