package one.cax.textractor.db;

import one.cax.textractor.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AppProfileRepositoryTest {

    @Autowired
    private AppProfileRepository appProfileRepository;

    @Test
    @Sql("/sql/create-app-profiles-h2.sql")
    void testFindByProfileName() {
        // Arrange
        String profileName = "Test Profile";
        
        // Act
        Optional<AppProfile> result = appProfileRepository.findByProfileName(profileName);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(profileName, result.get().getProfileName());
        assertEquals("Test Description", result.get().getProfileDescription());
    }

    @Test
    @Sql("/sql/create-app-profiles-h2.sql")
    void testFindByProfileNameNotFound() {
        // Arrange
        String profileName = "Non-existent Profile";
        
        // Act
        Optional<AppProfile> result = appProfileRepository.findByProfileName(profileName);
        
        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @Sql("/sql/create-app-profiles-h2.sql")
    void testFindByAppId() {
        // Arrange
        String appId = "abcdef"; // First 6 chars of the UUID without dashes
        
        // Act
        Optional<AppProfile> result = appProfileRepository.findByAppId(appId);
        
        // Assert
        assertTrue(result.isPresent(), "Should find a profile with the given appId");
        assertEquals(appId, result.get().getAppId());
    }

    @Test
    @Sql("/sql/create-app-profiles-h2.sql")
    void testDeleteByAppId() {
        // Arrange
        String appId = "abcdef";
        
        // Act
        appProfileRepository.deleteByAppId(appId);
        Optional<AppProfile> result = appProfileRepository.findByAppId(appId);
        
        // Assert
        assertFalse(result.isPresent(), "No profiles should be found after deletion");
    }

    @Test
    @Sql("/sql/create-app-profiles-h2.sql")
    void testSaveAndFindById() {
        // Arrange
        String profileName = "Brand New Profile";
        String profileDescription = "Brand New Description";
        
        // Find an existing profile to modify
        Optional<AppProfile> existingProfile = appProfileRepository.findByProfileName("Test Profile");
        assertTrue(existingProfile.isPresent(), "Test profile should exist");
        
        AppProfile profile = existingProfile.get();
        profile.setProfileName(profileName);
        profile.setProfileDescription(profileDescription);
        
        // Act
        AppProfile savedProfile = appProfileRepository.save(profile);
        Optional<AppProfile> retrievedProfile = appProfileRepository.findById(savedProfile.getId());
        
        // Assert
        assertNotNull(savedProfile);
        assertTrue(retrievedProfile.isPresent());
        assertEquals(profileName, retrievedProfile.get().getProfileName());
        assertEquals(profileDescription, retrievedProfile.get().getProfileDescription());
    }
}
