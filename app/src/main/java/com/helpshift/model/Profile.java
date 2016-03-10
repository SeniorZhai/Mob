package com.helpshift.model;

import io.fabric.sdk.android.services.events.EventsFilesManager;

public class Profile {
    private String email;
    private Long id;
    private String identifier;
    private String name;
    private String profileId;
    private String salt;

    public Profile(String identifier) {
        this.identifier = identifier;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Long getId() {
        return this.id;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getSaltedIdentifier() {
        return this.identifier + EventsFilesManager.ROLL_OVER_FILE_NAME_SEPARATOR + this.salt;
    }

    public String getProfileId() {
        return this.profileId;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getSalt() {
        return this.salt;
    }

    public String toString() {
        return "id:" + this.id + ", identifier:" + this.identifier + ", profileId:" + this.profileId + ", name:" + this.name + ", email:" + this.email + ", salt:" + this.salt;
    }
}
