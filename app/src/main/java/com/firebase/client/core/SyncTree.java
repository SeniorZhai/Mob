package com.firebase.client.core;

import com.firebase.client.FirebaseError;
import com.firebase.client.collection.LLRBNode.NodeVisitor;
import com.firebase.client.core.operation.AckUserWrite;
import com.firebase.client.core.operation.ListenComplete;
import com.firebase.client.core.operation.Merge;
import com.firebase.client.core.operation.Operation;
import com.firebase.client.core.operation.OperationSource;
import com.firebase.client.core.operation.Overwrite;
import com.firebase.client.core.persistence.PersistenceManager;
import com.firebase.client.core.utilities.ImmutableTree;
import com.firebase.client.core.utilities.ImmutableTree.TreeVisitor;
import com.firebase.client.core.view.CacheNode;
import com.firebase.client.core.view.Change;
import com.firebase.client.core.view.DataEvent;
import com.firebase.client.core.view.Event;
import com.firebase.client.core.view.Event.EventType;
import com.firebase.client.core.view.QuerySpec;
import com.firebase.client.core.view.View;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.EmptyNode;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.client.snapshot.NamedNode;
import com.firebase.client.snapshot.Node;
import com.firebase.client.utilities.Clock;
import com.firebase.client.utilities.LogWrapper;
import com.firebase.client.utilities.Pair;
import com.firebase.client.utilities.Utilities;
import io.fabric.sdk.android.BuildConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

public class SyncTree {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final EventRegistration keepSyncedEventRegistration = new EventRegistration() {
        public boolean respondsTo(EventType eventType) {
            return false;
        }

        public DataEvent createEvent(Change change, QuerySpec query) {
            return null;
        }

        public void fireEvent(DataEvent dataEvent) {
        }

        public void fireCancelEvent(FirebaseError error) {
        }
    };
    private final Set<QuerySpec> keepSyncedQueries = new HashSet();
    private final ListenProvider listenProvider;
    private final LogWrapper logger;
    private long nextQueryTag = 1;
    private final WriteTree pendingWriteTree = new WriteTree();
    private final PersistenceManager persistenceManager;
    private final Map<QuerySpec, Tag> queryToTagMap = new HashMap();
    private ImmutableTree<SyncPoint> syncPointTree = ImmutableTree.emptyInstance();
    private final Map<Tag, QuerySpec> tagToQueryMap = new HashMap();

    public interface ListenProvider {
        void startListening(QuerySpec querySpec, Tag tag, SyncTreeHash syncTreeHash, CompletionListener completionListener);

        void stopListening(QuerySpec querySpec, Tag tag);
    }

    public interface CompletionListener {
        List<? extends Event> onListenComplete(FirebaseError firebaseError);
    }

    public interface SyncTreeHash {
        String getHash();
    }

    private class ListenContainer implements SyncTreeHash, CompletionListener {
        private final Tag tag;
        private final View view;

        public ListenContainer(View view) {
            this.view = view;
            this.tag = SyncTree.this.tagForQuery(view.getQuery());
        }

        public String getHash() {
            Node cache = this.view.getServerCache();
            if (cache == null) {
                cache = EmptyNode.Empty();
            }
            return cache.getHash();
        }

        public List<? extends Event> onListenComplete(FirebaseError error) {
            if (error == null) {
                QuerySpec query = this.view.getQuery();
                if (this.tag != null) {
                    return SyncTree.this.applyTaggedListenComplete(this.tag);
                }
                return SyncTree.this.applyListenComplete(query.getPath());
            }
            SyncTree.this.logger.warn("Listen at " + this.view.getQuery().getPath() + " failed: " + error.toString());
            return SyncTree.this.removeEventRegistration(this.view.getQuery(), null, error);
        }
    }

