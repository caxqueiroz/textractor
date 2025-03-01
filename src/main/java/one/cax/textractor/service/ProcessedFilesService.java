package one.cax.textractor.service;


import one.cax.textractor.db.ProcessedFiles;
import one.cax.textractor.db.ProcessedFilesRepository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProcessedFilesService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${textractor.filestore.path}")
    private String fileStorePath;

    private final ProcessedFilesRepository processedFilesRepository;

    public ProcessedFilesService(@Autowired ProcessedFilesRepository processedFilesRepository) {
        this.processedFilesRepository = processedFilesRepository;
    }

    /**
     * Find a processed file by its ID
     * 
     * @param fileId The ID of the file to find
     * @return An Optional containing the file if found
     */
    public Optional<ProcessedFiles> findById(String fileId) {
        try {
            UUID uuid = UUID.fromString(fileId);
            return processedFilesRepository.findById(uuid);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid file ID format: {}", fileId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Find a processed file by its hash
     * 
     * @param fileHash The hash of the file to find
     * @return An Optional containing the file if found
     */
    public Optional<ProcessedFiles> findByHash(String fileHash) {
        return processedFilesRepository.findByFileHash(fileHash);
    }

    /**
     * Add a processed file to the repository
     * 
     * @param processedFile The processed file to add
     */
    public void addProcessedFile(ProcessedFiles processedFile) {
        processedFilesRepository.save(processedFile);
        logger.info("Saved processed file {}", processedFile.getFileId());
    }

    /**
     * Save file content to the file system
     * 
     * @param fileContent The content of the file to save
     * @return The path to the saved file
     */
    public String saveFile(byte[] fileContent) {
        Path directoryPath = Paths.get(fileStorePath);
        Path filePath = directoryPath.resolve(UUID.randomUUID().toString());
        try {
            Files.write(filePath, fileContent);
            return filePath.toString();
        } catch (Exception e) {
            logger.error("Error saving file", e);
            return null;
        }
    }
    
    
    
    
    
    /**
     * Update the processing status for a processed file
     * 
     * @param fileId The ID of the file to update
     * @param status The status to set
     * @return True if the update was successful, false otherwise
     */
    public boolean updateProcessingStatus(String fileId, String status) {
        try {
            UUID uuid = UUID.fromString(fileId);
            Optional<ProcessedFiles> fileOpt = processedFilesRepository.findById(uuid);
            
            if (fileOpt.isPresent()) {
                ProcessedFiles file = fileOpt.get();
                file.setProcessingStatus(status);
                processedFilesRepository.save(file);
                logger.info("Updated processing status for file {} to {}", fileId, status);
                return true;
            } else {
                logger.error("File with ID {} not found", fileId);
                return false;
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid file ID format: {}", fileId, e);
            return false;
        }
    }
    
    /**
     * Update the LLM output for a processed file
     * 
     * @param fileId The ID of the file to update
     * @param llmOutput The LLM output to set
     * @return True if the update was successful, false otherwise
     */
    public boolean updateLlmOutput(String fileId, String llmOutput) {
        try {
            UUID uuid = UUID.fromString(fileId);
            Optional<ProcessedFiles> fileOpt = processedFilesRepository.findById(uuid);
            
            if (fileOpt.isPresent()) {
                ProcessedFiles file = fileOpt.get();
                file.setLlmContent(llmOutput);
                processedFilesRepository.save(file);
                logger.info("Updated LLM output for file {}", fileId);
                return true;
            } else {
                logger.error("File with ID {} not found", fileId);
                return false;
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid file ID format: {}", fileId, e);
            return false;
        }
    }
}
