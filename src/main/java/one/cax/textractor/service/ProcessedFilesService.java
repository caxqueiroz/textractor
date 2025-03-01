package one.cax.textractor.service;

import one.cax.textractor.datamodel.XDoc;
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
import java.util.UUID;

@Service
public class ProcessedFilesService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Value("${textractor.filestore.path}")
    private String fileStorePath;

    private final ProcessedFilesRepository processedFilesRepository;

    public ProcessedFilesService(@Autowired  ProcessedFilesRepository processedFilesRepository) {
        this.processedFilesRepository = processedFilesRepository;
    }

    public void addProcessedFile(ProcessedFiles processedFile) {
        processedFilesRepository.save(processedFile);
        logger.info("Saved processed file {}", processedFile.getFileId());
    }

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

}
