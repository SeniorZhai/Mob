package com.facebook.share;

public interface Sharer {

    public static class Result {
        final String postId;

        public Result(String postId) {
            this.postId = postId;
        }

        public String getPostId() {
            return this.postId;
        }
    }

    boolean getShouldFailOnDataError();

    void setShouldFailOnDataError(boolean z);
}
