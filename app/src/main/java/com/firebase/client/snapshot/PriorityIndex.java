package com.firebase.client.snapshot;

public class PriorityIndex extends Index {
    private static final PriorityIndex INSTANCE = new PriorityIndex();

    public static PriorityIndex getInstance() {
        return INSTANCE;
    }

    private PriorityIndex() {
    }

    public int compare(NamedNode a, NamedNode b) {
        return NodeUtilities.nameAndPriorityCompare(a.getName(), a.getNode().getPriority(), b.getName(), b.getNode().getPriority());
    }

    public boolean isDefinedOn(Node a) {
        return !a.getPriority().isEmpty();
    }

    public NamedNode makePost(ChildKey name, Node value) {
        return new NamedNode(name, new StringNode("[PRIORITY-POST]", value));
    }

    public NamedNode maxPost() {
        return makePost(ChildKey.getMaxName(), Node.MAX_NODE);
    }

    public String getQueryDefinition() {
        throw new IllegalArgumentException("Can't get query definition on priority index!");
    }

    public boolean equals(Object o) {
        return o instanceof PriorityIndex;
    }

    public int hashCode() {
        return 3155577;
    }

    public String toString() {
        return "PriorityIndex";
    }
}
