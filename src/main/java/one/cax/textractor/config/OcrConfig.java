package one.cax.textractor.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OcrConfig {

    @Value("${ABBYY_ENGINE_PROFILE:TextExtraction_Accuracy}")
    private String profile;

    @Value("${ABBYY_LIB_FOLDER:}")
    private String libFolder;

    @Value("${ABBYY_CUSTOMER_PROJECT_ID:}")
    private String customerProjectId;

    @Value("${ABBYY_LICENSE_PATH:}")
    private String licensePath;

    @Value("${ABBYY_LICENSE_PASSWORD:}")
    private String licensePassword;


    public String getProfile() {
        return profile;
    }

    public String getLibFolder() {
        if (!is64BitJVMArchitecture()) {
            return "Directory\\where\\x86\\lib\\resides";
        }
        return libFolder;
    }


    public String getCustomerProjectId() {
        return customerProjectId;
    }

    public String getLicensePath() {
        return licensePath;
    }

    public String getLicensePassword() {
        return licensePassword;
    }

    // Determines whether the JVM architecture is a 64-bit architecture
    public boolean is64BitJVMArchitecture() {
        return checkArchitecture();
    }

    // This method can be overridden in tests
    protected String getSystemProperty(String key) {
        return System.getProperty(key);
    }

    private boolean checkArchitecture() {
        // Check if it's 64-bit
        String dataModel = getSystemProperty("sun.arch.data.model");
        String arch = getSystemProperty("os.arch");

        // First check if it's 64-bit
        boolean is64Bit = (dataModel != null && dataModel.contains("64")) ||
                (arch != null && arch.contains("64"));

        // Then check if it's x86/amd64 architecture
        boolean isX86 = (arch != null &&
                (arch.contains("amd64") ||
                        arch.contains("x86_64") ||
                        arch.contains("x86") ||
                        arch.contains("i386")));

        // Return true only if both 64-bit and x86 architecture
        return is64Bit && isX86;
    }

}
