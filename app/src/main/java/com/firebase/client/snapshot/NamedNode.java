package com.firebase.client.snapshot;

public class NamedNode {
    private static final NamedNode MAX_NODE = new NamedNode(ChildKey.getMaxName(), Node.MAX_NODE);
    private static final NamedNode MIN_NODE = new NamedNode(ChildKey.getMinName(), EmptyNode.Empty());
    private final ChildKey name;
    private final Node node;

    public static NamedNode getMinNode() {
        return MIN_NODE;
    }

    public static NamedNode getMaxNode() {
        return MAX_NODE;
    }

    public NamedNode(ChildKey name, Node node) {
        this.name = name;
        this.node = node;
    }

    public ChildKey getName() {
        return this.name;
    }

    public Node getNode() {
        return this.node;
    }

    public String toString() {
        return "NamedNode{name=" + this.name + ", node=" + this.node + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NamedNode namedNode = (NamedNode) o;
        if (!this.name.equals(namedNode.name)) {
            return false;
        }
        if (this.node.equals(namedNode.node)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (this.name.hashCode() * 31) + this.node.hashCode();
    }
}
