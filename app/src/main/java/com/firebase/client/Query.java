package com.firebase.client;

import com.firebase.client.core.ChildEventRegistration;
import com.firebase.client.core.EventRegistration;
import com.firebase.client.core.Path;
import com.firebase.client.core.Repo;
import com.firebase.client.core.ValueEventRegistration;
import com.firebase.client.core.view.QueryParams;
import com.firebase.client.core.view.QuerySpec;
import com.firebase.client.snapshot.BooleanNode;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.DoubleNode;
import com.firebase.client.snapshot.EmptyNode;
import com.firebase.client.snapshot.KeyIndex;
import com.firebase.client.snapshot.Node;
import com.firebase.client.snapshot.PriorityIndex;
import com.firebase.client.snapshot.PriorityUtilities;
import com.firebase.client.snapshot.StringNode;
import com.firebase.client.snapshot.SubKeyIndex;
import com.firebase.client.snapshot.ValueIndex;
import com.firebase.client.utilities.Validation;

public class Query {
    static final /* synthetic */ boolean $assertionsDisabled = (!Query.class.desiredAssertionStatus());
    private final boolean orderByCalled;
    protected final QueryParams params;
    protected final Path path;
    protected final Repo repo;

    private static class SingleEventProgress {
        private boolean called;

        private SingleEventProgress() {
            this.called = false;
        }

        public boolean hasBeenCalled() {
            return this.called;
        }

        public void setCalled() {
            this.called = true;
        }
    }

    Query(Repo repo, Path path, QueryParams params, boolean orderByCalled) throws FirebaseException {
        this.repo = repo;
        this.path = path;
        this.params = params;
        this.orderByCalled = orderByCalled;
        if (!params.isValid()) {
            throw new FirebaseException("Validation of queries failed. Please report to support@firebase.com");
        }
    }

    Query(Repo repo, Path path) {
        this.repo = repo;
        this.path = path;
        this.params = QueryParams.DEFAULT_PARAMS;
        this.orderByCalled = false;
    }

    private void validateQueryEndpoints(QueryParams params) {
        if (params.getIndex().equals(KeyIndex.getInstance())) {
            String message = "You must use startAt(String value), endAt(String value) or equalTo(String value) in combination with orderByKey(). Other type of values or using the version with 2 parameters is not supported";
            if (params.hasStart()) {
                Node startNode = params.getIndexStartValue();
                if (!(params.getIndexStartName() == ChildKey.getMinName() && (startNode instanceof StringNode))) {
                    throw new IllegalArgumentException(message);
                }
            }
            if (params.hasEnd()) {
                Node endNode = params.getIndexEndValue();
                if (params.getIndexEndName() != ChildKey.getMaxName() || !(endNode instanceof StringNode)) {
                    throw new IllegalArgumentException(message);
                }
            }
        } else if (!params.getIndex().equals(PriorityIndex.getInstance())) {
        } else {
            if ((params.hasStart() && !PriorityUtilities.isValidPriority(params.getIndexStartValue())) || (params.hasEnd() && !PriorityUtilities.isValidPriority(params.getIndexEndValue()))) {
                throw new IllegalArgumentException("When using orderByPriority(), values provided to startAt(), endAt(), or equalTo() must be valid priorities.");
            }
        }
    }

    private void validateLimit(QueryParams params) {
        if (params.hasStart() && params.hasEnd() && params.hasLimit() && !params.hasAnchoredLimit()) {
            throw new IllegalArgumentException("Can't combine startAt(), endAt() and limit(). Use limitToFirst() or limitToLast() instead");
        }
    }

    private void validateEqualToCall() {
        if (this.params.hasStart()) {
            throw new IllegalArgumentException("Can't call equalTo() and startAt() combined");
        } else if (this.params.hasEnd()) {
            throw new IllegalArgumentException("Can't call equalTo() and endAt() combined");
        }
    }

    private void validateNoOrderByCall() {
        if (this.orderByCalled) {
            throw new IllegalArgumentException("You can't combine multiple orderBy calls!");
        }
    }

    public ValueEventListener addValueEventListener(ValueEventListener listener) {
        addEventCallback(new ValueEventRegistration(this.repo, listener));
        return listener;
    }

