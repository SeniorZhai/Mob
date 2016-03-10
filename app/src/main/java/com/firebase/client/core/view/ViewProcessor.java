package com.firebase.client.core.view;

import com.firebase.client.core.CompoundWrite;
import com.firebase.client.core.Path;
import com.firebase.client.core.WriteTreeRef;
import com.firebase.client.core.operation.AckUserWrite;
import com.firebase.client.core.operation.Merge;
import com.firebase.client.core.operation.Operation;
import com.firebase.client.core.operation.Operation.OperationType;
import com.firebase.client.core.operation.Overwrite;
import com.firebase.client.core.view.filter.ChildChangeAccumulator;
import com.firebase.client.core.view.filter.NodeFilter;
import com.firebase.client.core.view.filter.NodeFilter.CompleteChildSource;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.ChildrenNode;
import com.firebase.client.snapshot.EmptyNode;
import com.firebase.client.snapshot.Index;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.client.snapshot.KeyIndex;
import com.firebase.client.snapshot.NamedNode;
import com.firebase.client.snapshot.Node;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ViewProcessor {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static CompleteChildSource NO_COMPLETE_SOURCE = new CompleteChildSource() {
        public Node getCompleteChild(ChildKey childKey) {
            return null;
        }

        public NamedNode getChildAfterChild(Index index, NamedNode child, boolean reverse) {
            return null;
        }
    };
    private final NodeFilter filter;

    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$firebase$client$core$operation$Operation$OperationType = new int[OperationType.values().length];

        static {
            try {
                $SwitchMap$com$firebase$client$core$operation$Operation$OperationType[OperationType.Overwrite.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$firebase$client$core$operation$Operation$OperationType[OperationType.Merge.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$firebase$client$core$operation$Operation$OperationType[OperationType.AckUserWrite.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$firebase$client$core$operation$Operation$OperationType[OperationType.ListenComplete.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public static class ProcessorResult {
        public final List<Change> changes;
        public final ViewCache viewCache;

        public ProcessorResult(ViewCache viewCache, List<Change> changes) {
            this.viewCache = viewCache;
            this.changes = changes;
        }
    }

    private static class WriteTreeCompleteChildSource implements CompleteChildSource {
        private final Node optCompleteServerCache;
        private final ViewCache viewCache;
        private final WriteTreeRef writes;

        public WriteTreeCompleteChildSource(WriteTreeRef writes, ViewCache viewCache, Node optCompleteServerCache) {
            this.writes = writes;
            this.viewCache = viewCache;
            this.optCompleteServerCache = optCompleteServerCache;
        }

        public Node getCompleteChild(ChildKey childKey) {
            CacheNode node = this.viewCache.getEventCache();
            if (node.isCompleteForChild(childKey)) {
                return node.getNode().getImmediateChild(childKey);
            }
            CacheNode serverNode;
            if (this.optCompleteServerCache != null) {
                serverNode = new CacheNode(IndexedNode.from(this.optCompleteServerCache, KeyIndex.getInstance()), true, false);
            } else {
                serverNode = this.viewCache.getServerCache();
            }
            return this.writes.calcCompleteChild(childKey, serverNode);
        }

        public NamedNode getChildAfterChild(Index index, NamedNode child, boolean reverse) {
            return this.writes.calcNextNodeAfterPost(this.optCompleteServerCache != null ? this.optCompleteServerCache : this.viewCache.getCompleteServerSnap(), child, reverse, index);
        }
    }

    static {
        boolean z;
        if (ViewProcessor.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        $assertionsDisabled = z;
    }

    public ViewProcessor(NodeFilter filter) {
        this.filter = filter;
    }

    public ProcessorResult applyOperation(ViewCache oldViewCache, Operation operation, WriteTreeRef writesCache, Node optCompleteCache) {
        ViewCache newViewCache;
        ChildChangeAccumulator accumulator = new ChildChangeAccumulator();
        ViewCache viewCache;
        switch (AnonymousClass2.$SwitchMap$com$firebase$client$core$operation$Operation$OperationType[operation.getType().ordinal()]) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                Overwrite overwrite = (Overwrite) operation;
                if (overwrite.getSource().isFromUser()) {
                    newViewCache = applyUserOverwrite(oldViewCache, overwrite.getPath(), overwrite.getSnapshot(), writesCache, optCompleteCache, accumulator);
                    break;
                } else if ($assertionsDisabled || overwrite.getSource().isFromServer()) {
                    viewCache = oldViewCache;
                    newViewCache = applyServerOverwrite(viewCache, overwrite.getPath(), overwrite.getSnapshot(), writesCache, optCompleteCache, overwrite.getSource().isTagged(), accumulator);
                    break;
                } else {
                    throw new AssertionError();
                }
                break;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                Merge merge = (Merge) operation;
                if (merge.getSource().isFromUser()) {
                    newViewCache = applyUserMerge(oldViewCache, merge.getPath(), merge.getChildren(), writesCache, optCompleteCache, accumulator);
                    break;
                } else if ($assertionsDisabled || merge.getSource().isFromServer()) {
                    viewCache = oldViewCache;
                    newViewCache = applyServerMerge(viewCache, merge.getPath(), merge.getChildren(), writesCache, optCompleteCache, merge.getSource().isTagged(), accumulator);
                    break;
                } else {
                    throw new AssertionError();
                }
                break;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                AckUserWrite ackUserWrite = (AckUserWrite) operation;
                if (!ackUserWrite.isRevert()) {
                    newViewCache = ackUserWrite(oldViewCache, ackUserWrite.getPath(), writesCache, optCompleteCache, accumulator);
                    break;
                }
                newViewCache = revertUserWrite(oldViewCache, ackUserWrite.getPath(), writesCache, optCompleteCache, accumulator);
                break;
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                newViewCache = listenComplete(oldViewCache, operation.getPath(), writesCache, optCompleteCache, accumulator);
                break;
            default:
                throw new AssertionError("Unknown operation: " + operation.getType());
        }
        List<Change> arrayList = new ArrayList(accumulator.getChanges());
        maybeAddValueEvent(oldViewCache, newViewCache, arrayList);
        return new ProcessorResult(newViewCache, arrayList);
    }

    private void maybeAddValueEvent(ViewCache oldViewCache, ViewCache newViewCache, List<Change> accumulator) {
        CacheNode eventSnap = newViewCache.getEventCache();
        if (eventSnap.isFullyInitialized()) {
            boolean isLeafOrEmpty = eventSnap.getNode().isLeafNode() || eventSnap.getNode().isEmpty();
            if (!accumulator.isEmpty() || !oldViewCache.getEventCache().isFullyInitialized() || ((isLeafOrEmpty && !eventSnap.getNode().equals(oldViewCache.getCompleteEventSnap())) || !eventSnap.getNode().getPriority().equals(oldViewCache.getCompleteEventSnap().getPriority()))) {
                accumulator.add(Change.valueChange(eventSnap.getIndexedNode()));
            }
        }
    }

    private ViewCache generateEventCacheAfterServerEvent(ViewCache viewCache, Path changePath, WriteTreeRef writesCache, CompleteChildSource source, ChildChangeAccumulator accumulator) {
        CacheNode oldEventSnap = viewCache.getEventCache();
        if (writesCache.shadowingWrite(changePath) != null) {
            return viewCache;
        }
        IndexedNode newEventCache;
        boolean z;
        if (!changePath.isEmpty()) {
            ChildKey childKey = changePath.getFront();
            if (!childKey.isPriorityChildName()) {
                Node newEventChild;
                Path childChangePath = changePath.popFront();
                if (oldEventSnap.isCompleteForChild(childKey)) {
                    Node eventChildUpdate = writesCache.calcEventCacheAfterServerOverwrite(changePath, oldEventSnap.getNode(), viewCache.getServerCache().getNode());
                    if (eventChildUpdate != null) {
                        newEventChild = oldEventSnap.getNode().getImmediateChild(childKey).updateChild(childChangePath, eventChildUpdate);
                    } else {
                        newEventChild = oldEventSnap.getNode().getImmediateChild(childKey);
                    }
                } else {
                    newEventChild = writesCache.calcCompleteChild(childKey, viewCache.getServerCache());
                }
                if (newEventChild != null) {
                    newEventCache = this.filter.updateChild(oldEventSnap.getIndexedNode(), childKey, newEventChild, source, accumulator);
                } else {
                    newEventCache = oldEventSnap.getIndexedNode();
                }
            } else if ($assertionsDisabled || changePath.size() == 1) {
                Node updatedPriority = writesCache.calcEventCacheAfterServerOverwrite(changePath, oldEventSnap.getNode(), viewCache.getServerCache().getNode());
                if (updatedPriority != null) {
                    newEventCache = this.filter.updatePriority(oldEventSnap.getIndexedNode(), updatedPriority);
                } else {
                    newEventCache = oldEventSnap.getIndexedNode();
                }
            } else {
                throw new AssertionError("Can't have a priority with additional path components");
            }
        } else if ($assertionsDisabled || viewCache.getServerCache().isFullyInitialized()) {
            Node nodeWithLocalWrites;
            if (viewCache.getServerCache().isFiltered()) {
                Node serverCache = viewCache.getCompleteServerSnap();
                nodeWithLocalWrites = writesCache.calcCompleteEventChildren(serverCache instanceof ChildrenNode ? serverCache : EmptyNode.Empty());
            } else {
                nodeWithLocalWrites = writesCache.calcCompleteEventCache(viewCache.getCompleteServerSnap());
            }
            newEventCache = this.filter.updateFullNode(viewCache.getEventCache().getIndexedNode(), IndexedNode.from(nodeWithLocalWrites, this.filter.getIndex()), accumulator);
        } else {
            throw new AssertionError("If change path is empty, we must have complete server data");
        }
        if (oldEventSnap.isFullyInitialized() || changePath.isEmpty()) {
            z = true;
        } else {
            z = false;
        }
        return viewCache.updateEventSnap(newEventCache, z, this.filter.filtersNodes());
    }

    private ViewCache applyServerOverwrite(ViewCache oldViewCache, Path changePath, Node changedSnap, WriteTreeRef writesCache, Node optCompleteCache, boolean constrainServerNode, ChildChangeAccumulator accumulator) {
        IndexedNode newServerCache;
        CacheNode oldServerSnap = oldViewCache.getServerCache();
        NodeFilter serverFilter = constrainServerNode ? this.filter : this.filter.getIndexedFilter();
        if (changePath.isEmpty()) {
            newServerCache = serverFilter.updateFullNode(oldServerSnap.getIndexedNode(), IndexedNode.from(changedSnap, serverFilter.getIndex()), null);
        } else if (!serverFilter.filtersNodes() || oldServerSnap.isFiltered()) {
            childKey = changePath.getFront();
            if (!oldServerSnap.isCompleteForPath(changePath) && changePath.size() > 1) {
                return oldViewCache;
            }
            Node newChildNode = oldServerSnap.getNode().getImmediateChild(childKey).updateChild(changePath.popFront(), changedSnap);
            if (childKey.isPriorityChildName()) {
                newServerCache = serverFilter.updatePriority(oldServerSnap.getIndexedNode(), newChildNode);
            } else {
                newServerCache = serverFilter.updateChild(oldServerSnap.getIndexedNode(), childKey, newChildNode, NO_COMPLETE_SOURCE, null);
            }
        } else if ($assertionsDisabled || !changePath.isEmpty()) {
            childKey = changePath.getFront();
            newServerCache = serverFilter.updateFullNode(oldServerSnap.getIndexedNode(), oldServerSnap.getIndexedNode().updateChild(childKey, oldServerSnap.getNode().getImmediateChild(childKey).updateChild(changePath.popFront(), changedSnap)), null);
        } else {
            throw new AssertionError("An empty path should have been caught in the other branch");
        }
        boolean z = oldServerSnap.isFullyInitialized() || changePath.isEmpty();
        ViewCache newViewCache = oldViewCache.updateServerSnap(newServerCache, z, serverFilter.filtersNodes());
        return generateEventCacheAfterServerEvent(newViewCache, changePath, writesCache, new WriteTreeCompleteChildSource(writesCache, newViewCache, optCompleteCache), accumulator);
    }

    private ViewCache applyUserOverwrite(ViewCache oldViewCache, Path changePath, Node changedSnap, WriteTreeRef writesCache, Node optCompleteCache, ChildChangeAccumulator accumulator) {
        CacheNode oldEventSnap = oldViewCache.getEventCache();
        CompleteChildSource source = new WriteTreeCompleteChildSource(writesCache, oldViewCache, optCompleteCache);
        if (changePath.isEmpty()) {
            return oldViewCache.updateEventSnap(this.filter.updateFullNode(oldViewCache.getEventCache().getIndexedNode(), IndexedNode.from(changedSnap, this.filter.getIndex()), accumulator), true, this.filter.filtersNodes());
        }
        ChildKey childKey = changePath.getFront();
        if (childKey.isPriorityChildName()) {
            return oldViewCache.updateEventSnap(this.filter.updatePriority(oldViewCache.getEventCache().getIndexedNode(), changedSnap), oldEventSnap.isFullyInitialized(), oldEventSnap.isFiltered());
        }
        Node newChild;
        Path childChangePath = changePath.popFront();
        Node oldChild = oldEventSnap.getNode().getImmediateChild(childKey);
        if (childChangePath.isEmpty()) {
            newChild = changedSnap;
        } else {
            Node childNode = source.getCompleteChild(childKey);
            if (childNode == null) {
                newChild = EmptyNode.Empty();
            } else if (childChangePath.getBack().isPriorityChildName() && childNode.getChild(childChangePath.getParent()).isEmpty()) {
                newChild = childNode;
            } else {
                newChild = childNode.updateChild(childChangePath, changedSnap);
            }
        }
        if (oldChild.equals(newChild)) {
            return oldViewCache;
        }
        return oldViewCache.updateEventSnap(this.filter.updateChild(oldEventSnap.getIndexedNode(), childKey, newChild, source, accumulator), oldEventSnap.isFullyInitialized(), this.filter.filtersNodes());
    }

    private static boolean cacheHasChild(ViewCache viewCache, ChildKey childKey) {
        return viewCache.getEventCache().isCompleteForChild(childKey);
    }

    private ViewCache applyUserMerge(ViewCache viewCache, Path path, CompoundWrite changedChildren, WriteTreeRef writesCache, Node serverCache, ChildChangeAccumulator accumulator) {
        if ($assertionsDisabled || changedChildren.rootWrite() == null) {
            Entry<Path, Node> entry;
            Path writePath;
            ViewCache currentViewCache = viewCache;
            Iterator i$ = changedChildren.iterator();
            while (i$.hasNext()) {
                entry = (Entry) i$.next();
                writePath = path.child((Path) entry.getKey());
                if (cacheHasChild(viewCache, writePath.getFront())) {
                    currentViewCache = applyUserOverwrite(currentViewCache, writePath, (Node) entry.getValue(), writesCache, serverCache, accumulator);
                }
            }
            i$ = changedChildren.iterator();
            while (i$.hasNext()) {
                entry = (Entry) i$.next();
                writePath = path.child((Path) entry.getKey());
                if (!cacheHasChild(viewCache, writePath.getFront())) {
                    currentViewCache = applyUserOverwrite(currentViewCache, writePath, (Node) entry.getValue(), writesCache, serverCache, accumulator);
                }
            }
            return currentViewCache;
        }
        throw new AssertionError("Can't have a merge that is an overwrite");
    }

    private ViewCache applyServerMerge(ViewCache viewCache, Path path, CompoundWrite changedChildren, WriteTreeRef writesCache, Node serverCache, boolean constrainServerNode, ChildChangeAccumulator accumulator) {
        if (viewCache.getServerCache().getNode().isEmpty() && !viewCache.getServerCache().isFullyInitialized()) {
            return viewCache;
        }
        ViewCache curViewCache = viewCache;
        if ($assertionsDisabled || changedChildren.rootWrite() == null) {
            CompoundWrite actualMerge;
            ChildKey childKey;
            if (path.isEmpty()) {
                actualMerge = changedChildren;
            } else {
                actualMerge = CompoundWrite.emptyWrite().addWrites(path, changedChildren);
            }
            Node serverNode = viewCache.getServerCache().getNode();
            Map<ChildKey, CompoundWrite> childCompoundWrites = actualMerge.childCompoundWrites();
            for (Entry<ChildKey, CompoundWrite> childMerge : childCompoundWrites.entrySet()) {
                childKey = (ChildKey) childMerge.getKey();
                if (serverNode.hasChild(childKey)) {
                    curViewCache = applyServerOverwrite(curViewCache, new Path(childKey), ((CompoundWrite) childMerge.getValue()).apply(serverNode.getImmediateChild(childKey)), writesCache, serverCache, constrainServerNode, accumulator);
                }
            }
            for (Entry<ChildKey, CompoundWrite> childMerge2 : childCompoundWrites.entrySet()) {
                childKey = (ChildKey) childMerge2.getKey();
                boolean isUnknownDeepMerge = !viewCache.getServerCache().isFullyInitialized() && ((CompoundWrite) childMerge2.getValue()).rootWrite() == null;
                if (!(serverNode.hasChild(childKey) || isUnknownDeepMerge)) {
                    curViewCache = applyServerOverwrite(curViewCache, new Path(childKey), ((CompoundWrite) childMerge2.getValue()).apply(serverNode.getImmediateChild(childKey)), writesCache, serverCache, constrainServerNode, accumulator);
                }
            }
            return curViewCache;
        }
        throw new AssertionError("Can't have a merge that is an overwrite");
    }

    private ViewCache ackUserWrite(ViewCache viewCache, Path ackPath, WriteTreeRef writesCache, Node optCompleteCache, ChildChangeAccumulator accumulator) {
        if (writesCache.shadowingWrite(ackPath) != null) {
            return viewCache;
        }
        boolean eventCacheComplete;
        CompleteChildSource source = new WriteTreeCompleteChildSource(writesCache, viewCache, optCompleteCache);
        IndexedNode oldEventCache = viewCache.getEventCache().getIndexedNode();
        IndexedNode newEventCache = oldEventCache;
        ChildKey childKey;
        if (viewCache.getServerCache().isFullyInitialized()) {
            if (ackPath.isEmpty()) {
                newEventCache = this.filter.updateFullNode(viewCache.getEventCache().getIndexedNode(), IndexedNode.from(writesCache.calcCompleteEventCache(viewCache.getCompleteServerSnap()), this.filter.getIndex()), accumulator);
            } else if (ackPath.getFront().isPriorityChildName()) {
                Node updatedPriority = writesCache.calcCompleteChild(ackPath.getFront(), viewCache.getServerCache());
                if (!(updatedPriority == null || oldEventCache.getNode().isEmpty() || oldEventCache.getNode().getPriority().equals(updatedPriority))) {
                    newEventCache = this.filter.updatePriority(oldEventCache, updatedPriority);
                }
            } else {
                childKey = ackPath.getFront();
                Node updatedChild = writesCache.calcCompleteChild(childKey, viewCache.getServerCache());
                if (updatedChild != null) {
                    newEventCache = this.filter.updateChild(viewCache.getEventCache().getIndexedNode(), childKey, updatedChild, source, accumulator);
                }
            }
            eventCacheComplete = true;
        } else if (viewCache.getEventCache().isFullyInitialized() || ackPath.isEmpty()) {
            newEventCache = oldEventCache;
            for (NamedNode entry : viewCache.getEventCache().getNode()) {
                completeChild = writesCache.calcCompleteChild(entry.getName(), viewCache.getServerCache());
                if (completeChild != null) {
                    newEventCache = this.filter.updateChild(newEventCache, entry.getName(), completeChild, source, accumulator);
                }
            }
            eventCacheComplete = viewCache.getEventCache().isFullyInitialized();
        } else {
            childKey = ackPath.getFront();
            if (ackPath.size() == 1 || viewCache.getEventCache().isCompleteForChild(childKey)) {
                completeChild = writesCache.calcCompleteChild(childKey, viewCache.getServerCache());
                if (completeChild != null) {
                    newEventCache = this.filter.updateChild(oldEventCache, childKey, completeChild, source, accumulator);
                }
            }
            eventCacheComplete = false;
        }
        return viewCache.updateEventSnap(newEventCache, eventCacheComplete, this.filter.filtersNodes());
    }

    public ViewCache revertUserWrite(ViewCache viewCache, Path path, WriteTreeRef writesCache, Node optCompleteServerCache, ChildChangeAccumulator accumulator) {
        if (writesCache.shadowingWrite(path) != null) {
            return viewCache;
        }
        IndexedNode newEventCache;
        boolean complete;
        CompleteChildSource source = new WriteTreeCompleteChildSource(writesCache, viewCache, optCompleteServerCache);
        IndexedNode oldEventCache = viewCache.getEventCache().getIndexedNode();
        if (path.isEmpty() || path.getFront().isPriorityChildName()) {
            Node newNode;
            if (viewCache.getServerCache().isFullyInitialized()) {
                newNode = writesCache.calcCompleteEventCache(viewCache.getCompleteServerSnap());
            } else {
                newNode = writesCache.calcCompleteEventChildren(viewCache.getServerCache().getNode());
            }
            newEventCache = this.filter.updateFullNode(oldEventCache, IndexedNode.from(newNode, this.filter.getIndex()), accumulator);
        } else {
            ChildKey childKey = path.getFront();
            Node newChild = writesCache.calcCompleteChild(childKey, viewCache.getServerCache());
            if (newChild == null && viewCache.getServerCache().isCompleteForChild(childKey)) {
                newChild = oldEventCache.getNode().getImmediateChild(childKey);
            }
            if (newChild != null) {
                newEventCache = this.filter.updateChild(oldEventCache, childKey, newChild, source, accumulator);
            } else if (newChild == null && viewCache.getEventCache().getNode().hasChild(childKey)) {
                newEventCache = this.filter.updateChild(oldEventCache, childKey, EmptyNode.Empty(), source, accumulator);
            } else {
                newEventCache = oldEventCache;
            }
            if (newEventCache.getNode().isEmpty() && viewCache.getServerCache().isFullyInitialized()) {
                Node complete2 = writesCache.calcCompleteEventCache(viewCache.getCompleteServerSnap());
                if (complete2.isLeafNode()) {
                    newEventCache = this.filter.updateFullNode(newEventCache, IndexedNode.from(complete2, this.filter.getIndex()), accumulator);
                }
            }
        }
        if (!viewCache.getServerCache().isFullyInitialized()) {
            if (writesCache.shadowingWrite(Path.getEmptyPath()) == null) {
                complete = false;
                return viewCache.updateEventSnap(newEventCache, complete, this.filter.filtersNodes());
            }
        }
        complete = true;
        return viewCache.updateEventSnap(newEventCache, complete, this.filter.filtersNodes());
    }

    private ViewCache listenComplete(ViewCache viewCache, Path path, WriteTreeRef writesCache, Node serverCache, ChildChangeAccumulator accumulator) {
        CacheNode oldServerNode = viewCache.getServerCache();
        IndexedNode indexedNode = oldServerNode.getIndexedNode();
        boolean z = oldServerNode.isFullyInitialized() || path.isEmpty();
        return generateEventCacheAfterServerEvent(viewCache.updateServerSnap(indexedNode, z, oldServerNode.isFiltered()), path, writesCache, NO_COMPLETE_SOURCE, accumulator);
    }
}
