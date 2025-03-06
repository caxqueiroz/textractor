package one.cax.textractor.db;

import jakarta.persistence.*;
import one.cax.textractor.datamodel.XDoc;
import one.cax.textractor.datamodel.XDocAttributeConverter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a processed file in the database.
 * Uses JPA annotations for ORM mapping.
 */
@Entity
@Table(name = "processed_files")
public class ProcessedFiles {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "file_id")
    private UUID fileId;

    @Column(name = "file_hash", nullable = false, unique = true)
    private String fileHash;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "app_id", nullable = false)
    private UUID appId;

    @Column(name = "processing_status", nullable = false)
    private String processingStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Convert(converter = XDocAttributeConverter.class)
    @Column(name = "ocr_content", columnDefinition = "jsonb")
    private XDoc ocrContent;

    @Convert(converter = XDocAttributeConverter.class)
    @Column(name = "llm_content", columnDefinition = "jsonb")
    private XDoc llmContent;


    /**
     * Default constructor
     */
    public ProcessedFiles() {
        // Default constructor required by JPA
    }
    
    /**
     * Ensure createdAt is set before persisting
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getters and setters

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

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public XDoc getOcrContent() {
        return ocrContent;
    }

    public void setOcrContent(XDoc ocrContent) {
        this.ocrContent = ocrContent;
    }

    public XDoc getLlmContent() {
        return llmContent;
    }

    public void setLlmContent(XDoc llmContent) {
        this.llmContent = llmContent;
    }
}
