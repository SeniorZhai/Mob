package com.firebase.client.core.view.filter;

import com.firebase.client.core.view.Change;
import com.firebase.client.core.view.filter.NodeFilter.CompleteChildSource;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.Index;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.client.snapshot.NamedNode;
import com.firebase.client.snapshot.Node;

public class IndexedFilter implements NodeFilter {
    static final /* synthetic */ boolean $assertionsDisabled = (!IndexedFilter.class.desiredAssertionStatus());
    private final Index index;

    public IndexedFilter(Index index) {
        this.index = index;
    }

    public IndexedNode updateChild(IndexedNode indexedNode, ChildKey key, Node newChild, CompleteChildSource source, ChildChangeAccumulator optChangeAccumulator) {
        if ($assertionsDisabled || indexedNode.hasIndex(this.index)) {
            Node snap = indexedNode.getNode();
            Node oldChild = snap.getImmediateChild(key);
            if (oldChild.equals(newChild)) {
                return indexedNode;
            }
            if (optChangeAccumulator != null) {
                if (newChild.isEmpty()) {
                    if (snap.hasChild(key)) {
                        optChangeAccumulator.trackChildChange(Change.childRemovedChange(key, oldChild));
                    } else if (!($assertionsDisabled || snap.isLeafNode())) {
                        throw new AssertionError("A child remove without an old child only makes sense on a leaf node");
                    }
                } else if (oldChild.isEmpty()) {
                    optChangeAccumulator.trackChildChange(Change.childAddedChange(key, newChild));
                } else {
                    optChangeAccumulator.trackChildChange(Change.childChangedChange(key, newChild, oldChild));
                }
            }
            return (snap.isLeafNode() && newChild.isEmpty()) ? indexedNode : indexedNode.updateChild(key, newChild);
        } else {
            throw new AssertionError("The index must match the filter");
        }
    }

    public IndexedNode updateFullNode(IndexedNode oldSnap, IndexedNode newSnap, ChildChangeAccumulator optChangeAccumulator) {
        if ($assertionsDisabled || newSnap.hasIndex(this.index)) {
            if (optChangeAccumulator != null) {
                for (NamedNode child : oldSnap.getNode()) {
                    if (!newSnap.getNode().hasChild(child.getName())) {
                        optChangeAccumulator.trackChildChange(Change.childRemovedChange(child.getName(), child.getNode()));
                    }
                }
                if (!newSnap.getNode().isLeafNode()) {
                    for (NamedNode child2 : newSnap.getNode()) {
                        if (oldSnap.getNode().hasChild(child2.getName())) {
                            Node oldChild = oldSnap.getNode().getImmediateChild(child2.getName());
                            if (!oldChild.equals(child2.getNode())) {
                                optChangeAccumulator.trackChildChange(Change.childChangedChange(child2.getName(), child2.getNode(), oldChild));
                            }
                        } else {
                            optChangeAccumulator.trackChildChange(Change.childAddedChange(child2.getName(), child2.getNode()));
                        }
                    }
                }
            }
            return newSnap;
        }
        throw new AssertionError("Can't use IndexedNode that doesn't have filter's index");
    }

    public IndexedNode updatePriority(IndexedNode oldSnap, Node newPriority) {
        return oldSnap.getNode().isEmpty() ? oldSnap : oldSnap.updatePriority(newPriority);
    }

    public NodeFilter getIndexedFilter() {
        return this;
    }

    public Index getIndex() {
        return this.index;
    }

    public boolean filtersNodes() {
        return false;
    }
}
