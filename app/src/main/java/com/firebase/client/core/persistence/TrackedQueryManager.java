package com.firebase.client.core.persistence;

import com.android.volley.DefaultRetryPolicy;
import com.firebase.client.core.Path;
import com.firebase.client.core.utilities.ImmutableTree;
import com.firebase.client.core.utilities.ImmutableTree.TreeVisitor;
import com.firebase.client.core.utilities.Predicate;
import com.firebase.client.core.view.QueryParams;
import com.firebase.client.core.view.QuerySpec;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.utilities.Clock;
import com.firebase.client.utilities.LogWrapper;
import com.firebase.client.utilities.Utilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TrackedQueryManager {
    static final /* synthetic */ boolean $assertionsDisabled = (!TrackedQueryManager.class.desiredAssertionStatus());
    private static final Predicate<Map<QueryParams, TrackedQuery>> HAS_ACTIVE_DEFAULT_PREDICATE = new Predicate<Map<QueryParams, TrackedQuery>>() {
        public boolean evaluate(Map<QueryParams, TrackedQuery> trackedQueries) {
            TrackedQuery trackedQuery = (TrackedQuery) trackedQueries.get(QueryParams.DEFAULT_PARAMS);
            return trackedQuery != null && trackedQuery.active;
        }
    };
    private static final Predicate<Map<QueryParams, TrackedQuery>> HAS_DEFAULT_COMPLETE_PREDICATE = new Predicate<Map<QueryParams, TrackedQuery>>() {
        public boolean evaluate(Map<QueryParams, TrackedQuery> trackedQueries) {
            TrackedQuery trackedQuery = (TrackedQuery) trackedQueries.get(QueryParams.DEFAULT_PARAMS);
            return trackedQuery != null && trackedQuery.complete;
        }
    };
    private static final Predicate<TrackedQuery> IS_QUERY_PRUNABLE_PREDICATE = new Predicate<TrackedQuery>() {
        public boolean evaluate(TrackedQuery query) {
            return !query.active;
        }
    };
    private static final Predicate<TrackedQuery> IS_QUERY_UNPRUNABLE_PREDICATE = new Predicate<TrackedQuery>() {
        public boolean evaluate(TrackedQuery query) {
            return !TrackedQueryManager.IS_QUERY_PRUNABLE_PREDICATE.evaluate(query);
        }
    };
    private final Clock clock;
    private long currentQueryId = 0;
    private final LogWrapper logger;
    private final PersistenceStorageEngine storageLayer;
    private ImmutableTree<Map<QueryParams, TrackedQuery>> trackedQueryTree;

    private static void assertValidTrackedQuery(QuerySpec query) {
        boolean z = !query.loadsAllData() || query.isDefault();
        Utilities.hardAssert(z, "Can't have tracked non-default query that loads all data");
    }

    private static QuerySpec normalizeQuery(QuerySpec query) {
        return query.loadsAllData() ? QuerySpec.defaultQueryAtPath(query.getPath()) : query;
    }

    public TrackedQueryManager(PersistenceStorageEngine storageLayer, LogWrapper logger, Clock clock) {
        this.storageLayer = storageLayer;
        this.logger = logger;
        this.clock = clock;
        this.trackedQueryTree = new ImmutableTree(null);
        resetPreviouslyActiveTrackedQueries();
        for (TrackedQuery query : this.storageLayer.loadTrackedQueries()) {
            this.currentQueryId = Math.max(query.id + 1, this.currentQueryId);
            cacheTrackedQuery(query);
        }
    }

    private void resetPreviouslyActiveTrackedQueries() {
        try {
            this.storageLayer.beginTransaction();
            this.storageLayer.resetPreviouslyActiveTrackedQueries(this.clock.millis());
            this.storageLayer.setTransactionSuccessful();
        } finally {
            this.storageLayer.endTransaction();
        }
    }

    public TrackedQuery findTrackedQuery(QuerySpec query) {
        query = normalizeQuery(query);
        Map<QueryParams, TrackedQuery> set = (Map) this.trackedQueryTree.get(query.getPath());
        return set != null ? (TrackedQuery) set.get(query.getParams()) : null;
    }

    public void removeTrackedQuery(QuerySpec query) {
        query = normalizeQuery(query);
        TrackedQuery trackedQuery = findTrackedQuery(query);
        if ($assertionsDisabled || trackedQuery != null) {
            this.storageLayer.deleteTrackedQuery(trackedQuery.id);
            Map<QueryParams, TrackedQuery> trackedQueries = (Map) this.trackedQueryTree.get(query.getPath());
            trackedQueries.remove(query.getParams());
            if (trackedQueries.isEmpty()) {
                this.trackedQueryTree = this.trackedQueryTree.remove(query.getPath());
                return;
            }
            return;
        }
        throw new AssertionError("Query must exist to be removed.");
    }

    public void setQueryActive(QuerySpec query) {
        setQueryActiveFlag(query, true);
    }

    public void setQueryInactive(QuerySpec query) {
        setQueryActiveFlag(query, false);
    }

    private void setQueryActiveFlag(QuerySpec query, boolean isActive) {
        query = normalizeQuery(query);
        TrackedQuery trackedQuery = findTrackedQuery(query);
        long lastUse = this.clock.millis();
        if (trackedQuery != null) {
            trackedQuery = trackedQuery.updateLastUse(lastUse).setActiveState(isActive);
        } else if ($assertionsDisabled || isActive) {
            long j = this.currentQueryId;
            this.currentQueryId = 1 + j;
            trackedQuery = new TrackedQuery(j, query, lastUse, false, isActive);
        } else {
            throw new AssertionError("If we're setting the query to inactive, we should already be tracking it!");
        }
        saveTrackedQuery(trackedQuery);
    }

    public void setQueryCompleteIfExists(QuerySpec query) {
        TrackedQuery trackedQuery = findTrackedQuery(normalizeQuery(query));
        if (trackedQuery != null && !trackedQuery.complete) {
            saveTrackedQuery(trackedQuery.setComplete());
        }
    }

    public void setQueriesComplete(Path path) {
        this.trackedQueryTree.subtree(path).foreach(new TreeVisitor<Map<QueryParams, TrackedQuery>, Void>() {
            public Void onNodeValue(Path relativePath, Map<QueryParams, TrackedQuery> value, Void accum) {
                for (Entry<QueryParams, TrackedQuery> e : value.entrySet()) {
                    TrackedQuery trackedQuery = (TrackedQuery) e.getValue();
                    if (!trackedQuery.complete) {
                        TrackedQueryManager.this.saveTrackedQuery(trackedQuery.setComplete());
                    }
                }
                return null;
            }
        });
    }

    public boolean isQueryComplete(QuerySpec query) {
        if (includedInDefaultCompleteQuery(query.getPath())) {
            return true;
        }
        if (query.loadsAllData()) {
            return false;
        }
        Map<QueryParams, TrackedQuery> trackedQueries = (Map) this.trackedQueryTree.get(query.getPath());
        boolean z = trackedQueries != null && trackedQueries.containsKey(query.getParams()) && ((TrackedQuery) trackedQueries.get(query.getParams())).complete;
        return z;
    }

    public PruneForest pruneOldQueries(CachePolicy cachePolicy) {
        int i;
        List<TrackedQuery> prunable = getQueriesMatching(IS_QUERY_PRUNABLE_PREDICATE);
        long countToPrune = calculateCountToPrune(cachePolicy, (long) prunable.size());
        PruneForest forest = new PruneForest();
        if (this.logger.logsDebug()) {
            this.logger.debug("Pruning old queries.  Prunable: " + prunable.size() + " Count to prune: " + countToPrune);
        }
        Collections.sort(prunable, new Comparator<TrackedQuery>() {
            public int compare(TrackedQuery q1, TrackedQuery q2) {
                return Utilities.compareLongs(q1.lastUse, q2.lastUse);
            }
        });
        for (i = 0; ((long) i) < countToPrune; i++) {
            TrackedQuery toPrune = (TrackedQuery) prunable.get(i);
            forest = forest.prune(toPrune.querySpec.getPath());
            removeTrackedQuery(toPrune.querySpec);
        }
        for (i = (int) countToPrune; i < prunable.size(); i++) {
            forest = forest.keep(((TrackedQuery) prunable.get(i)).querySpec.getPath());
        }
        List<TrackedQuery> unprunable = getQueriesMatching(IS_QUERY_UNPRUNABLE_PREDICATE);
        if (this.logger.logsDebug()) {
            this.logger.debug("Unprunable queries: " + unprunable.size());
        }
        for (TrackedQuery toKeep : unprunable) {
            forest = forest.keep(toKeep.querySpec.getPath());
        }
        return forest;
    }

    private static long calculateCountToPrune(CachePolicy cachePolicy, long prunableCount) {
        return prunableCount - Math.min((long) Math.floor((double) (((float) prunableCount) * (DefaultRetryPolicy.DEFAULT_BACKOFF_MULT - cachePolicy.getPercentOfQueriesToPruneAtOnce()))), cachePolicy.getMaxNumberOfQueriesToKeep());
    }

    public Set<ChildKey> getKnownCompleteChildren(Path path) {
        if ($assertionsDisabled || !isQueryComplete(QuerySpec.defaultQueryAtPath(path))) {
            Set<ChildKey> completeChildren = new HashSet();
            Set queryIds = filteredQueryIdsAtPath(path);
            if (!queryIds.isEmpty()) {
                completeChildren.addAll(this.storageLayer.loadTrackedQueryKeys(queryIds));
            }
            Iterator i$ = this.trackedQueryTree.subtree(path).getChildren().iterator();
            while (i$.hasNext()) {
                Entry<ChildKey, ImmutableTree<Map<QueryParams, TrackedQuery>>> childEntry = (Entry) i$.next();
                ChildKey childKey = (ChildKey) childEntry.getKey();
                ImmutableTree<Map<QueryParams, TrackedQuery>> childTree = (ImmutableTree) childEntry.getValue();
                if (childTree.getValue() != null && HAS_DEFAULT_COMPLETE_PREDICATE.evaluate(childTree.getValue())) {
                    completeChildren.add(childKey);
                }
            }
            return completeChildren;
        }
        throw new AssertionError("Path is fully complete.");
    }

    public void ensureCompleteTrackedQuery(Path path) {
        if (!includedInDefaultCompleteQuery(path)) {
            QuerySpec querySpec = QuerySpec.defaultQueryAtPath(path);
            TrackedQuery trackedQuery = findTrackedQuery(querySpec);
            if (trackedQuery == null) {
                long j = this.currentQueryId;
                this.currentQueryId = 1 + j;
                trackedQuery = new TrackedQuery(j, querySpec, this.clock.millis(), true, false);
            } else if ($assertionsDisabled || !trackedQuery.complete) {
                trackedQuery = trackedQuery.setComplete();
            } else {
                throw new AssertionError("This should have been handled above!");
            }
            saveTrackedQuery(trackedQuery);
        }
    }

    public boolean hasActiveDefaultQuery(Path path) {
        return this.trackedQueryTree.rootMostValueMatching(path, HAS_ACTIVE_DEFAULT_PREDICATE) != null;
    }

    public long countOfPrunableQueries() {
        return (long) getQueriesMatching(IS_QUERY_PRUNABLE_PREDICATE).size();
    }

    void verifyCache() {
        List<TrackedQuery> storedTrackedQueries = this.storageLayer.loadTrackedQueries();
        final List<TrackedQuery> trackedQueries = new ArrayList();
        this.trackedQueryTree.foreach(new TreeVisitor<Map<QueryParams, TrackedQuery>, Void>() {
            public Void onNodeValue(Path relativePath, Map<QueryParams, TrackedQuery> value, Void accum) {
                for (TrackedQuery trackedQuery : value.values()) {
                    trackedQueries.add(trackedQuery);
                }
                return null;
            }
        });
        Collections.sort(trackedQueries, new Comparator<TrackedQuery>() {
            public int compare(TrackedQuery o1, TrackedQuery o2) {
                return Utilities.compareLongs(o1.id, o2.id);
            }
        });
        Utilities.hardAssert(storedTrackedQueries.equals(trackedQueries), "Tracked queries out of sync.  Tracked queries: " + trackedQueries + " Stored queries: " + storedTrackedQueries);
    }

    private boolean includedInDefaultCompleteQuery(Path path) {
        return this.trackedQueryTree.findRootMostMatchingPath(path, HAS_DEFAULT_COMPLETE_PREDICATE) != null;
    }

    private Set<Long> filteredQueryIdsAtPath(Path path) {
        Set<Long> ids = new HashSet();
        Map<QueryParams, TrackedQuery> queries = (Map) this.trackedQueryTree.get(path);
        if (queries != null) {
            for (TrackedQuery query : queries.values()) {
                if (!query.querySpec.loadsAllData()) {
                    ids.add(Long.valueOf(query.id));
                }
            }
        }
        return ids;
    }

    private void cacheTrackedQuery(TrackedQuery query) {
        assertValidTrackedQuery(query.querySpec);
        Map<QueryParams, TrackedQuery> trackedSet = (Map) this.trackedQueryTree.get(query.querySpec.getPath());
        if (trackedSet == null) {
            trackedSet = new HashMap();
            this.trackedQueryTree = this.trackedQueryTree.set(query.querySpec.getPath(), trackedSet);
        }
        TrackedQuery existing = (TrackedQuery) trackedSet.get(query.querySpec.getParams());
        boolean z = existing == null || existing.id == query.id;
        Utilities.hardAssert(z);
        trackedSet.put(query.querySpec.getParams(), query);
    }

    private void saveTrackedQuery(TrackedQuery query) {
        cacheTrackedQuery(query);
        this.storageLayer.saveTrackedQuery(query);
    }

    private List<TrackedQuery> getQueriesMatching(Predicate<TrackedQuery> predicate) {
        List<TrackedQuery> matching = new ArrayList();
        Iterator it = this.trackedQueryTree.iterator();
        while (it.hasNext()) {
            for (TrackedQuery query : ((Map) ((Entry) it.next()).getValue()).values()) {
                if (predicate.evaluate(query)) {
                    matching.add(query);
                }
            }
        }
        return matching;
    }
}
