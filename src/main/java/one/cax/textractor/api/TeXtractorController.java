package one.cax.textractor.api;

import one.cax.textractor.Orchestrator;
import one.cax.textractor.api.dto.ApiResponse;
import one.cax.textractor.api.dto.ResponseEntityUtil;
import one.cax.textractor.datamodel.FileProcessing;
import one.cax.textractor.datamodel.ProcessingStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class TeXtractorController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Orchestrates the files processing */
    private final Orchestrator orchestrator;

    /**
     *
     * @param orchestrator orchestrates the files processing
     */
    public TeXtractorController(@Autowired Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }


    /**
     * Process the file uploaded.
     * @param appId application Id
     * @param file file to be processed
     * @return
     */
    @PostMapping("/process/{appId}")
    public ResponseEntity<ApiResponse<String>> process(@PathVariable String appId, @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            var message = "File is empty";
            logger.warn("Request with appId => {}: {}", appId, message);
            return ResponseEntityUtil.failedResponse(message, "FILE_EMPTY", HttpStatus.BAD_REQUEST);
        }

        if(!orchestrator.appIdIsValid(appId)) {
            return ResponseEntityUtil.failedResponse(
                    "AppProfile not found",
                    "APP_PROFILE_NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }

        try {

            var fileId = orchestrator.initialize(initialSetup(file, appId));
            return ResponseEntityUtil.successResponse(fileId.toString());

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return ResponseEntityUtil.failedResponse(
                    e.getMessage(),
                    "PROCESSING_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

    }

    @GetMapping("/status/{fileId}")
    public ResponseEntity<ApiResponse<ProcessingStatus>> getProcessingStatus(@PathVariable String fileId) {
        try {
            ProcessingStatus status = orchestrator.getProcessingStatus(fileId);
            return ResponseEntityUtil.successResponse(status);
        } catch (Exception e) {
            return ResponseEntityUtil.failedResponse(
                    "An error occurred while fetching the processing status",
                    "STATUS_FETCH_FAILED",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private FileProcessing initialSetup(MultipartFile file, String appId) throws IOException {

        var contentType =  file.getContentType();
        var fileName = file.getOriginalFilename();
        var fileSize = file.getSize();
        var fileContent = file.getBytes();

        return new FileProcessing(fileName, fileSize, contentType, fileContent, appId);

    }
}
