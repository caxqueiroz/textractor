package one.cax.textractor.datamodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * 
 * Created by Carlos Queiroz on 2025-03-01
 * 
 */
public class FileProcessing {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UUID fileId;
    private final long fileSize;
    private final String fileName;
    private final String contentType;
    private final byte[] fileContent;
    private final String appId;
    private ProcessingStatus status;
    private String message;
    private String fileHash;

    public FileProcessing(String fileName, long fileSize, String contentType, byte[] fileContent, String appId) {
        this.fileId = UUID.randomUUID();
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.fileContent = fileContent;
        this.appId = appId;
    }

    public UUID initialize() {
        logger.info("Initializing file processing for file {}", fileName);
        this.status = ProcessingStatus.PROCESSING;
        this.message = "File started to being processed";
        this.fileHash = calculateHash(fileContent);
        return fileId;
    }


    public byte[] getFileContent() {
        return fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public UUID getFileId() {
        return fileId;
    }

    public ProcessingStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessingStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAppId() {
        return appId;
    }


    private String calculateHash(byte[] fileContent) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(fileContent);
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to calculate hash", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }


    public String toString() {
        return "FileProcessing{" +
                "fileId=" + fileId +
                ", fileSize=" + fileSize +
                ", fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", appId='" + appId + '\'' +
                ", status=" + status +
                ", message='" + message + '\'' +
                '}';
    }

    public String getFileHash() {
        return fileHash;
    }
}