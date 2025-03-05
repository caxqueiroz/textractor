package one.cax.textractor.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for accessing app profiles in the database
 */
@Repository
public interface AppProfileRepository extends JpaRepository<AppProfile, UUID> {

    /**
     * Find an app profile by its name
     * @param profileName the name of the profile to find
     * @return the profile if found
     */
    @Query("SELECT a FROM AppProfile a WHERE a.profileName = :profileName")
    Optional<AppProfile> findByProfileName(@Param("profileName") String profileName);

    /**
     * Find an app profile by its app ID
     * @param appId the app ID to find
     * @return the profile if found
     */
    @Query(value = "SELECT * FROM app_profiles WHERE REPLACE(CAST(id AS VARCHAR), '-', '') LIKE CONCAT(:appId, '%') LIMIT 1", nativeQuery = true)
    Optional<AppProfile> findByAppId(@Param("appId") String appId);

    /**
     * Delete an app profile by its app ID
     * @param appId the app ID to delete
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM app_profiles WHERE REPLACE(CAST(id AS VARCHAR), '-', '') LIKE CONCAT(:appId, '%')", nativeQuery = true)
    void deleteByAppId(@Param("appId") String appId);
}
