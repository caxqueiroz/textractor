package one.cax.textractor.ocr;

import com.abbyy.FREngine.Engine;
import com.abbyy.FREngine.IEngine;
import one.cax.textractor.config.OcrConfig;
import one.cax.textractor.datamodel.FileProcessing;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AbbyyEngine {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final OcrConfig config;

    private AbbyyEnginePool enginesPool;


    public AbbyyEngine( @Autowired OcrConfig config) throws Exception {
        this.config = config;

    }

    public String process(FileProcessing fileProcessing) throws Exception {
        return null;
    }

    public void initialize() throws Exception {
        // Initialize JNI library path
        Engine.SetJNIDllFolder(config.getLibFolder());
        logger.info("Using ABBYY lib folder: {}", config.getLibFolder());
        logger.info("Initializing engines pool...");
        enginesPool = new AbbyyEnginePool(config);
        enginesPool.initialize();

    }


    private int getAllowedCoresNumber() throws Exception {
        int cores = 0;
        IEngine engine = null;
        try {
            engine = Engine.InitializeEngine(config.getLibFolder(), config.getCustomerProjectId(), config.getLicensePath(),
                    config.getLicensePassword(), "", "", false);
            cores = engine.getCurrentLicense().getAllowedCoresCount();

        } finally {
            if (engine != null) {
                Engine.DeinitializeEngine();
            }
        }
        return cores;
    }

    @PreDestroy
    public void cleanup() {
        try {
            enginesPool.shutdown();
        } catch (Exception e) {
            logger.warn("Error during cleanup: {}", e.getMessage());
        }
    }
}
