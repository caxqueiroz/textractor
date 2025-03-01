package one.cax.textractor.api.dto;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class ResponseEntityUtilTest {

    @Test
    void testSuccessResponse() {
        // Arrange
        String testData = "Test Data";
        
        // Act
        ResponseEntity<ApiResponse<String>> responseEntity = ResponseEntityUtil.successResponse(testData);
        
        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody().isSuccess());
        assertEquals(testData, responseEntity.getBody().getData());
    }

    @Test
    void testFailedResponse() {
        // Arrange
        String errorMessage = "Error occurred";
        String errorCode = "ERR_001";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        // Act
        ResponseEntity<ApiResponse<String>> responseEntity = ResponseEntityUtil.failedResponse(errorMessage, errorCode, status);
        
        // Assert
        assertEquals(status, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertFalse(responseEntity.getBody().isSuccess());
        assertEquals(errorMessage, responseEntity.getBody().getMessage());
        assertEquals(errorCode, responseEntity.getBody().getErrorCode());
    }

    @Test
    void testSuccessResponseWithNullData() {
        // Act
        ResponseEntity<ApiResponse<Object>> responseEntity = ResponseEntityUtil.successResponse(null);
        
        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody().isSuccess());
        assertNull(responseEntity.getBody().getData());
    }

    @Test
    void testFailedResponseWithDifferentStatusCodes() {
        // Test with INTERNAL_SERVER_ERROR
        ResponseEntity<ApiResponse<String>> serverErrorResponse = 
            ResponseEntityUtil.failedResponse("Server error", "ERR_500", HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, serverErrorResponse.getStatusCode());
        
        // Test with UNAUTHORIZED
        ResponseEntity<ApiResponse<String>> unauthorizedResponse = 
            ResponseEntityUtil.failedResponse("Unauthorized", "ERR_401", HttpStatus.UNAUTHORIZED);
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorizedResponse.getStatusCode());
        
        // Test with NOT_FOUND
        ResponseEntity<ApiResponse<String>> notFoundResponse = 
            ResponseEntityUtil.failedResponse("Not found", "ERR_404", HttpStatus.NOT_FOUND);
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());
    }

    @Test
    void testGenericTypeHandling() {
        // Test with String
        ResponseEntity<ApiResponse<String>> stringResponse = ResponseEntityUtil.successResponse("String data");
        assertEquals("String data", stringResponse.getBody().getData());
        
        // Test with Integer
        ResponseEntity<ApiResponse<Integer>> intResponse = ResponseEntityUtil.successResponse(42);
        assertEquals(Integer.valueOf(42), intResponse.getBody().getData());
        
        // Test with custom object
        TestObject testObject = new TestObject("test", 123);
        ResponseEntity<ApiResponse<TestObject>> objectResponse = ResponseEntityUtil.successResponse(testObject);
        assertEquals(testObject, objectResponse.getBody().getData());
    }
    
    // Helper class for testing generic type handling
    private static class TestObject {
        private final String name;
        private final int value;
        
        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}
