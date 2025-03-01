package one.cax.textractor.api.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testSuccessConstructor() {
        // Arrange
        String testData = "Test Data";
        
        // Act
        ApiResponse<String> response = new ApiResponse<>(testData);
        
        // Assert
        assertTrue(response.isSuccess());
        assertEquals(testData, response.getData());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void testErrorConstructor() {
        // Arrange
        String errorMessage = "An error occurred";
        String errorCode = "ERR_001";
        
        // Act
        ApiResponse<String> response = new ApiResponse<>(errorMessage, errorCode);
        
        // Assert
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals(errorMessage, response.getMessage());
        assertEquals(errorCode, response.getErrorCode());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void testTimestampIsCurrentTime() {
        // Arrange
        long beforeCreation = System.currentTimeMillis();
        
        // Act
        ApiResponse<String> response = new ApiResponse<>("Test");
        long afterCreation = System.currentTimeMillis();
        
        // Assert
        assertTrue(response.getTimestamp() >= beforeCreation);
        assertTrue(response.getTimestamp() <= afterCreation);
    }

    @Test
    void testGenericTypeHandling() {
        // Test with String
        ApiResponse<String> stringResponse = new ApiResponse<>("String data");
        assertEquals("String data", stringResponse.getData());
        
        // Test with Integer
        ApiResponse<Integer> intResponse = new ApiResponse<>(42);
        assertEquals(Integer.valueOf(42), intResponse.getData());
        
        // Test with custom object
        TestObject testObject = new TestObject("test", 123);
        ApiResponse<TestObject> objectResponse = new ApiResponse<>(testObject);
        assertEquals(testObject, objectResponse.getData());
        assertEquals("test", objectResponse.getData().getName());
        assertEquals(123, objectResponse.getData().getValue());
    }
    
    // Helper class for testing generic type handling
    private static class TestObject {
        private final String name;
        private final int value;
        
        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() {
            return name;
        }
        
        public int getValue() {
            return value;
        }
    }
}