    static {
        boolean z;
        if (SyncTree.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        $assertionsDisabled = z;
    }

    public SyncTree(Context context, PersistenceManager persistenceManager, ListenProvider listenProvider) {
        this.listenProvider = listenProvider;
        this.persistenceManager = persistenceManager;
        this.logger = context.getLogger("SyncTree");
    }

    public boolean isEmpty() {
        return this.syncPointTree.isEmpty();
    }

    public List<? extends Event> applyUserOverwrite(Path path, Node newDataUnresolved, Node newData, long writeId, boolean visible, boolean persist) {
        boolean z = visible || !persist;
        Utilities.hardAssert(z, "We shouldn't be persisting non-visible writes.");
        final boolean z2 = persist;
        final Path path2 = path;
        final Node node = newDataUnresolved;
        final long j = writeId;
        final Node node2 = newData;
        final boolean z3 = visible;
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() {
                if (z2) {
                    SyncTree.this.persistenceManager.saveUserOverwrite(path2, node, j);
                }
                SyncTree.this.pendingWriteTree.addOverwrite(path2, node2, Long.valueOf(j), z3);
                if (z3) {
                    return SyncTree.this.applyOperationToSyncPoints(new Overwrite(OperationSource.USER, path2, node2));
                }
                return Collections.emptyList();
            }
        });
    }

    public List<? extends Event> applyUserMerge(Path path, CompoundWrite unresolvedChildren, CompoundWrite children, long writeId, boolean persist) {
        final boolean z = persist;
        final Path path2 = path;
        final CompoundWrite compoundWrite = unresolvedChildren;
        final long j = writeId;
        final CompoundWrite compoundWrite2 = children;
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() throws Exception {
                if (z) {
                    SyncTree.this.persistenceManager.saveUserMerge(path2, compoundWrite, j);
                }
                SyncTree.this.pendingWriteTree.addMerge(path2, compoundWrite2, Long.valueOf(j));
                return SyncTree.this.applyOperationToSyncPoints(new Merge(OperationSource.USER, path2, compoundWrite2));
            }
        });
    }

    public List<? extends Event> ackUserWrite(long writeId, boolean revert, boolean persist, Clock serverClock) {
        final boolean z = persist;
        final long j = writeId;
        final boolean z2 = revert;
        final Clock clock = serverClock;
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() {
                if (z) {
                    SyncTree.this.persistenceManager.removeUserWrite(j);
                }
                UserWriteRecord write = SyncTree.this.pendingWriteTree.getWrite(j);
                Path pathToReevaluate = SyncTree.this.pendingWriteTree.removeWrite(j);
                if (write.isVisible() && !z2) {
                    Map<String, Object> serverValues = ServerValues.generateServerValues(clock);
                    if (write.isOverwrite()) {
                        SyncTree.this.persistenceManager.applyUserWriteToServerCache(write.getPath(), ServerValues.resolveDeferredValueSnapshot(write.getOverwrite(), serverValues));
                    } else {
                        SyncTree.this.persistenceManager.applyUserWriteToServerCache(write.getPath(), ServerValues.resolveDeferredValueMerge(write.getMerge(), serverValues));
                    }
                }
                if (pathToReevaluate == null) {
                    return Collections.emptyList();
                }
                return SyncTree.this.applyOperationToSyncPoints(new AckUserWrite(pathToReevaluate, z2));
            }
        });
    }

    public List<? extends Event> removeAllWrites() {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() throws Exception {
                SyncTree.this.persistenceManager.removeAllUserWrites();
                if (SyncTree.this.pendingWriteTree.purgeAllWrites().isEmpty()) {
                    return Collections.emptyList();
                }
                return SyncTree.this.applyOperationToSyncPoints(new AckUserWrite(Path.getEmptyPath(), true));
            }
        });
    }

    public List<? extends Event> applyServerOverwrite(final Path path, final Node newData) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() {
                SyncTree.this.persistenceManager.updateServerCache(QuerySpec.defaultQueryAtPath(path), newData);
                return SyncTree.this.applyOperationToSyncPoints(new Overwrite(OperationSource.SERVER, path, newData));
            }
        });
    }

    public List<? extends Event> applyServerMerge(final Path path, final Map<Path, Node> changedChildren) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() {
                CompoundWrite merge = CompoundWrite.fromPathMerge(changedChildren);
                SyncTree.this.persistenceManager.updateServerCache(path, merge);
                return SyncTree.this.applyOperationToSyncPoints(new Merge(OperationSource.SERVER, path, merge));
            }
        });
    }

    public List<? extends Event> applyListenComplete(final Path path) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() {
                SyncTree.this.persistenceManager.setQueryComplete(QuerySpec.defaultQueryAtPath(path));
                return SyncTree.this.applyOperationToSyncPoints(new ListenComplete(OperationSource.SERVER, path));
            }
        });
    }

    public List<? extends Event> applyTaggedListenComplete(final Tag tag) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() {
                QuerySpec query = SyncTree.this.queryForTag(tag);
                if (query == null) {
                    return Collections.emptyList();
                }
                SyncTree.this.persistenceManager.setQueryComplete(query);
                return SyncTree.this.applyTaggedOperation(query, new ListenComplete(OperationSource.forServerTaggedQuery(query.getParams()), Path.getEmptyPath()));
            }
        });
    }

    private List<? extends Event> applyTaggedOperation(QuerySpec query, Operation operation) {
        Path queryPath = query.getPath();
        SyncPoint syncPoint = (SyncPoint) this.syncPointTree.get(queryPath);
        if ($assertionsDisabled || syncPoint != null) {
            return syncPoint.applyOperation(operation, this.pendingWriteTree.childWrites(queryPath), null);
        }
        throw new AssertionError("Missing sync point for query tag that we're tracking");
    }

    public List<? extends Event> applyTaggedQueryOverwrite(final Path path, final Node snap, final Tag tag) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() {
                QuerySpec query = SyncTree.this.queryForTag(tag);
                if (query == null) {
                    return Collections.emptyList();
                }
                Path relativePath = Path.getRelative(query.getPath(), path);
                SyncTree.this.persistenceManager.updateServerCache(relativePath.isEmpty() ? query : QuerySpec.defaultQueryAtPath(path), snap);
                return SyncTree.this.applyTaggedOperation(query, new Overwrite(OperationSource.forServerTaggedQuery(query.getParams()), relativePath, snap));
            }
        });
    }

    public List<? extends Event> applyTaggedQueryMerge(final Path path, final Map<Path, Node> changedChildren, final Tag tag) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            public List<? extends Event> call() {
                QuerySpec query = SyncTree.this.queryForTag(tag);
                if (query == null) {
                    return Collections.emptyList();
                }
                Path relativePath = Path.getRelative(query.getPath(), path);
                CompoundWrite merge = CompoundWrite.fromPathMerge(changedChildren);
                SyncTree.this.persistenceManager.updateServerCache(path, merge);
                return SyncTree.this.applyTaggedOperation(query, new Merge(OperationSource.forServerTaggedQuery(query.getParams()), relativePath, merge));
            }
        });
    }

    public List<? extends Event> addEventRegistration(final QuerySpec query, final EventRegistration eventRegistration) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() {
            static final /* synthetic */ boolean $assertionsDisabled = (!SyncTree.class.desiredAssertionStatus());

            public List<? extends Event> call() {
                CacheNode serverCache;
                Path path = query.getPath();
                Node serverCacheNode = null;
                boolean foundAncestorDefaultView = false;
                ImmutableTree<SyncPoint> tree = SyncTree.this.syncPointTree;
                Path currentPath = path;
                while (!tree.isEmpty() && serverCacheNode == null) {
                    SyncPoint currentSyncPoint = (SyncPoint) tree.getValue();
                    if (currentSyncPoint != null) {
                        serverCacheNode = currentSyncPoint.getCompleteServerCache(currentPath);
                        foundAncestorDefaultView = foundAncestorDefaultView || currentSyncPoint.hasCompleteView();
                    }
                    tree = tree.getChild(currentPath.isEmpty() ? ChildKey.fromString(BuildConfig.FLAVOR) : currentPath.getFront());
                    currentPath = currentPath.popFront();
                }
                SyncPoint syncPoint = (SyncPoint) SyncTree.this.syncPointTree.get(path);
                if (syncPoint == null) {
                    SyncPoint syncPoint2 = new SyncPoint(SyncTree.this.persistenceManager);
                    SyncTree.this.syncPointTree = SyncTree.this.syncPointTree.set(path, syncPoint2);
                } else {
                    foundAncestorDefaultView = foundAncestorDefaultView || syncPoint.hasCompleteView();
                    if (serverCacheNode == null) {
                        serverCacheNode = syncPoint.getCompleteServerCache(Path.getEmptyPath());
                    }
                }
                SyncTree.this.persistenceManager.setQueryActive(query);
                CacheNode cacheNode;
                if (serverCacheNode != null) {
                    cacheNode = new CacheNode(IndexedNode.from(serverCacheNode, query.getIndex()), true, false);
                } else {
                    CacheNode persistentServerCache = SyncTree.this.persistenceManager.serverCache(query);
                    if (persistentServerCache.isFullyInitialized()) {
                        serverCache = persistentServerCache;
                    } else {
                        serverCacheNode = EmptyNode.Empty();
                        Iterator i$ = SyncTree.this.syncPointTree.subtree(path).getChildren().iterator();
                        while (i$.hasNext()) {
                            Entry<ChildKey, ImmutableTree<SyncPoint>> child = (Entry) i$.next();
                            SyncPoint childSyncPoint = (SyncPoint) ((ImmutableTree) child.getValue()).getValue();
                            if (childSyncPoint != null) {
                                Node completeCache = childSyncPoint.getCompleteServerCache(Path.getEmptyPath());
                                if (completeCache != null) {
                                    serverCacheNode = serverCacheNode.updateImmediateChild((ChildKey) child.getKey(), completeCache);
                                }
                            }
                        }
                        for (NamedNode child2 : persistentServerCache.getNode()) {
                            if (!serverCacheNode.hasChild(child2.getName())) {
                                serverCacheNode = serverCacheNode.updateImmediateChild(child2.getName(), child2.getNode());
                            }
                        }
                        cacheNode = new CacheNode(IndexedNode.from(serverCacheNode, query.getIndex()), false, false);
                    }
                }
                boolean viewAlreadyExists = syncPoint.viewExistsForQuery(query);
                if (!(viewAlreadyExists || query.loadsAllData())) {
                    if ($assertionsDisabled || !SyncTree.this.queryToTagMap.containsKey(query)) {
                        Tag tag = SyncTree.this.getNextQueryTag();
                        SyncTree.this.queryToTagMap.put(query, tag);
                        SyncTree.this.tagToQueryMap.put(tag, query);
                    } else {
                        throw new AssertionError("View does not exist but we have a tag");
                    }
                }
                List<? extends Event> events = syncPoint.addEventRegistration(query, eventRegistration, SyncTree.this.pendingWriteTree.childWrites(path), serverCache);
                if (!(viewAlreadyExists || foundAncestorDefaultView)) {
                    SyncTree.this.setupListener(query, syncPoint.viewForQuery(query));
                }
                return events;
            }
        });
    }

    public List<Event> removeEventRegistration(QuerySpec query, EventRegistration eventRegistration) {
        return removeEventRegistration(query, eventRegistration, null);
    }

    public List<Event> removeEventRegistration(final QuerySpec query, final EventRegistration eventRegistration, final FirebaseError cancelError) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<Event>>() {
            static final /* synthetic */ boolean $assertionsDisabled = (!SyncTree.class.desiredAssertionStatus());

            public List<Event> call() {
                Path path = query.getPath();
                SyncPoint maybeSyncPoint = (SyncPoint) SyncTree.this.syncPointTree.get(path);
                List<Event> cancelEvents = new ArrayList();
                if (maybeSyncPoint != null && (query.isDefault() || maybeSyncPoint.viewExistsForQuery(query))) {
                    Pair<List<QuerySpec>, List<Event>> removedAndEvents = maybeSyncPoint.removeEventRegistration(query, eventRegistration, cancelError);
                    if (maybeSyncPoint.isEmpty()) {
                        SyncTree.this.syncPointTree = SyncTree.this.syncPointTree.remove(path);
                    }
                    List<QuerySpec> removed = (List) removedAndEvents.getFirst();
                    cancelEvents = (List) removedAndEvents.getSecond();
                    boolean removingDefault = false;
                    for (QuerySpec queryRemoved : removed) {
                        SyncTree.this.persistenceManager.setQueryInactive(query);
                        removingDefault = removingDefault || queryRemoved.loadsAllData();
                    }
                    ImmutableTree<SyncPoint> currentTree = SyncTree.this.syncPointTree;
                    boolean covered = currentTree.getValue() != null && ((SyncPoint) currentTree.getValue()).hasCompleteView();
                    Iterator i$ = path.iterator();
                    while (i$.hasNext()) {
                        currentTree = currentTree.getChild((ChildKey) i$.next());
                        covered = covered || (currentTree.getValue() != null && ((SyncPoint) currentTree.getValue()).hasCompleteView());
                        if (!covered) {
                            if (currentTree.isEmpty()) {
                                break;
                            }
                        }
                        break;
                    }
                    if (removingDefault && !covered) {
                        ImmutableTree<SyncPoint> subtree = SyncTree.this.syncPointTree.subtree(path);
                        if (!subtree.isEmpty()) {
                            for (View view : SyncTree.this.collectDistinctViewsForSubTree(subtree)) {
                                ListenContainer container = new ListenContainer(view);
                                SyncTree.this.listenProvider.startListening(view.getQuery(), container.tag, container, container);
                            }
                        }
                    }
                    if (!(covered || removed.isEmpty() || cancelError != null)) {
                        if (removingDefault) {
                            SyncTree.this.listenProvider.stopListening(query, null);
                        } else {
                            for (QuerySpec queryToRemove : removed) {
                                Tag tag = SyncTree.this.tagForQuery(queryToRemove);
                                if ($assertionsDisabled || tag != null) {
                                    SyncTree.this.listenProvider.stopListening(queryToRemove, tag);
                                } else {
                                    throw new AssertionError();
                                }
                            }
                        }
                    }
                    SyncTree.this.removeTags(removed);
                }
                return cancelEvents;
            }
        });
    }

    public void keepSynced(QuerySpec query, boolean keep) {
        if (keep && !this.keepSyncedQueries.contains(query)) {
            addEventRegistration(query, keepSyncedEventRegistration);
            this.keepSyncedQueries.add(query);
        } else if (!keep && this.keepSyncedQueries.contains(query)) {
            removeEventRegistration(query, keepSyncedEventRegistration);
            this.keepSyncedQueries.remove(query);
        }
    }

    private List<View> collectDistinctViewsForSubTree(ImmutableTree<SyncPoint> subtree) {
        ArrayList<View> accumulator = new ArrayList();
        collectDistinctViewsForSubTree(subtree, accumulator);
        return accumulator;
    }

    private void collectDistinctViewsForSubTree(ImmutableTree<SyncPoint> subtree, List<View> accumulator) {
        SyncPoint maybeSyncPoint = (SyncPoint) subtree.getValue();
        if (maybeSyncPoint == null || !maybeSyncPoint.hasCompleteView()) {
            if (maybeSyncPoint != null) {
                accumulator.addAll(maybeSyncPoint.getQueryViews());
            }
            Iterator i$ = subtree.getChildren().iterator();
            while (i$.hasNext()) {
                collectDistinctViewsForSubTree((ImmutableTree) ((Entry) i$.next()).getValue(), accumulator);
            }
            return;
        }
        accumulator.add(maybeSyncPoint.getCompleteView());
    }

    private void removeTags(List<QuerySpec> queries) {
        for (QuerySpec removedQuery : queries) {
            if (!removedQuery.loadsAllData()) {
                Tag tag = tagForQuery(removedQuery);
                if ($assertionsDisabled || tag != null) {
                    this.queryToTagMap.remove(removedQuery);
                    this.tagToQueryMap.remove(tag);
                } else {
                    throw new AssertionError();
                }
            }
        }
    }

    private void setupListener(QuerySpec query, View view) {
        Path path = query.getPath();
        Tag tag = tagForQuery(query);
        ListenContainer container = new ListenContainer(view);
        this.listenProvider.startListening(query, tag, container, container);
        ImmutableTree<SyncPoint> subtree = this.syncPointTree.subtree(path);
        if (tag == null) {
            subtree.foreach(new TreeVisitor<SyncPoint, Void>() {
                public Void onNodeValue(Path relativePath, SyncPoint maybeChildSyncPoint, Void accum) {
                    if (relativePath.isEmpty() || !maybeChildSyncPoint.hasCompleteView()) {
                        for (View syncPointView : maybeChildSyncPoint.getQueryViews()) {
                            QuerySpec childQuery = syncPointView.getQuery();
                            SyncTree.this.listenProvider.stopListening(childQuery, SyncTree.this.tagForQuery(childQuery));
                        }
                    } else {
                        QuerySpec query = maybeChildSyncPoint.getCompleteView().getQuery();
                        SyncTree.this.listenProvider.stopListening(query, SyncTree.this.tagForQuery(query));
                    }
                    return null;
                }
            });
        } else if (!$assertionsDisabled && ((SyncPoint) subtree.getValue()).hasCompleteView()) {
            throw new AssertionError("If we're adding a query, it shouldn't be shadowed");
        }
    }

    private QuerySpec queryForTag(Tag tag) {
        return (QuerySpec) this.tagToQueryMap.get(tag);
    }

    private Tag tagForQuery(QuerySpec query) {
        return (Tag) this.queryToTagMap.get(query);
    }

    public Node calcCompleteEventCache(Path path, List<Long> writeIdsToExclude) {
        ImmutableTree<SyncPoint> tree = this.syncPointTree;
        SyncPoint currentSyncPoint = (SyncPoint) tree.getValue();
        Node serverCache = null;
        Path pathToFollow = path;
        Path pathSoFar = Path.getEmptyPath();
        do {
            ChildKey front = pathToFollow.getFront();
            pathToFollow = pathToFollow.popFront();
            pathSoFar = pathSoFar.child(front);
            Path relativePath = Path.getRelative(pathSoFar, path);
            tree = front != null ? tree.getChild(front) : ImmutableTree.emptyInstance();
            currentSyncPoint = (SyncPoint) tree.getValue();
            if (currentSyncPoint != null) {
                serverCache = currentSyncPoint.getCompleteServerCache(relativePath);
            }
            if (pathToFollow.isEmpty()) {
                break;
            }
        } while (serverCache == null);
        return this.pendingWriteTree.calcCompleteEventCache(path, serverCache, writeIdsToExclude, true);
    }

    private Tag getNextQueryTag() {
        long j = this.nextQueryTag;
        this.nextQueryTag = 1 + j;
        return new Tag(j);
    }

    private List<Event> applyOperationToSyncPoints(Operation operation) {
        return applyOperationHelper(operation, this.syncPointTree, null, this.pendingWriteTree.childWrites(Path.getEmptyPath()));
    }

    private List<Event> applyOperationHelper(Operation operation, ImmutableTree<SyncPoint> syncPointTree, Node serverCache, WriteTreeRef writesCache) {
        if (operation.getPath().isEmpty()) {
            return applyOperationDescendantsHelper(operation, syncPointTree, serverCache, writesCache);
        }
        SyncPoint syncPoint = (SyncPoint) syncPointTree.getValue();
        if (serverCache == null && syncPoint != null) {
            serverCache = syncPoint.getCompleteServerCache(Path.getEmptyPath());
        }
        List<Event> events = new ArrayList();
        ChildKey childKey = operation.getPath().getFront();
        Operation childOperation = operation.operationForChild(childKey);
        ImmutableTree<SyncPoint> childTree = (ImmutableTree) syncPointTree.getChildren().get(childKey);
        if (!(childTree == null || childOperation == null)) {
            events.addAll(applyOperationHelper(childOperation, childTree, serverCache != null ? serverCache.getImmediateChild(childKey) : null, writesCache.child(childKey)));
        }
        if (syncPoint == null) {
            return events;
        }
        events.addAll(syncPoint.applyOperation(operation, writesCache, serverCache));
        return events;
    }

    private List<Event> applyOperationDescendantsHelper(Operation operation, ImmutableTree<SyncPoint> syncPointTree, Node serverCache, WriteTreeRef writesCache) {
        Node resolvedServerCache;
        SyncPoint syncPoint = (SyncPoint) syncPointTree.getValue();
        if (serverCache != null || syncPoint == null) {
            resolvedServerCache = serverCache;
        } else {
            resolvedServerCache = syncPoint.getCompleteServerCache(Path.getEmptyPath());
        }
        final List<Event> events = new ArrayList();
        final WriteTreeRef writeTreeRef = writesCache;
        final Operation operation2 = operation;
        syncPointTree.getChildren().inOrderTraversal(new NodeVisitor<ChildKey, ImmutableTree<SyncPoint>>() {
            public void visitEntry(ChildKey key, ImmutableTree<SyncPoint> childTree) {
                Node childServerCache = null;
                if (resolvedServerCache != null) {
                    childServerCache = resolvedServerCache.getImmediateChild(key);
                }
                WriteTreeRef childWritesCache = writeTreeRef.child(key);
                Operation childOperation = operation2.operationForChild(key);
                if (childOperation != null) {
                    events.addAll(SyncTree.this.applyOperationDescendantsHelper(childOperation, childTree, childServerCache, childWritesCache));
                }
            }
        });
        if (syncPoint != null) {
            events.addAll(syncPoint.applyOperation(operation, writesCache, resolvedServerCache));
        }
        return events;
    }
}
