package com.firebase.client.snapshot;

import java.util.Comparator;

public abstract class Index implements Comparator<NamedNode> {
    public abstract String getQueryDefinition();

    public abstract boolean isDefinedOn(Node node);

    public abstract NamedNode makePost(ChildKey childKey, Node node);

    public abstract NamedNode maxPost();

    public boolean indexedValueChanged(Node oldNode, Node newNode) {
        return compare(new NamedNode(ChildKey.getMinName(), oldNode), new NamedNode(ChildKey.getMinName(), newNode)) != 0;
    }

    public NamedNode minPost() {
        return NamedNode.getMinNode();
    }

    public static Index fromQueryDefinition(String str) {
        if (str.equals(".value")) {
            return ValueIndex.getInstance();
        }
        if (str.equals(".key")) {
            return KeyIndex.getInstance();
        }
        if (!str.equals(".priority")) {
            return new SubKeyIndex(ChildKey.fromString(str));
        }
        throw new IllegalStateException("queryDefinition shouldn't ever be .priority since it's the default");
    }

    public int compare(NamedNode one, NamedNode two, boolean reverse) {
        if (reverse) {
            return compare(two, one);
        }
        return compare(one, two);
    }
}
