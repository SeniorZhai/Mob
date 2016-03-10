package com.firebase.client.core.operation;

import com.firebase.client.core.view.QueryParams;

public class OperationSource {
    static final /* synthetic */ boolean $assertionsDisabled = (!OperationSource.class.desiredAssertionStatus());
    public static final OperationSource SERVER = new OperationSource(Source.Server, null, false);
    public static final OperationSource USER = new OperationSource(Source.User, null, false);
    private final QueryParams queryParams;
    private final Source source;
    private final boolean tagged;

    private enum Source {
        User,
        Server
    }

    public static OperationSource forServerTaggedQuery(QueryParams queryParams) {
        return new OperationSource(Source.Server, queryParams, true);
    }

    public OperationSource(Source source, QueryParams queryParams, boolean tagged) {
        this.source = source;
        this.queryParams = queryParams;
        this.tagged = tagged;
        if (!$assertionsDisabled && tagged && !isFromServer()) {
            throw new AssertionError();
        }
    }

    public boolean isFromUser() {
        return this.source == Source.User;
    }

    public boolean isFromServer() {
        return this.source == Source.Server;
    }

    public boolean isTagged() {
        return this.tagged;
    }

    public String toString() {
        return "OperationSource{source=" + this.source + ", queryParams=" + this.queryParams + ", tagged=" + this.tagged + '}';
    }

    public QueryParams getQueryParams() {
        return this.queryParams;
    }
}
