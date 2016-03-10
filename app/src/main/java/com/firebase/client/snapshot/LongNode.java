package com.firebase.client.snapshot;

import com.firebase.client.utilities.Utilities;

public class LongNode extends LeafNode<LongNode> {
    private final long value;

    public LongNode(Long value, Node priority) {
        super(priority);
        this.value = value.longValue();
    }

    public Object getValue() {
        return Long.valueOf(this.value);
    }

    public String getHashString() {
        return (getPriorityHash() + "number:") + Utilities.doubleToHashString((double) this.value);
    }

    public LongNode updatePriority(Node priority) {
        return new LongNode(Long.valueOf(this.value), priority);
    }

    protected LeafType getLeafType() {
        return LeafType.Number;
    }

    protected int compareLeafValues(LongNode other) {
        return Utilities.compareLongs(this.value, other.value);
    }

    public boolean equals(Object other) {
        if (!(other instanceof LongNode)) {
            return false;
        }
        LongNode otherLongNode = (LongNode) other;
        if (this.value == otherLongNode.value && this.priority.equals(otherLongNode.priority)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return ((int) (this.value ^ (this.value >>> 32))) + this.priority.hashCode();
    }
}
