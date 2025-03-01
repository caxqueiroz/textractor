package one.cax.textractor.db;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;


/**
 * Repository for accessing processed files in the database
 */
public interface ProcessedFilesRepository extends CrudRepository<ProcessedFiles, UUID> {

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
    Iterable<ProcessedFiles> findByAppId(UUID appId);
}
