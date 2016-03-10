package com.firebase.client.core.view.filter;

import com.firebase.client.core.view.Change;
import com.firebase.client.core.view.QueryParams;
import com.firebase.client.core.view.filter.NodeFilter.CompleteChildSource;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.EmptyNode;
import com.firebase.client.snapshot.Index;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.client.snapshot.NamedNode;
import com.firebase.client.snapshot.Node;
import com.firebase.client.snapshot.PriorityUtilities;
import java.util.Iterator;

public class LimitedFilter implements NodeFilter {
    static final /* synthetic */ boolean $assertionsDisabled = (!LimitedFilter.class.desiredAssertionStatus());
    private final Index index;
    private final int limit;
    private final RangedFilter rangedFilter;
    private final boolean reverse;

    public LimitedFilter(QueryParams params) {
        this.rangedFilter = new RangedFilter(params);
        this.index = params.getIndex();
        this.limit = params.getLimit();
        this.reverse = !params.isViewFromLeft();
    }

    public IndexedNode updateChild(IndexedNode snap, ChildKey key, Node newChild, CompleteChildSource source, ChildChangeAccumulator optChangeAccumulator) {
        if (!this.rangedFilter.matches(new NamedNode(key, newChild))) {
            newChild = EmptyNode.Empty();
        }
        if (snap.getNode().getImmediateChild(key).equals(newChild)) {
            return snap;
        }
        if (snap.getNode().getChildCount() < this.limit) {
            return this.rangedFilter.getIndexedFilter().updateChild(snap, key, newChild, source, optChangeAccumulator);
        }
        return fullLimitUpdateChild(snap, key, newChild, source, optChangeAccumulator);
    }

    private IndexedNode fullLimitUpdateChild(IndexedNode oldIndexed, ChildKey childKey, Node childSnap, CompleteChildSource source, ChildChangeAccumulator optChangeAccumulator) {
        if ($assertionsDisabled || oldIndexed.getNode().getChildCount() == this.limit) {
            NamedNode newChildNamedNode = new NamedNode(childKey, childSnap);
            NamedNode windowBoundary = this.reverse ? oldIndexed.getFirstChild() : oldIndexed.getLastChild();
            boolean inRange = this.rangedFilter.matches(newChildNamedNode);
            if (oldIndexed.getNode().hasChild(childKey)) {
                Node oldChildSnap = oldIndexed.getNode().getImmediateChild(childKey);
                NamedNode nextChild = source.getChildAfterChild(this.index, windowBoundary, this.reverse);
                while (nextChild != null && (nextChild.getName().equals(childKey) || oldIndexed.getNode().hasChild(nextChild.getName()))) {
                    nextChild = source.getChildAfterChild(this.index, nextChild, this.reverse);
                }
                boolean remainsInWindow = inRange && !childSnap.isEmpty() && (nextChild == null ? 1 : this.index.compare(nextChild, newChildNamedNode, this.reverse)) >= 0;
                if (remainsInWindow) {
                    if (optChangeAccumulator != null) {
                        optChangeAccumulator.trackChildChange(Change.childChangedChange(childKey, childSnap, oldChildSnap));
                    }
                    return oldIndexed.updateChild(childKey, childSnap);
                }
                if (optChangeAccumulator != null) {
                    optChangeAccumulator.trackChildChange(Change.childRemovedChange(childKey, oldChildSnap));
                }
                IndexedNode newIndexed = oldIndexed.updateChild(childKey, EmptyNode.Empty());
                boolean nextChildInRange = nextChild != null && this.rangedFilter.matches(nextChild);
                if (!nextChildInRange) {
                    return newIndexed;
                }
                if (optChangeAccumulator != null) {
                    optChangeAccumulator.trackChildChange(Change.childAddedChange(nextChild.getName(), nextChild.getNode()));
                }
                return newIndexed.updateChild(nextChild.getName(), nextChild.getNode());
            } else if (childSnap.isEmpty() || !inRange || this.index.compare(windowBoundary, newChildNamedNode, this.reverse) < 0) {
                return oldIndexed;
            } else {
                if (optChangeAccumulator != null) {
                    optChangeAccumulator.trackChildChange(Change.childRemovedChange(windowBoundary.getName(), windowBoundary.getNode()));
                    optChangeAccumulator.trackChildChange(Change.childAddedChange(childKey, childSnap));
                }
                return oldIndexed.updateChild(childKey, childSnap).updateChild(windowBoundary.getName(), EmptyNode.Empty());
            }
        }
        throw new AssertionError();
    }

    public IndexedNode updateFullNode(IndexedNode oldSnap, IndexedNode newSnap, ChildChangeAccumulator optChangeAccumulator) {
        IndexedNode filtered;
        if (newSnap.getNode().isLeafNode() || newSnap.getNode().isEmpty()) {
            filtered = IndexedNode.from(EmptyNode.Empty(), this.index);
        } else {
            Iterator<NamedNode> iterator;
            NamedNode endPost;
            int sign;
            filtered = newSnap.updatePriority(PriorityUtilities.NullPriority());
            NamedNode startPost;
            if (this.reverse) {
                iterator = newSnap.reverseIterator();
                startPost = this.rangedFilter.getEndPost();
                endPost = this.rangedFilter.getStartPost();
                sign = -1;
            } else {
                iterator = newSnap.iterator();
                startPost = this.rangedFilter.getStartPost();
                endPost = this.rangedFilter.getEndPost();
                sign = 1;
            }
            int count = 0;
            boolean foundStartPost = false;
            while (iterator.hasNext()) {
                NamedNode next = (NamedNode) iterator.next();
                if (!foundStartPost && this.index.compare(startPost, next) * sign <= 0) {
                    foundStartPost = true;
                }
                boolean inRange = foundStartPost && count < this.limit && this.index.compare(next, endPost) * sign <= 0;
                if (inRange) {
                    count++;
                } else {
                    filtered = filtered.updateChild(next.getName(), EmptyNode.Empty());
                }
            }
        }
        return this.rangedFilter.getIndexedFilter().updateFullNode(oldSnap, filtered, optChangeAccumulator);
    }

    public IndexedNode updatePriority(IndexedNode oldSnap, Node newPriority) {
        return oldSnap;
    }

    public NodeFilter getIndexedFilter() {
        return this.rangedFilter.getIndexedFilter();
    }

    public Index getIndex() {
        return this.index;
    }

    public boolean filtersNodes() {
        return true;
    }
}
