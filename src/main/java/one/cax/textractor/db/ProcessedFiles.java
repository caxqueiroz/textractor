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

    @Column("ocr_content")
    private String ocrContent;
    
    @Column("processing_status")
    private String processingStatus;
    
    @Column("llm_content")
    private String llmContent;


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

    public String getOcrContent() {
        return ocrContent;
    }

    public void setOcrContent(String ocrContent) {
        this.ocrContent = ocrContent;
    }
    
    public String getLlmContent() {
        return llmContent;
    }
    
    public void setLlmContent(String llmContent) {
        this.llmContent = llmContent;
    }
    
    
    public String getProcessingStatus() {
        return processingStatus;
    }
    
    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }
}
