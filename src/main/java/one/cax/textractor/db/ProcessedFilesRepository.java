package one.cax.textractor.db;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;


/**
 *
 */
public interface ProcessedFilesRepository extends CrudRepository<ProcessedFiles, UUID> {


}
