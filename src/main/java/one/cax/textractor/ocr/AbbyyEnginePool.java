package one.cax.textractor.ocr;

import com.abbyy.FREngine.*;
import one.cax.textractor.config.OcrConfig;
import one.cax.textractor.datamodel.FileProcessing;
import one.cax.textractor.datamodel.XDoc;
import one.cax.textractor.datamodel.XPage;
import one.cax.textractor.db.ProcessedFiles;
import one.cax.textractor.service.ProcessedFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Pool of ABBYY OCR engines for processing documents.
 */
@Service
public class AbbyyEnginePool {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private BlockingQueue<IEngine> enginePool;
    private ExecutorService executorService;
    private BlockingQueue<FileProcessing> taskQueue;
    private final OcrConfig config;
    private volatile boolean running = true;

    private ProcessedFilesService processedFilesService;
    
    /**
     * Constructor for AbbyyEnginePool.
     * @param config OCR configuration
     */
    public AbbyyEnginePool(@Autowired OcrConfig config) {
        this.config = config;
    }

    /**
     * Sets the ProcessedFilesService.
     * @param processedFilesService The service to use for processed files
     */
    public void setProcessedFilesService(ProcessedFilesService processedFilesService) {
        this.processedFilesService = processedFilesService;
    }

    /**
     * Initialize the engine pool.
     */
    public void initialize() {
        int coreCount = Runtime.getRuntime().availableProcessors();
        this.enginePool = new ArrayBlockingQueue<>(coreCount);
        this.taskQueue = new LinkedBlockingQueue<>();
        this.executorService = Executors.newFixedThreadPool(coreCount);

        // Initialize the pool with Engine instances
        for (int i = 0; i < coreCount; i++) {
            try {
                IEngine engine = Engine.InitializeEngine(
                        config.getLibFolder(),
                        config.getCustomerProjectId(),
                        config.getLicensePath(),
                        config.getLicensePassword(),
                        "",
                        "",
                        false
                );
                enginePool.offer(engine);
                logger.info("Engine {} initialized", i);

            } catch (Exception e) {
                // Handle initialization error
                throw new RuntimeException("Failed to initialize engine", e);
            }
        }

        // Start the task processor
        startTaskProcessor();
    }

    /**
     * Submit a file processing task to the queue.
     * @param task The file processing task
     */
    public void submitTask(FileProcessing task) {
        taskQueue.offer(task);
        logger.info("Task submitted for file: {}", task.getFileHash());
    }

    /**
     * Start the background thread that processes tasks from the queue.
     */
    private void startTaskProcessor() {
        Thread processorThread = new Thread(() -> {
            while (running) {
                try {
                    FileProcessing task = taskQueue.take(); // This will block if the queue is empty
                    processFileTask(task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error processing task", e);
                }
            }
        });
        processorThread.setDaemon(true);
        processorThread.start();
        logger.info("Task processor started");
    }

    /**
     * Process a file task using an available engine.
     * @param fileProcessing The file processing task
     */
    private void processFileTask(FileProcessing fileProcessing) {
        executorService.submit(() -> {
            IEngine engine = null;
            try {
                engine = enginePool.take(); // This will block if no engine is available
                logger.info("Processing file: {}", fileProcessing.getFileHash());
                IFRDocument frDocument = engine.CreateFRDocument();
                frDocument.AddImageFileFromMemory(fileProcessing.getFileContent(), null, null, null, "");
                
                // Get the pages collection
                IFRPages pages = frDocument.getPages();
                // Process each page
                XDoc xDoc = new XDoc();
                xDoc.setId(fileProcessing.getFileId());
                List<XPage> xPages = new ArrayList<>();
                for (int i = 0; i < pages.getCount(); i++) {
                    IFRPage page = pages.Item(i);
                    IPlainText pageText = page.getPlainText();
                    logger.info("Processed page {}", i + 1);
                    XPage xPage = new XPage();
                    xPage.setText(pageText.getText());
                    xPage.setPageNumber(i + 1);
                    xPages.add(xPage);
                }
                xDoc.setPages(xPages);
                logger.info("Completed processing file: {}", fileProcessing.getFileHash());
                
                if (processedFilesService != null) {
                    var filePath = processedFilesService.saveFile(fileProcessing.getFileContent());

                    ProcessedFiles processedFile = new ProcessedFiles();
                    processedFile.setFileHash(fileProcessing.getFileHash());
                    processedFile.setFileName(fileProcessing.getFileName());
                    processedFile.setFilePath(filePath);
                    processedFile.setFileSize(fileProcessing.getFileSize());
                    processedFile.setAppId(UUID.fromString(fileProcessing.getAppId()));
                    processedFile.setOcrContent(xDoc.toJSON().toString());
                    processedFilesService.addProcessedFile(processedFile);
                } else {
                    logger.warn("ProcessedFilesService is null, cannot save processed file");
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Processing interrupted", e);
            } catch (Exception e) {
                logger.error("Error processing file: {}", fileProcessing.getFileHash(), e);
            } finally {
                if (engine != null) {
                    enginePool.offer(engine); // Return the engine to the pool
                }
            }
        });
    }

    /**
     * Shutdown the engine pool and executor service.
     */
    public void shutdown() {
        running = false;
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            // deinitialize engines
            for (IEngine engine : enginePool) {
                Engine.DeinitializeEngine();
            }
            logger.info("Engine pool shutdown complete");
        } catch (Exception e) {
            logger.error("Error during shutdown", e);
            Thread.currentThread().interrupt();
        }
    }
}
