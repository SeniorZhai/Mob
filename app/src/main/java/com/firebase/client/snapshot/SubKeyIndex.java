package com.firebase.client.snapshot;

public class SubKeyIndex extends Index {
    private final ChildKey indexKey;

    public SubKeyIndex(ChildKey indexKey) {
        if (indexKey.isPriorityChildName()) {
            throw new IllegalArgumentException("Can't create SubKeyIndex with '.priority' as key. Please use PriorityIndex instead!");
        }
        this.indexKey = indexKey;
    }

    public boolean isDefinedOn(Node snapshot) {
        return !snapshot.getImmediateChild(this.indexKey).isEmpty();
    }

    public int compare(NamedNode a, NamedNode b) {
        int indexCmp = a.getNode().getImmediateChild(this.indexKey).compareTo(b.getNode().getImmediateChild(this.indexKey));
        if (indexCmp == 0) {
            return a.getName().compareTo(b.getName());
        }
        return indexCmp;
    }

    public NamedNode makePost(ChildKey name, Node value) {
        return new NamedNode(name, EmptyNode.Empty().updateImmediateChild(this.indexKey, value));
    }

    public NamedNode maxPost() {
        return new NamedNode(ChildKey.getMaxName(), EmptyNode.Empty().updateImmediateChild(this.indexKey, Node.MAX_NODE));
    }

    public String getQueryDefinition() {
        return this.indexKey.asString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (this.indexKey.equals(((SubKeyIndex) o).indexKey)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.indexKey.hashCode();
    }
}
