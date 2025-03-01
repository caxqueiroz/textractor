package one.cax.textractor.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("app_profiles")
public class AppProfile {

    @Id
    private UUID id;

    private String profileName;
    private String ProfileDescription;

    public UUID getId() {
        return id;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getProfileDescription() {
        return ProfileDescription;
    }

    public String getAppId() {
        return id.toString().replace("-", "").substring(0, 6);
    }

}
