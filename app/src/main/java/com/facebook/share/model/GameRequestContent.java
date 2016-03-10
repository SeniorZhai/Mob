package com.facebook.share.model;

import android.os.Parcel;
import java.util.ArrayList;

public final class GameRequestContent implements ShareModel {
    private final ActionType actionType;
    private final String data;
    private final Filters filters;
    private final String message;
    private final String objectId;
    private final ArrayList<String> suggestions;
    private final String title;
    private final String to;

    public enum ActionType {
        SEND,
        ASKFOR,
        TURN
    }

    public static class Builder implements ShareModelBuilder<GameRequestContent, Builder> {
        private ActionType actionType;
        private String data;
        private Filters filters;
        private String message;
        private String objectId;
        private ArrayList<String> suggestions;
        private String title;
        private String to;

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setTo(String to) {
            this.to = to;
            return this;
        }

        public Builder setData(String data) {
            this.data = data;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setActionType(ActionType actionType) {
            this.actionType = actionType;
            return this;
        }

        public Builder setObjectId(String objectId) {
            this.objectId = objectId;
            return this;
        }

        public Builder setFilters(Filters filters) {
            this.filters = filters;
            return this;
        }

        public Builder setSuggestions(ArrayList<String> suggestions) {
            this.suggestions = suggestions;
            return this;
        }

        public GameRequestContent build() {
            return new GameRequestContent();
        }

        public Builder readFrom(GameRequestContent content) {
            return content == null ? this : setMessage(content.getMessage()).setTo(content.getTo()).setTitle(content.getTitle()).setData(content.getData()).setActionType(content.getActionType()).setObjectId(content.getObjectId()).setFilters(content.getFilters()).setSuggestions(content.getSuggestions());
        }

        public Builder readFrom(Parcel parcel) {
            return readFrom((GameRequestContent) parcel.readParcelable(GameRequestContent.class.getClassLoader()));
        }
    }

    public enum Filters {
        APP_USERS,
        APP_NON_USERS
    }

    private GameRequestContent(Builder builder) {
        this.message = builder.message;
        this.to = builder.to;
        this.title = builder.title;
        this.data = builder.data;
        this.actionType = builder.actionType;
        this.objectId = builder.objectId;
        this.filters = builder.filters;
        this.suggestions = builder.suggestions;
    }

    GameRequestContent(Parcel in) {
        this.message = in.readString();
        this.to = in.readString();
        this.title = in.readString();
        this.data = in.readString();
        this.actionType = ActionType.valueOf(in.readString());
        this.objectId = in.readString();
        this.filters = Filters.valueOf(in.readString());
        this.suggestions = new ArrayList();
        in.readStringList(this.suggestions);
    }

    public String getMessage() {
        return this.message;
    }

    public String getTo() {
        return this.to;
    }

    public String getTitle() {
        return this.title;
    }

    public String getData() {
        return this.data;
    }

    public ActionType getActionType() {
        return this.actionType;
    }

    public String getObjectId() {
        return this.objectId;
    }

    public Filters getFilters() {
        return this.filters;
    }

    public ArrayList<String> getSuggestions() {
        return this.suggestions;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.message);
        out.writeString(this.to);
        out.writeString(this.title);
        out.writeString(this.data);
        out.writeString(getActionType().toString());
        out.writeString(getObjectId());
        out.writeString(getFilters().toString());
        out.writeStringList(getSuggestions());
    }
}
