package com.helpshift;

import io.fabric.sdk.android.BuildConfig;

public class Section {
    private long id;
    private String publish_id;
    private String section_id;
    private String title;

    public Section() {
        this.id = -1;
        this.section_id = BuildConfig.FLAVOR;
        this.publish_id = BuildConfig.FLAVOR;
        this.title = BuildConfig.FLAVOR;
    }

    public Section(long id, String sectionId, String title, String publish_id) {
        this.id = id;
        this.section_id = sectionId;
        this.title = title;
        this.publish_id = publish_id;
    }

    public Section(String title, String publish_id) {
        this.id = -1;
        this.title = title;
        this.publish_id = publish_id;
    }

    public Section(String id, String title, String publish_id) {
        this.id = -1;
        this.section_id = id;
        this.title = title;
        this.publish_id = publish_id;
    }

    public String toString() {
        return this.title;
    }

    public String getPublishId() {
        return this.publish_id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSectionId() {
        return this.section_id;
    }

    public boolean equals(Object otherObj) {
        Section other = (Section) otherObj;
        if (this.title.equals(other.title) && this.publish_id.equals(other.publish_id) && this.section_id.equals(other.section_id)) {
            return true;
        }
        return false;
    }
}
