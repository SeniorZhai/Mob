package com.firebase.client.snapshot;

public class BooleanNode extends LeafNode<BooleanNode> {
    private final boolean value;

    public BooleanNode(Boolean value, Node priority) {
        super(priority);
        this.value = value.booleanValue();
    }

    public Object getValue() {
        return Boolean.valueOf(this.value);
    }

    public String getHashString() {
        return getPriorityHash() + "boolean:" + this.value;
    }

    public BooleanNode updatePriority(Node priority) {
        return new BooleanNode(Boolean.valueOf(this.value), priority);
    }

    protected LeafType getLeafType() {
        return LeafType.Boolean;
    }

    protected int compareLeafValues(BooleanNode other) {
        if (this.value == other.value) {
            return 0;
        }
        return this.value ? 1 : -1;
    }

    public boolean equals(Object other) {
        if (!(other instanceof BooleanNode)) {
            return false;
        }
        BooleanNode otherBooleanNode = (BooleanNode) other;
        if (this.value == otherBooleanNode.value && this.priority.equals(otherBooleanNode.priority)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (this.value ? 1 : 0) + this.priority.hashCode();
    }
}
