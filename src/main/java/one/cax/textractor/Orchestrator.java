package one.cax.textractor;


import one.cax.textractor.datamodel.FileProcessing;
import one.cax.textractor.datamodel.ProcessingStatus;
import one.cax.textractor.ocr.AbbyyEngine;
import one.cax.textractor.service.AppProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class Orchestrator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${textractor.redis.ocr.topic}")
    private String ocrTopic;


    private final RedisTemplate<String, String> redisTemplate;


    /** Abbyy OCR engine */
    private AbbyyEngine abbyyEngine;

    /** AppProfile service */
    private AppProfileService appProfileService;


    /**
     *
     * @param abbyyEngine
     * @throws Exception
     */
    public Orchestrator(RedisTemplate<String, String> redisTemplate, @Autowired  AbbyyEngine abbyyEngine) throws Exception {
        this.redisTemplate = redisTemplate;
        this.abbyyEngine = abbyyEngine;
        this.abbyyEngine.initialize();

    }




    public void setAppProfileService(@Autowired AppProfileService appProfileService) {
        this.appProfileService = appProfileService;
    }


    public UUID process(FileProcessing fileProcessing) {
        var fileId = fileProcessing.initialize();
        logger.info("Starting processing for file {}", fileId);
        redisTemplate.convertAndSend(ocrTopic, fileProcessing);
        return fileId;
    }

    public boolean appIdIsValid(String appId) {

        return appProfileService.findByAppId(appId) != null;
    }




    @Autowired
    public void setAbbyyEngine(AbbyyEngine abbyyEngine) {
        this.abbyyEngine = abbyyEngine;
    }

    public ProcessingStatus getProcessingStatus(String fileId) {

        return null;
    }
}
