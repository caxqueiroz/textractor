package one.cax.textractor.api.dto;

public class ApiResponse<T> {
    private final boolean success;
    private T data;
    private String message;
    private String errorCode;
    private final long timestamp;

    // Constructor for successful response
    public ApiResponse(T data) {
        this.success = true;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // Constructor for failed response
    public ApiResponse(String message, String errorCode) {
        this.success = false;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // You might want to add setters if needed
}
