package one.cax.textractor.db;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

/**
 *
 */
public interface AppProfileRepository extends CrudRepository<AppProfile, UUID> {

    @Query("SELECT * FROM app_profiles WHERE profile_name = :profileName")
    Optional<AppProfile> findByProfileName(String profileName);

    @Query("SELECT * FROM app_profiles WHERE REPLACE(CAST(id AS VARCHAR), '-', '') LIKE CONCAT(:appId, '%') LIMIT 1")
    Optional<AppProfile> findByAppId(String appId);

    @Modifying
    @Query("DELETE FROM app_profiles WHERE REPLACE(CAST(id AS VARCHAR), '-', '') LIKE CONCAT(:appId, '%')")
    void deleteByAppId(String appId);
}
