package one.cax.textractor.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("processed_files")
public class ProcessedFiles {

    @Id
    @Column("file_id")
    private UUID fileId;

    @Column("file_hash")
    private String fileHash;

    @Column("file_name")
    private String fileName;

    @Column("file_path")
    private String filePath;

    @Column("file_size")
    private long fileSize;

    @Column("app_id")
    private UUID appId;

    @Column("extracted_content")
    private String extractedContent;
    
    @Column("analysis_results")
    private String analysisResults;
    
    @Column("document_summary")
    private String documentSummary;
    
    @Column("processing_status")
    private String processingStatus;
    
    @Column("llm_output")
    private String llmOutput;


    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public UUID getAppId() {
        return appId;
    }

    public void setAppId(UUID appId) {
        this.appId = appId;
    }

    public String getExtractedContent() {
        return extractedContent;
    }

    public void setExtractedContent(String extractedContent) {
        this.extractedContent = extractedContent;
    }
    
    public String getAnalysisResults() {
        return analysisResults;
    }
    
    public void setAnalysisResults(String analysisResults) {
        this.analysisResults = analysisResults;
    }
    
    public String getDocumentSummary() {
        return documentSummary;
    }
    
    public void setDocumentSummary(String documentSummary) {
        this.documentSummary = documentSummary;
    }
    
    public String getProcessingStatus() {
        return processingStatus;
    }
    
    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }
    
    public String getLlmOutput() {
        return llmOutput;
    }
    
    public void setLlmOutput(String llmOutput) {
        this.llmOutput = llmOutput;
    }
}
