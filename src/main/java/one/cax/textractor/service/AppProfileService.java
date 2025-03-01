package one.cax.textractor.service;


import one.cax.textractor.db.AppProfile;
import one.cax.textractor.db.AppProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AppProfileService {

    private final AppProfileRepository appProfileRepository;

    public AppProfileService(@Autowired  AppProfileRepository appProfileRepository) {
        this.appProfileRepository = appProfileRepository;
    }

    public AppProfile save(AppProfile appProfile) {
        return appProfileRepository.save(appProfile);
    }

    public AppProfile findById(UUID id) {
        return appProfileRepository.findById(id).orElse(null);
    }

    public Iterable<AppProfile> findAll() {
        return appProfileRepository.findAll();
    }

    public void delete(AppProfile appProfile) {
        appProfileRepository.delete(appProfile);
    }

    public AppProfile findByProfileName(String profileName) {
        return appProfileRepository.findByProfileName(profileName).orElse(null);
    }

    public AppProfile findByAppId(String appId) {
        return appProfileRepository.findByAppId(appId).orElse(null);

    }

    public void deleteAppProfile(UUID id) {
        appProfileRepository.deleteById(id);
    }

    // delete by app id
    public void deleteByAppId(String appId) {
        appProfileRepository.deleteByAppId(appId);
    }

}
