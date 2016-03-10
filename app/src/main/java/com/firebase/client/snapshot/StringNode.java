package com.firebase.client.snapshot;

public class StringNode extends LeafNode<StringNode> {
    private final String value;

    public StringNode(String value, Node priority) {
        super(priority);
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }

    public String getHashString() {
        return getPriorityHash() + "string:" + this.value;
    }

    public StringNode updatePriority(Node priority) {
        return new StringNode(this.value, priority);
    }

    protected LeafType getLeafType() {
        return LeafType.String;
    }

    protected int compareLeafValues(StringNode other) {
        return this.value.compareTo(other.value);
    }

    public boolean equals(Object other) {
        if (!(other instanceof StringNode)) {
            return false;
        }
        StringNode otherStringNode = (StringNode) other;
        if (this.value.equals(otherStringNode.value) && this.priority.equals(otherStringNode.priority)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.value.hashCode() + this.priority.hashCode();
    }
}
