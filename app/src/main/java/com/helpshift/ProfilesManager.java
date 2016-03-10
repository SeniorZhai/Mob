package com.helpshift;

import android.content.Context;
import com.helpshift.model.Profile;
import com.helpshift.storage.ProfilesDataSource;
import java.util.UUID;

public class ProfilesManager {
    private static final String TAG = "HelpShiftDebug";
    private ProfilesDataSource storage;

    private static class Holder {
        private static final ProfilesManager INSTANCE = new ProfilesManager(HelpshiftContext.getApplicationContext());

        private Holder() {
        }
    }

    public ProfilesManager(Context context) {
        this.storage = new ProfilesDataSource(context);
    }

    public static ProfilesManager getInstance() {
        return Holder.INSTANCE;
    }

    public Profile getProfile(String identifier) {
        Profile profile = this.storage.getProfile(identifier);
        if (profile != null) {
            return profile;
        }
        profile = new Profile(identifier);
        profile.setSalt(UUID.randomUUID().toString());
        return profile;
    }

    public String getProfileId(String identifier) {
        return getProfile(identifier).getProfileId();
    }

    public void setProfileId(String identifier, String profileId) {
        Profile profile = getProfile(identifier);
        profile.setProfileId(profileId);
        this.storage.addProfile(profile);
    }

    public void setName(String identifier, String name) {
        Profile profile = getProfile(identifier);
        profile.setName(name);
        this.storage.addProfile(profile);
    }

    public String getName(String identifier) {
        return getProfile(identifier).getName();
    }

    public void setEmail(String identifier, String email) {
        Profile profile = getProfile(identifier);
        profile.setEmail(email);
        this.storage.addProfile(profile);
    }

    public String getEmail(String identifier) {
        return getProfile(identifier).getEmail();
    }
}
