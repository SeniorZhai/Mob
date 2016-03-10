package com.firebase.client.core.persistence;

import com.firebase.client.core.CompoundWrite;
import com.firebase.client.core.Context;
import com.firebase.client.core.Path;
import com.firebase.client.core.UserWriteRecord;
import com.firebase.client.core.view.CacheNode;
import com.firebase.client.core.view.QuerySpec;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.EmptyNode;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.client.snapshot.Node;
import com.firebase.client.utilities.Clock;
import com.firebase.client.utilities.DefaultClock;
import com.firebase.client.utilities.LogWrapper;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

public class DefaultPersistenceManager implements PersistenceManager {
    static final /* synthetic */ boolean $assertionsDisabled = (!DefaultPersistenceManager.class.desiredAssertionStatus());
    private final CachePolicy cachePolicy;
    private final LogWrapper logger;
    private long serverCacheUpdatesSinceLastPruneCheck;
    private final PersistenceStorageEngine storageLayer;
    private final TrackedQueryManager trackedQueryManager;

    public DefaultPersistenceManager(Context ctx, PersistenceStorageEngine engine, CachePolicy cachePolicy) {
        this(ctx, engine, cachePolicy, new DefaultClock());
    }

    public DefaultPersistenceManager(Context ctx, PersistenceStorageEngine engine, CachePolicy cachePolicy, Clock clock) {
        this.serverCacheUpdatesSinceLastPruneCheck = 0;
        this.storageLayer = engine;
        this.logger = ctx.getLogger("Persistence");
        this.trackedQueryManager = new TrackedQueryManager(this.storageLayer, this.logger, clock);
        this.cachePolicy = cachePolicy;
    }

    public void saveUserOverwrite(Path path, Node node, long writeId) {
        this.storageLayer.saveUserOverwrite(path, node, writeId);
    }

    public void saveUserMerge(Path path, CompoundWrite children, long writeId) {
        this.storageLayer.saveUserMerge(path, children, writeId);
    }

    public void removeUserWrite(long writeId) {
        this.storageLayer.removeUserWrite(writeId);
    }

    public void removeAllUserWrites() {
        this.storageLayer.removeAllUserWrites();
    }

    public void applyUserWriteToServerCache(Path path, Node node) {
        if (!this.trackedQueryManager.hasActiveDefaultQuery(path)) {
            this.storageLayer.overwriteServerCache(path, node);
            this.trackedQueryManager.ensureCompleteTrackedQuery(path);
        }
    }

    public void applyUserWriteToServerCache(Path path, CompoundWrite merge) {
        Iterator i$ = merge.iterator();
        while (i$.hasNext()) {
            Entry<Path, Node> write = (Entry) i$.next();
            applyUserWriteToServerCache(path.child((Path) write.getKey()), (Node) write.getValue());
        }
    }

    public List<UserWriteRecord> loadUserWrites() {
        return this.storageLayer.loadUserWrites();
    }

    public CacheNode serverCache(QuerySpec query) {
        boolean complete;
        Set<ChildKey> trackedKeys;
        if (this.trackedQueryManager.isQueryComplete(query)) {
            complete = true;
            TrackedQuery trackedQuery = this.trackedQueryManager.findTrackedQuery(query);
            if (query.loadsAllData() || trackedQuery == null || !trackedQuery.complete) {
                trackedKeys = null;
            } else {
                trackedKeys = this.storageLayer.loadTrackedQueryKeys(trackedQuery.id);
            }
        } else {
            complete = false;
            trackedKeys = this.trackedQueryManager.getKnownCompleteChildren(query.getPath());
        }
        Node serverCacheNode = this.storageLayer.serverCache(query.getPath());
        if (trackedKeys == null) {
            return new CacheNode(IndexedNode.from(serverCacheNode, query.getIndex()), complete, false);
        }
        Node filteredNode = EmptyNode.Empty();
        for (ChildKey key : trackedKeys) {
            filteredNode = filteredNode.updateImmediateChild(key, serverCacheNode.getImmediateChild(key));
        }
        return new CacheNode(IndexedNode.from(filteredNode, query.getIndex()), complete, true);
    }

