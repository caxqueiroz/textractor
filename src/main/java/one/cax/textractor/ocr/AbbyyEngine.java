package one.cax.textractor.ocr;

import com.abbyy.FREngine.Engine;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import one.cax.textractor.config.OcrConfig;
import one.cax.textractor.datamodel.FileProcessing;
import one.cax.textractor.service.ProcessedFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by gaoxiaojun on 2017/3/24.
 */
@Service
public class AbbyyEngine {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${textractor.redis.ocr.topic}")
    private String ocrTopic;

    private final OcrConfig config;

    private AbbyyEnginePool enginesPool;

    private final RedisMessageListenerContainer container;
    private final RedisTemplate<String, FileProcessing> redisTemplate;
    
    // Use AtomicBoolean for thread safety
    private final AtomicBoolean initialized = new AtomicBoolean(false);


    public AbbyyEngine(@Autowired OcrConfig config, RedisMessageListenerContainer container,
                       RedisTemplate<String, FileProcessing> redisTemplate, @Autowired ProcessedFilesService processedFilesService) throws Exception {
        this.config = config;
        this.container = container;
        this.redisTemplate = redisTemplate;
        enginesPool = new AbbyyEnginePool(processedFilesService, config);
    }

    public synchronized void initialize() throws Exception {
        // Check if already initialized
        if (initialized.get()) {
            logger.info("AbbyyEngine already initialized, skipping initialization");
            return;
        }
        
        // Initialize JNI library path
        Engine.SetJNIDllFolder(config.getLibFolder());
        logger.info("Using ABBYY lib folder: {}", config.getLibFolder());
        logger.info("Initializing engines pool...");

        enginesPool.initialize();

        // subscribe to topic
        MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(this, "handleMessage");
        ObjectMapper objectMapper = new ObjectMapper();
        Jackson2JsonRedisSerializer<FileProcessing> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, FileProcessing.class);
        listenerAdapter.setSerializer(serializer);
        container.addMessageListener(listenerAdapter, new org.springframework.data.redis.listener.ChannelTopic(ocrTopic));
        logger.info("Subscribed to Redis topic: {}", ocrTopic);
        
        // Mark as initialized
        initialized.set(true);
        logger.info("AbbyyEngine successfully initialized");
    }

    public void handleMessage(FileProcessing fileProcessing) {
        logger.info("Received message: {}", fileProcessing);
        try {
            enginesPool.submitTask(fileProcessing);
        } catch (Exception e) {
            logger.error("Error processing message", e);
        }
    }

    // private int getAllowedCoresNumber() throws Exception {
    //     int cores = 0;
    //     IEngine engine = null;
    //     try {
    //         engine = Engine.InitializeEngine(config.getLibFolder(), config.getCustomerProjectId(), config.getLicensePath(),
    //                 config.getLicensePassword(), "", "", false);
    //         cores = engine.getCurrentLicense().getAllowedCoresCount();

    //     } finally {
    //         if (engine != null) {
    //             Engine.DeinitializeEngine();
    //         }
    //     }
    //     return cores;
    // }

    @PreDestroy
    public void cleanup() {
        try {
            if (initialized.get() && enginesPool != null) {
                enginesPool.shutdown();
                initialized.set(false);
            }
        } catch (Exception e) {
            logger.warn("Error during cleanup: {}", e.getMessage());
        }
    }
    
    // Method to check if engine is initialized
    public boolean isInitialized() {
        return initialized.get();
    }
}
