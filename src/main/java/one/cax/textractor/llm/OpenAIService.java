package one.cax.textractor.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import one.cax.textractor.datamodel.FileProcessing;
import one.cax.textractor.datamodel.XDoc;
import one.cax.textractor.db.ProcessedFiles;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import one.cax.textractor.service.ProcessedFilesService;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

/**
 * Service for extracting text from documents using LLMs technology
 * through Spring AI.
 */
@Service
public class OpenAIService {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Create the system message with instructions
    private final String systemInstruction = "You are an AI assistant specialized in extracting text from documents. using OCR or not." +
            "Extract the text from the document and respond with a json format of the extracted information.";
    
    private final ChatModel chatModel;
    private final ProcessedFilesService processedFilesService;
    private final RedisMessageListenerContainer container;
    private MessageListenerAdapter listenerAdapter;
    private final RedisTemplate<String, FileProcessing> redisTemplate;
    private volatile boolean running = true;

    private ExecutorService executorService;
    private final BlockingQueue<FileProcessing> requestQueue;

    @Value("${openai.model:gpt-4o}")
    private String model;
    
    @Value("${openai.temperature:0.1}")
    private double temperature;
    
    @Value("${openai.max-tokens:4000}")
    private int maxTokens;

    @Value("${textractor.redis.llm.topic:llm-processing-topic}")
    private String llmTopic;
    
    @Autowired
    public OpenAIService(@Autowired ChatModel chatModel, RedisMessageListenerContainer container,
                         RedisTemplate<String, FileProcessing> redisTemplate,
                         @Autowired ProcessedFilesService processedFilesService) {
        this.chatModel = chatModel;
        this.processedFilesService = processedFilesService;
        this.container = container;
        this.redisTemplate = redisTemplate;
        this.requestQueue = new LinkedBlockingQueue<>();
    }

    public void initialize() {

        int cores = Runtime.getRuntime().availableProcessors();
        logger.info("Using {} cores", cores);
        executorService = Executors.newFixedThreadPool(cores);


        listenerAdapter = new MessageListenerAdapter(this, "handleMessage");
        ObjectMapper objectMapper = new ObjectMapper();
        Jackson2JsonRedisSerializer<FileProcessing> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, FileProcessing.class);
        listenerAdapter.setSerializer(serializer);
        container.addMessageListener(listenerAdapter, new org.springframework.data.redis.listener.ChannelTopic(llmTopic));
        logger.info("Subscribed to Redis topic: {}", llmTopic);
        
        // Start the task processor thread
        startTaskProcessor();
    }

    /**
     *
     * @param fileProcessing
     * @return
     */
    public void handleMessage(FileProcessing fileProcessing) {
        try {

            UUID fileId = fileProcessing.getFileId();
            requestQueue.offer(fileProcessing);
            logger.info("Message received for file: {}", fileId);

        } catch (Exception e) {
            logger.error("Error extracting information from document: {}", e.getMessage(), e);

        }
    }

    private void startTaskProcessor() {
        Thread processorThread = new Thread(() -> {
            while (running) {
                try {
                    FileProcessing task = requestQueue.take(); // This will block if the queue is empty
                    processFileTask(task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error processing task", e);
                }
            }
        });
        processorThread.setDaemon(true);
        processorThread.start();
        logger.info("LLM Task processor started");
    }

    private void processFileTask(FileProcessing fileProcessing) {
        executorService.submit(() -> {
            try {
                UUID fileId = fileProcessing.getFileId();
                String base64Image = Base64.getEncoder().encodeToString(fileProcessing.getFileContent());

                // Create a message with image data
                Map<String, Object> mediaContent = new HashMap<>();
                mediaContent.put("type", "image_url");

                Map<String, String> imageData = new HashMap<>();
                imageData.put("url", "data:" + determineMimeType(fileProcessing.getContentType()) + ";base64," + base64Image);
                mediaContent.put("image_url", imageData);

                MimeType mimeType = MimeTypeUtils.parseMimeType(determineMimeType(fileProcessing.getContentType()));
                Resource resource = new ByteArrayResource(Base64.getDecoder().decode(base64Image));
                // Create a user message with the prompt text
                UserMessage userMessage = new UserMessage(
                        "Extract all text from this file. Return the result as a JSON object with the following structure: " +
                                "{ \"numberPages\": number-of-pages, \"pages\": [ " +
                                "{ \"page_number\": page-number-value, \"content\": \"text content of the page\" }, " +
                                "{ \"page_number\": page-number-value, \"content\": \"text content of the page\" }, ... ] }",
                        new Media(mimeType, resource));

                ;
                // Create options with the GPT-4 model
                OpenAiChatOptions options = OpenAiChatOptions.builder()
                        .model(OpenAiApi.ChatModel.GPT_4_O.getValue())
                        .build();

                // Create prompt with message and options
                Prompt prompt = new Prompt(List.of(userMessage), options);

                // Call the model
                ChatResponse response = chatModel.call(prompt);

                logger.info("Response: {}", response);
                // Process the response
                String returnedText = response.getResult().getOutput().getText();
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    // Parse the returned JSON into an XDoc object
                    XDoc xdoc = objectMapper.readValue(returnedText, XDoc.class);
                    
                    // Set the correct fileId from the original request
                    // Note: We'll need to create a new XDoc with the correct fileId since fileId is final
                    XDoc processedDoc = new XDoc();
                    processedDoc.setId(fileId);

                    // Convert to JSON for logging
                    String processedJson = processedDoc.toJSON().toString();
                    logger.info("Processed XDoc: {}", processedJson);
                    ProcessedFiles processedFiles = new ProcessedFiles();
                    processedFiles.setFileId(fileId);
                    processedFiles.setLlmContent(processedDoc);
                    processedFiles.setFileName(fileProcessing.getFileName());
                    processedFiles.setFileSize(fileProcessing.getFileSize());
                    processedFiles.setFileHash(fileProcessing.getFileHash());
                    var filePath = processedFilesService.saveFile(fileProcessing.getFileContent());
                    processedFiles.setFilePath(filePath);
                    processedFiles.setAppId(UUID.fromString(fileProcessing.getAppId()));
                
                    processedFilesService.addProcessedFile(processedFiles); 
                    
                } catch (Exception e) {
                    logger.error("Error parsing response to XDoc: {}", e.getMessage(), e);
                }
            }
            catch (Exception e) {
                logger.error("Error processing file: {}", e.getMessage(), e);
            }
        });
    }

    @PreDestroy
    public void cleanup() {
        running = false;
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    private String determineMimeType(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerPath.endsWith(".png")) {
            return MimeTypeUtils.IMAGE_PNG_VALUE;
        } else if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return MimeTypeUtils.IMAGE_JPEG_VALUE;
        } else if (lowerPath.endsWith(".gif")) {
            return MimeTypeUtils.IMAGE_GIF_VALUE;
        } else if (lowerPath.endsWith(".tiff") || lowerPath.endsWith(".tif")) {
            return "image/tiff";
        } else {
            // Default to octet-stream for unknown types
            return MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

}