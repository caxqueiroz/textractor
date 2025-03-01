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


    Optional<AppProfile> findByProfileName(String profileName);

    @Query("SELECT * FROM app_profiles WHERE REPLACE(id::text, '-', '') LIKE :appId%")
    Optional<AppProfile> findByAppId(String appId);

    @Modifying
    @Query("DELETE FROM app_profiles WHERE REPLACE(id::text, '-', '') LIKE :appId%")
    void deleteByAppId(String appId);
}
