package one.cax.textractor;


import one.cax.textractor.datamodel.FileProcessing;
import one.cax.textractor.datamodel.ProcessingStatus;
import one.cax.textractor.ocr.AbbyyEngine;
import one.cax.textractor.db.AppProfile;
import one.cax.textractor.service.AppProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class Orchestrator {

    /** Abbyy OCR engine */
    private AbbyyEngine abbyyEngine;

    /** AppProfile service */
    private AppProfileService appProfileService;


    /**
     *
     * @param abbyyEngine
     * @throws Exception
     */
    public Orchestrator(@Autowired  AbbyyEngine abbyyEngine) throws Exception {
        this.abbyyEngine = abbyyEngine;
        this.abbyyEngine.initialize();
    }

    public void setAppProfileService(@Autowired AppProfileService appProfileService) {
        this.appProfileService = appProfileService;
    }


    public UUID initialize(FileProcessing fileProcessing) {
        return fileProcessing.initialize();
    }

    public boolean appIdIsValid(String appId) {

        return appProfileService.findByAppId(appId) != null;
    }

    /**
     * Process the file uploaded.
     * @param appId
     * @param file
     * @return the file as json string
     */
    public String process() throws Exception {

        AppProfile appProfile = appProfileService.findByAppId(appId);

        if (appProfile != null) {

            return abbyyEngine.process(fileProcessing);
        }

        return null;
    }


    @Autowired
    public void setAbbyyEngine(AbbyyEngine abbyyEngine) {
        this.abbyyEngine = abbyyEngine;
    }

    public ProcessingStatus getProcessingStatus(String fileId) {

        return null;
    }
}
