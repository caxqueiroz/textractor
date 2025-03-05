package one.cax.textractor.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for accessing processed files in the database
 */
@Repository
public interface ProcessedFilesRepository extends JpaRepository<ProcessedFiles, UUID> {

    /**
     * Find a processed file by its hash
     * @param fileHash the hash of the file to find
     * @return the processed file if found
     */
    Optional<ProcessedFiles> findByFileHash(String fileHash);
    
    /**
     * Find all processed files for a specific app
     * @param appId the app ID to find files for
     * @return all processed files for the app
     */
    List<ProcessedFiles> findByAppId(UUID appId);
}