    public ChildEventListener addChildEventListener(ChildEventListener listener) {
        addEventCallback(new ChildEventRegistration(this.repo, listener));
        return listener;
    }

    public void addListenerForSingleValueEvent(final ValueEventListener listener) {
        final SingleEventProgress progress = new SingleEventProgress();
        addEventCallback(new ValueEventRegistration(this.repo, new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                if (!progress.hasBeenCalled()) {
                    progress.setCalled();
                    Query.this.removeEventListener((ValueEventListener) this);
                    listener.onDataChange(snapshot);
                }
            }

            public void onCancelled(FirebaseError error) {
                listener.onCancelled(error);
            }
        }));
    }

    public void removeEventListener(final ValueEventListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener must not be null");
        }
        this.repo.scheduleNow(new Runnable() {
            public void run() {
                Query.this.repo.removeEventCallback(Query.this.getSpec(), new ValueEventRegistration(Query.this.repo, listener));
            }
        });
    }

    public void removeEventListener(final ChildEventListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener must not be null");
        }
        this.repo.scheduleNow(new Runnable() {
            public void run() {
                Query.this.repo.removeEventCallback(Query.this.getSpec(), new ChildEventRegistration(Query.this.repo, listener));
            }
        });
    }

    public void keepSynced(final boolean keepSynced) {
        if (this.path.getFront().equals(ChildKey.getInfoKey())) {
            throw new FirebaseException("Can't call keepSynced() on .info paths.");
        }
        this.repo.scheduleNow(new Runnable() {
            public void run() {
                Query.this.repo.keepSynced(Query.this.getSpec(), keepSynced);
            }
        });
    }

    public Query startAt() {
        return startAt(EmptyNode.Empty(), null);
    }

    public Query startAt(String value) {
        return startAt(value, null);
    }

    public Query startAt(double value) {
        return startAt(value, null);
    }

    public Query startAt(boolean value) {
        return startAt(value, null);
    }

    public Query startAt(String value, String key) {
        return startAt(value != null ? new StringNode(value, PriorityUtilities.NullPriority()) : EmptyNode.Empty(), key);
    }

    public Query startAt(double value, String key) {
        return startAt(new DoubleNode(Double.valueOf(value), PriorityUtilities.NullPriority()), key);
    }

    public Query startAt(boolean value, String key) {
        return startAt(new BooleanNode(Boolean.valueOf(value), PriorityUtilities.NullPriority()), key);
    }

    private Query startAt(Node node, String key) {
        Validation.validateNullableKey(key);
        if (!node.isLeafNode() && !node.isEmpty()) {
            throw new IllegalArgumentException("Can only use simple values for startAt()");
        } else if (this.params.hasStart()) {
            throw new IllegalArgumentException("Can't call startAt() or equalTo() multiple times");
        } else {
            QueryParams newParams = this.params.startAt(node, key != null ? ChildKey.fromString(key) : null);
            validateLimit(newParams);
            validateQueryEndpoints(newParams);
            if ($assertionsDisabled || newParams.isValid()) {
                return new Query(this.repo, this.path, newParams, this.orderByCalled);
            }
            throw new AssertionError();
        }
    }

    public Query endAt() {
        return endAt(Node.MAX_NODE, null);
    }

    public Query endAt(String value) {
        return endAt(value, null);
    }

    public Query endAt(double value) {
        return endAt(value, null);
    }

    public Query endAt(boolean value) {
        return endAt(value, null);
    }

    public Query endAt(String value, String key) {
        return endAt(value != null ? new StringNode(value, PriorityUtilities.NullPriority()) : EmptyNode.Empty(), key);
    }

    public Query endAt(double value, String key) {
        return endAt(new DoubleNode(Double.valueOf(value), PriorityUtilities.NullPriority()), key);
    }

    public Query endAt(boolean value, String key) {
        return endAt(new BooleanNode(Boolean.valueOf(value), PriorityUtilities.NullPriority()), key);
    }

    private Query endAt(Node node, String key) {
        Validation.validateNullableKey(key);
        if (node.isLeafNode() || node.isEmpty()) {
            ChildKey childKey = key != null ? ChildKey.fromString(key) : null;
            if (this.params.hasEnd()) {
                throw new IllegalArgumentException("Can't call endAt() or equalTo() multiple times");
            }
            QueryParams newParams = this.params.endAt(node, childKey);
            validateLimit(newParams);
            validateQueryEndpoints(newParams);
            if ($assertionsDisabled || newParams.isValid()) {
                return new Query(this.repo, this.path, newParams, this.orderByCalled);
            }
            throw new AssertionError();
        }
        throw new IllegalArgumentException("Can only use simple values for endAt()");
    }

    public Query equalTo(String value) {
        validateEqualToCall();
        return startAt(value).endAt(value);
    }

    public Query equalTo(double value) {
        validateEqualToCall();
        return startAt(value).endAt(value);
    }

    public Query equalTo(boolean value) {
        validateEqualToCall();
        return startAt(value).endAt(value);
    }

    public Query equalTo(String value, String key) {
        validateEqualToCall();
        return startAt(value, key).endAt(value, key);
    }

    public Query equalTo(double value, String key) {
        validateEqualToCall();
        return startAt(value, key).endAt(value, key);
    }

    public Query equalTo(boolean value, String key) {
        validateEqualToCall();
        return startAt(value, key).endAt(value, key);
    }

    @Deprecated
    public Query limit(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be a positive integer!");
        } else if (this.params.hasLimit()) {
            throw new IllegalArgumentException("Can't call limitToLast on query with previously set limit!");
        } else {
            QueryParams newParams = this.params.limit(limit);
            validateLimit(newParams);
            return new Query(this.repo, this.path, newParams, this.orderByCalled);
        }
    }

    public Query limitToFirst(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be a positive integer!");
        } else if (!this.params.hasLimit()) {
            return new Query(this.repo, this.path, this.params.limitToFirst(limit), this.orderByCalled);
        } else {
            throw new IllegalArgumentException("Can't call limitToLast on query with previously set limit!");
        }
    }

    public Query limitToLast(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be a positive integer!");
        } else if (!this.params.hasLimit()) {
            return new Query(this.repo, this.path, this.params.limitToLast(limit), this.orderByCalled);
        } else {
            throw new IllegalArgumentException("Can't call limitToLast on query with previously set limit!");
        }
    }

    public Query orderByChild(String childKey) {
        if (childKey == null) {
            throw new NullPointerException("Key can't be null");
        } else if (childKey.equals("$key") || childKey.equals(".key")) {
            throw new IllegalArgumentException("Can't use '" + childKey + "' as childKey, please use orderByKey() instead!");
        } else if (childKey.equals("$priority") || childKey.equals(".priority")) {
            throw new IllegalArgumentException("Can't use '" + childKey + "' as childKey, please use orderByPriority() instead!");
        } else if (childKey.equals("$value") || childKey.equals(".value")) {
            throw new IllegalArgumentException("Can't use '" + childKey + "' as childKey, please use orderByValue() instead!");
        } else {
            Validation.validateNullableKey(childKey);
            validateNoOrderByCall();
            return new Query(this.repo, this.path, this.params.orderBy(new SubKeyIndex(ChildKey.fromString(childKey))), true);
        }
    }

    public Query orderByPriority() {
        validateNoOrderByCall();
        QueryParams newParams = this.params.orderBy(PriorityIndex.getInstance());
        validateQueryEndpoints(newParams);
        return new Query(this.repo, this.path, newParams, true);
    }

    public Query orderByKey() {
        validateNoOrderByCall();
        QueryParams newParams = this.params.orderBy(KeyIndex.getInstance());
        validateQueryEndpoints(newParams);
        return new Query(this.repo, this.path, newParams, true);
    }

    public Query orderByValue() {
        validateNoOrderByCall();
        return new Query(this.repo, this.path, this.params.orderBy(ValueIndex.getInstance()), true);
    }

    public Firebase getRef() {
        return new Firebase(this.repo, getPath());
    }

    private void addEventCallback(final EventRegistration listener) {
        this.repo.scheduleNow(new Runnable() {
            public void run() {
                Query.this.repo.addEventCallback(Query.this.getSpec(), listener);
            }
        });
    }

    public Path getPath() {
        return this.path;
    }

    public Repo getRepo() {
        return this.repo;
    }

    public QuerySpec getSpec() {
        return new QuerySpec(this.path, this.params);
    }
}
