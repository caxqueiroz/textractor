package one.cax.textractor;

import one.cax.textractor.datamodel.FileProcessing;
import one.cax.textractor.datamodel.ProcessingStatus;
import one.cax.textractor.db.ProcessedFiles;
import one.cax.textractor.llm.OpenAIService;
import one.cax.textractor.ocr.AbbyyEngine;
import one.cax.textractor.service.AppProfileService;
import one.cax.textractor.service.ProcessedFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class Orchestrator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${textractor.redis.ocr.topic}")
    private String ocrTopic;

    @Value("${textractor.redis.llm.topic:llm-processing-topic}")
    private String llmTopic;

    private final RedisTemplate<String, String> redisTemplate;

    /** Abbyy OCR engine */
    private AbbyyEngine abbyyEngine;

    /** OpenAI service for LLM processing */
    private OpenAIService openAIService;

    /** AppProfile service */
    private AppProfileService appProfileService;
    
    /** ProcessedFiles service */
    private ProcessedFilesService processedFilesService;

    /**
     * Constructor for Orchestrator
     * 
     * @param redisTemplate Redis template for messaging
     * @param abbyyEngine Abbyy OCR engine
     * @throws Exception if initialization fails
     */
    public Orchestrator(RedisTemplate<String, String> redisTemplate, @Autowired AbbyyEngine abbyyEngine, @Autowired OpenAIService openAIService) throws Exception {
        this.redisTemplate = redisTemplate;
        this.abbyyEngine = abbyyEngine;
        this.abbyyEngine.initialize();
        this.openAIService = openAIService;
        openAIService.initialize();
    }

    @Autowired
    public void setAppProfileService(AppProfileService appProfileService) {
        this.appProfileService = appProfileService;
    }

    /**
     * Set the OpenAI service
     * 
     * @param openAIService OpenAI service for LLM processing
     */
    @Autowired
    public void setOpenAIService(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }
    
    /**
     * Set the ProcessedFiles service
     * 
     * @param processedFilesService ProcessedFiles service
     */
    @Autowired
    public void setProcessedFilesService(ProcessedFilesService processedFilesService) {
        this.processedFilesService = processedFilesService;
    }

    /**
     * Process a file using both OCR and LLM
     * 
     * @param fileProcessing File processing request
     * @return File ID
     */
    public UUID process(FileProcessing fileProcessing) {
        var fileId = fileProcessing.initialize();
        logger.info("Starting processing for file {}", fileId);
        
        // Send to OCR processing
        redisTemplate.convertAndSend(ocrTopic, fileProcessing);
        
        // Send to LLM processing
        redisTemplate.convertAndSend(llmTopic, fileProcessing);
        
        // Create a new ProcessedFiles record
        createProcessedFileRecord(fileProcessing);
        
        return fileId;
    }
    
    /**
     * Create a new ProcessedFiles record
     * 
     * @param fileProcessing File processing request
     */
    private void createProcessedFileRecord(FileProcessing fileProcessing) {
        try {
            ProcessedFiles processedFile = new ProcessedFiles();
            processedFile.setFileId(fileProcessing.getFileId());
            processedFile.setFileName(fileProcessing.getFileName());
            processedFile.setFileSize(fileProcessing.getFileSize());
            processedFile.setFileHash(fileProcessing.getFileHash());
            processedFile.setProcessingStatus("PROCESSING");
            
            // Save file content to disk
            String filePath = processedFilesService.saveFile(fileProcessing.getFileContent());
            processedFile.setFilePath(filePath);
            
            // Save the app ID
            if (fileProcessing.getAppId() != null) {
                processedFile.setAppId(UUID.fromString(fileProcessing.getAppId()));
            }
            
            processedFilesService.addProcessedFile(processedFile);
            logger.info("Created ProcessedFiles record for file {}", fileProcessing.getFileId());
        } catch (Exception e) {
            logger.error("Error creating ProcessedFiles record: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if an app ID is valid
     * 
     * @param appId App ID to check
     * @return True if the app ID is valid
     */
    public boolean appIdIsValid(String appId) {
        return appProfileService.findByAppId(appId) != null;
    }

    /**
     * Set the Abbyy engine
     * 
     * @param abbyyEngine Abbyy engine
     */
    @Autowired
    public void setAbbyyEngine(AbbyyEngine abbyyEngine) {
        this.abbyyEngine = abbyyEngine;
    }

    /**
     * Get the processing status of a file
     * 
     * @param fileId File ID
     * @return Processing status
     */
    public ProcessingStatus getProcessingStatus(String fileId) {
        try {
            Optional<ProcessedFiles> fileOpt = processedFilesService.findById(fileId);
            if (fileOpt.isPresent()) {
                ProcessedFiles file = fileOpt.get();
                String status = file.getProcessingStatus();
                
                if (status == null) {
                    return ProcessingStatus.UNKNOWN;
                }
                
                return ProcessingStatus.valueOf(status);
            }
            return ProcessingStatus.UNKNOWN;
        } catch (Exception e) {
            logger.error("Error getting processing status: {}", e.getMessage(), e);
            return ProcessingStatus.UNKNOWN;
        }
    }
}
