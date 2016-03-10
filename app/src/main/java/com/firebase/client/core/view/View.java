package com.firebase.client.core.view;

import com.firebase.client.FirebaseError;
import com.firebase.client.core.EventRegistration;
import com.firebase.client.core.Path;
import com.firebase.client.core.WriteTreeRef;
import com.firebase.client.core.operation.Operation;
import com.firebase.client.core.operation.Operation.OperationType;
import com.firebase.client.core.view.ViewProcessor.ProcessorResult;
import com.firebase.client.core.view.filter.IndexedFilter;
import com.firebase.client.core.view.filter.NodeFilter;
import com.firebase.client.snapshot.EmptyNode;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.client.snapshot.NamedNode;
import com.firebase.client.snapshot.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class View {
    static final /* synthetic */ boolean $assertionsDisabled = (!View.class.desiredAssertionStatus());
    private final EventGenerator eventGenerator;
    private final List<EventRegistration> eventRegistrations = new ArrayList();
    private final ViewProcessor processor;
    private final QuerySpec query;
    private ViewCache viewCache;

    public static class OperationResult {
        public final List<Change> changes;
        public final List<DataEvent> events;

        public OperationResult(List<DataEvent> events, List<Change> changes) {
            this.events = events;
            this.changes = changes;
        }
    }

    public View(QuerySpec query, ViewCache initialViewCache) {
        this.query = query;
        IndexedFilter indexFilter = new IndexedFilter(query.getIndex());
        NodeFilter filter = query.getParams().getNodeFilter();
        this.processor = new ViewProcessor(filter);
        CacheNode initialServerCache = initialViewCache.getServerCache();
        CacheNode initialEventCache = initialViewCache.getEventCache();
        IndexedNode emptyIndexedNode = IndexedNode.from(EmptyNode.Empty(), query.getIndex());
        IndexedNode serverSnap = indexFilter.updateFullNode(emptyIndexedNode, initialServerCache.getIndexedNode(), null);
        IndexedNode eventSnap = filter.updateFullNode(emptyIndexedNode, initialEventCache.getIndexedNode(), null);
        this.viewCache = new ViewCache(new CacheNode(eventSnap, initialEventCache.isFullyInitialized(), filter.filtersNodes()), new CacheNode(serverSnap, initialServerCache.isFullyInitialized(), indexFilter.filtersNodes()));
        this.eventGenerator = new EventGenerator(query);
    }

    public QuerySpec getQuery() {
        return this.query;
    }

    public Node getCompleteNode() {
        return this.viewCache.getCompleteEventSnap();
    }

    public Node getServerCache() {
        return this.viewCache.getServerCache().getNode();
    }

    public Node getEventCache() {
        return this.viewCache.getEventCache().getNode();
    }

    public Node getCompleteServerCache(Path path) {
        Node cache = this.viewCache.getCompleteServerSnap();
        if (cache == null || (!this.query.loadsAllData() && (path.isEmpty() || cache.getImmediateChild(path.getFront()).isEmpty()))) {
            return null;
        }
        return cache.getChild(path);
    }

    public boolean isEmpty() {
        return this.eventRegistrations.isEmpty();
    }

    public void addEventRegistration(EventRegistration registration) {
        this.eventRegistrations.add(registration);
    }

    public List<Event> removeEventRegistration(EventRegistration registration, FirebaseError cancelError) {
        List<Event> cancelEvents;
        if (cancelError != null) {
            cancelEvents = new ArrayList();
            if ($assertionsDisabled || registration == null) {
                Path path = this.query.getPath();
                for (EventRegistration eventRegistration : this.eventRegistrations) {
                    cancelEvents.add(new CancelEvent(eventRegistration, cancelError, path));
                }
            } else {
                throw new AssertionError("A cancel should cancel all event registrations");
            }
        }
        cancelEvents = Collections.emptyList();
        if (registration != null) {
            this.eventRegistrations.remove(registration);
        } else {
            this.eventRegistrations.clear();
        }
        return cancelEvents;
    }

    public OperationResult applyOperation(Operation operation, WriteTreeRef writesCache, Node optCompleteServerCache) {
        if (operation.getType() == OperationType.Merge && operation.getSource().getQueryParams() != null) {
            if (!$assertionsDisabled && this.viewCache.getCompleteServerSnap() == null) {
                throw new AssertionError("We should always have a full cache before handling merges");
            } else if (!$assertionsDisabled && this.viewCache.getCompleteEventSnap() == null) {
                throw new AssertionError("Missing event cache, even though we have a server cache");
            }
        }
        ViewCache oldViewCache = this.viewCache;
        ProcessorResult result = this.processor.applyOperation(oldViewCache, operation, writesCache, optCompleteServerCache);
        if ($assertionsDisabled || result.viewCache.getServerCache().isFullyInitialized() || !oldViewCache.getServerCache().isFullyInitialized()) {
            this.viewCache = result.viewCache;
            return new OperationResult(generateEventsForChanges(result.changes, result.viewCache.getEventCache().getIndexedNode(), null), result.changes);
        }
        throw new AssertionError("Once a server snap is complete, it should never go back");
    }

    public List<DataEvent> getInitialEvents(EventRegistration registration) {
        CacheNode eventSnap = this.viewCache.getEventCache();
        List<Change> initialChanges = new ArrayList();
        for (NamedNode child : eventSnap.getNode()) {
            initialChanges.add(Change.childAddedChange(child.getName(), child.getNode()));
        }
        if (eventSnap.isFullyInitialized()) {
            initialChanges.add(Change.valueChange(eventSnap.getIndexedNode()));
        }
        return generateEventsForChanges(initialChanges, eventSnap.getIndexedNode(), registration);
    }

    private List<DataEvent> generateEventsForChanges(List<Change> changes, IndexedNode eventCache, EventRegistration registration) {
        List<EventRegistration> registrations;
        if (registration == null) {
            registrations = this.eventRegistrations;
        } else {
            registrations = Arrays.asList(new EventRegistration[]{registration});
        }
        return this.eventGenerator.generateEventsForChanges(changes, eventCache, registrations);
    }
}
