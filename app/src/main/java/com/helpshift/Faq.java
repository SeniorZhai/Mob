package com.helpshift;

import io.fabric.sdk.android.BuildConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class Faq {
    private String body;
    private long id;
    private int is_helpful;
    private Boolean is_rtl;
    private String publish_id;
    private String qId;
    private ArrayList<String> searchTerms;
    private String section_publish_id;
    private List<String> tags;
    private String title;
    private String type;

    public Faq() {
        this.title = BuildConfig.FLAVOR;
        this.publish_id = BuildConfig.FLAVOR;
        this.type = BuildConfig.FLAVOR;
        this.body = BuildConfig.FLAVOR;
        this.section_publish_id = BuildConfig.FLAVOR;
        this.is_helpful = 0;
        this.is_rtl = Boolean.valueOf(false);
        this.tags = new ArrayList();
    }

    public Faq(String title, String publish_id, String type) {
        this.title = title;
        this.publish_id = publish_id;
        this.type = type;
    }

    public Faq(long id, String qId, String publish_id, String sectionId, String title, String body, int isHelpful, Boolean isRtl, List<String> tags) {
        this.id = id;
        this.qId = qId;
        this.title = title;
        this.publish_id = publish_id;
        this.type = "faq";
        this.section_publish_id = sectionId;
        this.body = body;
        this.is_helpful = isHelpful;
        this.is_rtl = isRtl;
        this.tags = tags;
    }

    public String toString() {
        return this.title;
    }

    public String getId() {
        return this.qId;
    }

    public String getPublishId() {
        return this.publish_id;
    }

    public String getSectionPublishId() {
        return this.section_publish_id;
    }

    protected String getType() {
        return this.type;
    }

    public String getTitle() {
        return this.title;
    }

    public String getBody() {
        return this.body;
    }

    public int getIsHelpful() {
        return this.is_helpful;
    }

    public List<String> getTags() {
        if (this.tags == null) {
            return new ArrayList();
        }
        return this.tags;
    }

    public Boolean getIsRtl() {
        return this.is_rtl;
    }

    public ArrayList<String> getSearchTerms() {
        return this.searchTerms;
    }

    public void setId(long id) {
        this.id = id;
    }

    protected void clearSearchTerms() {
        this.searchTerms = null;
    }

    protected void addSearchTerms(ArrayList<String> searchTerms) {
        this.searchTerms = mergeSearchTerms(this.searchTerms, searchTerms);
    }

    private static ArrayList<String> mergeSearchTerms(ArrayList<String> searchTerms1, ArrayList<String> searchTerms2) {
        HashSet<String> searchTermsSet = new HashSet();
        if (searchTerms1 != null) {
            searchTermsSet.addAll(searchTerms1);
        }
        if (searchTerms2 != null) {
            searchTermsSet.addAll(searchTerms2);
        }
        return new ArrayList(searchTermsSet);
    }

    public boolean equals(Object otherObj) {
        Faq other = (Faq) otherObj;
        if (this.qId.equals(other.qId) && this.title.equals(other.title) && this.body.equals(other.body) && this.publish_id.equals(other.publish_id) && this.section_publish_id.equals(other.section_publish_id) && this.is_rtl == other.is_rtl && this.is_helpful == this.is_helpful && this.tags.equals(other.tags)) {
            return true;
        }
        return false;
    }
}