    public void updateServerCache(QuerySpec query, Node node) {
        if (query.loadsAllData()) {
            this.storageLayer.overwriteServerCache(query.getPath(), node);
        } else {
            this.storageLayer.mergeIntoServerCache(query.getPath(), node);
        }
        setQueryComplete(query);
        doPruneCheckAfterServerUpdate();
    }

    public void updateServerCache(Path path, CompoundWrite children) {
        this.storageLayer.mergeIntoServerCache(path, children);
        doPruneCheckAfterServerUpdate();
    }

    public void setQueryActive(QuerySpec query) {
        this.trackedQueryManager.setQueryActive(query);
    }

    public void setQueryInactive(QuerySpec query) {
        this.trackedQueryManager.setQueryInactive(query);
    }

    public void setQueryComplete(QuerySpec query) {
        if (query.loadsAllData()) {
            this.trackedQueryManager.setQueriesComplete(query.getPath());
        } else {
            this.trackedQueryManager.setQueryCompleteIfExists(query);
        }
    }

    public void setTrackedQueryKeys(QuerySpec query, Set<ChildKey> keys) {
        if ($assertionsDisabled || !query.loadsAllData()) {
            TrackedQuery trackedQuery = this.trackedQueryManager.findTrackedQuery(query);
            if ($assertionsDisabled || (trackedQuery != null && trackedQuery.active)) {
                this.storageLayer.saveTrackedQueryKeys(trackedQuery.id, keys);
                return;
            }
            throw new AssertionError("We only expect tracked keys for currently-active queries.");
        }
        throw new AssertionError("We should only track keys for filtered queries.");
    }

    public void updateTrackedQueryKeys(QuerySpec query, Set<ChildKey> added, Set<ChildKey> removed) {
        if ($assertionsDisabled || !query.loadsAllData()) {
            TrackedQuery trackedQuery = this.trackedQueryManager.findTrackedQuery(query);
            if ($assertionsDisabled || (trackedQuery != null && trackedQuery.active)) {
                this.storageLayer.updateTrackedQueryKeys(trackedQuery.id, added, removed);
                return;
            }
            throw new AssertionError("We only expect tracked keys for currently-active queries.");
        }
        throw new AssertionError("We should only track keys for filtered queries.");
    }

    public <T> T runInTransaction(Callable<T> callable) {
        try {
            this.storageLayer.beginTransaction();
            T result = callable.call();
            this.storageLayer.setTransactionSuccessful();
            this.storageLayer.endTransaction();
            return result;
        } catch (Throwable th) {
            this.storageLayer.endTransaction();
        }
    }

    private void doPruneCheckAfterServerUpdate() {
        this.serverCacheUpdatesSinceLastPruneCheck++;
        if (this.cachePolicy.shouldCheckCacheSize(this.serverCacheUpdatesSinceLastPruneCheck)) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Reached prune check threshold.");
            }
            this.serverCacheUpdatesSinceLastPruneCheck = 0;
            boolean canPrune = true;
            long cacheSize = this.storageLayer.serverCacheEstimatedSizeInBytes();
            if (this.logger.logsDebug()) {
                this.logger.debug("Cache size: " + cacheSize);
            }
            while (canPrune && this.cachePolicy.shouldPrune(cacheSize, this.trackedQueryManager.countOfPrunableQueries())) {
                PruneForest pruneForest = this.trackedQueryManager.pruneOldQueries(this.cachePolicy);
                if (pruneForest.prunesAnything()) {
                    this.storageLayer.pruneCache(Path.getEmptyPath(), pruneForest);
                } else {
                    canPrune = false;
                }
                cacheSize = this.storageLayer.serverCacheEstimatedSizeInBytes();
                if (this.logger.logsDebug()) {
                    this.logger.debug("Cache size after prune: " + cacheSize);
                }
            }
        }
    }
}
