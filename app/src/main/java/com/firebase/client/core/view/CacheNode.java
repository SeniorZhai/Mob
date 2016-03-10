package com.firebase.client.core.view;

import com.firebase.client.core.Path;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.client.snapshot.Node;

public class CacheNode {
    private final boolean filtered;
    private final boolean fullyInitialized;
    private final IndexedNode indexedNode;

    public CacheNode(IndexedNode node, boolean fullyInitialized, boolean filtered) {
        this.indexedNode = node;
        this.fullyInitialized = fullyInitialized;
        this.filtered = filtered;
    }

    public boolean isFullyInitialized() {
        return this.fullyInitialized;
    }

    public boolean isFiltered() {
        return this.filtered;
    }

    public boolean isCompleteForPath(Path path) {
        if (path.isEmpty()) {
            return isFullyInitialized() && !this.filtered;
        } else {
            return isCompleteForChild(path.getFront());
        }
    }

    public boolean isCompleteForChild(ChildKey key) {
        return (isFullyInitialized() && !this.filtered) || this.indexedNode.getNode().hasChild(key);
    }

    public Node getNode() {
        return this.indexedNode.getNode();
    }

    public IndexedNode getIndexedNode() {
        return this.indexedNode;
    }
}
