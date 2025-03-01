package one.cax.textractor.api.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseEntityUtil {
    public static <T> ResponseEntity<ApiResponse<T>> successResponse(T data) {
        ApiResponse<T> response = new ApiResponse<>(data);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public static <T> ResponseEntity<ApiResponse<T>> failedResponse(String message, String errorCode, HttpStatus status) {
        ApiResponse<T> response = new ApiResponse<>(message, errorCode);
        return new ResponseEntity<>(response, status);
    }
}
