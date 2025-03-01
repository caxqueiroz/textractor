package one.cax.textractor.db;

import one.cax.textractor.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("test")
@Import(TestConfig.class)
class AppProfileTest {

    @Test
    void testGetters() {
        // Arrange
        AppProfile appProfile = new AppProfile();
        UUID id = UUID.randomUUID();
        
        // Use reflection to set private fields for testing
        try {
            java.lang.reflect.Field idField = AppProfile.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(appProfile, id);
            
            java.lang.reflect.Field nameField = AppProfile.class.getDeclaredField("profileName");
            nameField.setAccessible(true);
            nameField.set(appProfile, "Test Profile");
            
            java.lang.reflect.Field descField = AppProfile.class.getDeclaredField("profileDescription");
            descField.setAccessible(true);
            descField.set(appProfile, "Test Description");
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
        
        // Act & Assert
        assertEquals(id, appProfile.getId());
        assertEquals("Test Profile", appProfile.getProfileName());
        assertEquals("Test Description", appProfile.getProfileDescription());
    }

    @Test
    void testGetAppId() {
        // Arrange
        AppProfile appProfile = new AppProfile();
        UUID id = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        
        // Use reflection to set private fields for testing
        try {
            java.lang.reflect.Field idField = AppProfile.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(appProfile, id);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
        
        // Act
        String appId = appProfile.getAppId();
        
        // Assert
        assertEquals("123456", appId);
        assertEquals(6, appId.length());
    }
}
