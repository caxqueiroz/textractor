package one.cax.textractor.db;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "app_profiles")
public class AppProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "profile_name")
    private String profileName;
    
    @Column(name = "profile_description")
    private String profileDescription;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileDescription() {
        return profileDescription;
    }

    public void setProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }

    public String getAppId() {
        return id.toString().replace("-", "").substring(0, 6);
    }

}
