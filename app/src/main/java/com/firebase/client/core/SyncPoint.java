package com.firebase.client.core;

import com.firebase.client.FirebaseError;
import com.firebase.client.core.operation.Operation;
import com.firebase.client.core.persistence.PersistenceManager;
import com.firebase.client.core.view.CacheNode;
import com.firebase.client.core.view.Change;
import com.firebase.client.core.view.DataEvent;
import com.firebase.client.core.view.Event;
import com.firebase.client.core.view.Event.EventType;
import com.firebase.client.core.view.QueryParams;
import com.firebase.client.core.view.QuerySpec;
import com.firebase.client.core.view.View;
import com.firebase.client.core.view.View.OperationResult;
import com.firebase.client.core.view.ViewCache;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.client.snapshot.NamedNode;
import com.firebase.client.snapshot.Node;
import com.firebase.client.utilities.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SyncPoint {
    static final /* synthetic */ boolean $assertionsDisabled = (!SyncPoint.class.desiredAssertionStatus());
    private final PersistenceManager persistenceManager;
    private final Map<QueryParams, View> views = new HashMap();

    public SyncPoint(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    public boolean isEmpty() {
        return this.views.isEmpty();
    }

    private List<DataEvent> applyOperationToView(View view, Operation operation, WriteTreeRef writes, Node optCompleteServerCache) {
        OperationResult result = view.applyOperation(operation, writes, optCompleteServerCache);
        if (!view.getQuery().loadsAllData()) {
            Set<ChildKey> removed = new HashSet();
            Set<ChildKey> added = new HashSet();
            for (Change change : result.changes) {
                EventType type = change.getEventType();
                if (type == EventType.CHILD_ADDED) {
                    added.add(change.getChildKey());
                } else if (type == EventType.CHILD_REMOVED) {
                    removed.add(change.getChildKey());
                }
            }
            if (!(added.isEmpty() && removed.isEmpty())) {
                this.persistenceManager.updateTrackedQueryKeys(view.getQuery(), added, removed);
            }
        }
        return result.events;
    }

    public List<DataEvent> applyOperation(Operation operation, WriteTreeRef writesCache, Node optCompleteServerCache) {
        QueryParams queryParams = operation.getSource().getQueryParams();
        if (queryParams != null) {
            View view = (View) this.views.get(queryParams);
            if ($assertionsDisabled || view != null) {
                return applyOperationToView(view, operation, writesCache, optCompleteServerCache);
            }
            throw new AssertionError();
        }
        List<DataEvent> events = new ArrayList();
        for (Entry<QueryParams, View> entry : this.views.entrySet()) {
            events.addAll(applyOperationToView((View) entry.getValue(), operation, writesCache, optCompleteServerCache));
        }
        return events;
    }

    public List<DataEvent> addEventRegistration(QuerySpec query, EventRegistration eventRegistration, WriteTreeRef writesCache, CacheNode serverCache) {
        View view = (View) this.views.get(query.getParams());
        if (view == null) {
            boolean eventCacheComplete;
            Node eventCache = writesCache.calcCompleteEventCache(serverCache.isFullyInitialized() ? serverCache.getNode() : null);
            if (eventCache != null) {
                eventCacheComplete = true;
            } else {
                eventCache = writesCache.calcCompleteEventChildren(serverCache.getNode());
                eventCacheComplete = false;
            }
            view = new View(query, new ViewCache(new CacheNode(IndexedNode.from(eventCache, query.getIndex()), eventCacheComplete, false), serverCache));
            if (!query.loadsAllData()) {
                Set<ChildKey> allChildren = new HashSet();
                for (NamedNode node : view.getEventCache()) {
                    allChildren.add(node.getName());
                }
                this.persistenceManager.setTrackedQueryKeys(query, allChildren);
            }
            this.views.put(query.getParams(), view);
        }
        view.addEventRegistration(eventRegistration);
        return view.getInitialEvents(eventRegistration);
    }

    public Pair<List<QuerySpec>, List<Event>> removeEventRegistration(QuerySpec query, EventRegistration eventRegistration, FirebaseError cancelError) {
        List<QuerySpec> removed = new ArrayList();
        List<Event> cancelEvents = new ArrayList();
        boolean hadCompleteView = hasCompleteView();
        View view;
        if (query.isDefault()) {
            Iterator<Entry<QueryParams, View>> iterator = this.views.entrySet().iterator();
            while (iterator.hasNext()) {
                view = (View) ((Entry) iterator.next()).getValue();
                cancelEvents.addAll(view.removeEventRegistration(eventRegistration, cancelError));
                if (view.isEmpty()) {
                    iterator.remove();
                    if (!view.getQuery().loadsAllData()) {
                        removed.add(view.getQuery());
                    }
                }
            }
        } else {
            view = (View) this.views.get(query.getParams());
            if (view != null) {
                cancelEvents.addAll(view.removeEventRegistration(eventRegistration, cancelError));
                if (view.isEmpty()) {
                    this.views.remove(query.getParams());
                    if (!view.getQuery().loadsAllData()) {
                        removed.add(view.getQuery());
                    }
                }
            }
        }
        if (hadCompleteView && !hasCompleteView()) {
            removed.add(QuerySpec.defaultQueryAtPath(query.getPath()));
        }
        return new Pair(removed, cancelEvents);
    }

    public List<View> getQueryViews() {
        List<View> views = new ArrayList();
        for (Entry<QueryParams, View> entry : this.views.entrySet()) {
            View view = (View) entry.getValue();
            if (!view.getQuery().loadsAllData()) {
                views.add(view);
            }
        }
        return views;
    }

    public Node getCompleteServerCache(Path path) {
        for (View view : this.views.values()) {
            if (view.getCompleteServerCache(path) != null) {
                return view.getCompleteServerCache(path);
            }
        }
        return null;
    }

    public View viewForQuery(QuerySpec query) {
        if (query.loadsAllData()) {
            return getCompleteView();
        }
        return (View) this.views.get(query.getParams());
    }

    public boolean viewExistsForQuery(QuerySpec query) {
        return viewForQuery(query) != null;
    }

    public boolean hasCompleteView() {
        return getCompleteView() != null;
    }

    public View getCompleteView() {
        for (Entry<QueryParams, View> entry : this.views.entrySet()) {
            View view = (View) entry.getValue();
            if (view.getQuery().loadsAllData()) {
                return view;
            }
        }
        return null;
    }
}
