package com.firebase.client.snapshot;

import com.firebase.client.core.Path;
import com.firebase.client.utilities.Utilities;
import io.fabric.sdk.android.BuildConfig;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public abstract class LeafNode<T extends LeafNode> implements Node {
    static final /* synthetic */ boolean $assertionsDisabled = (!LeafNode.class.desiredAssertionStatus());
    private String lazyHash;
    protected final Node priority;

    protected enum LeafType {
        DeferredValue,
        Boolean,
        Number,
        String
    }

    protected abstract int compareLeafValues(T t);

    public abstract boolean equals(Object obj);

    protected abstract LeafType getLeafType();

    public abstract int hashCode();

    LeafNode(Node priority) {
        this.priority = priority;
    }

    public boolean hasChild(ChildKey childKey) {
        return false;
    }

    public boolean isLeafNode() {
        return true;
    }

    public Node getPriority() {
        return this.priority;
    }

    public Node getChild(Path path) {
        if (path.isEmpty()) {
            return this;
        }
        if (path.getFront().isPriorityChildName()) {
            return this.priority;
        }
        return EmptyNode.Empty();
    }

    public Node updateChild(Path path, Node node) {
        ChildKey front = path.getFront();
        if (front == null) {
            return node;
        }
        if (node.isEmpty() && !front.isPriorityChildName()) {
            return this;
        }
        if ($assertionsDisabled || !path.getFront().isPriorityChildName() || path.size() == 1) {
            return updateImmediateChild(front, EmptyNode.Empty().updateChild(path.popFront(), node));
        }
        throw new AssertionError();
    }

    public boolean isEmpty() {
        return false;
    }

    public int getChildCount() {
        return 0;
    }

    public ChildKey getPredecessorChildKey(ChildKey childKey) {
        return null;
    }

    public ChildKey getSuccessorChildKey(ChildKey childKey) {
        return null;
    }

    public Node getImmediateChild(ChildKey name) {
        if (name.isPriorityChildName()) {
            return this.priority;
        }
        return EmptyNode.Empty();
    }

    public Object getValue(boolean useExportFormat) {
        if (!useExportFormat || this.priority.isEmpty()) {
            return getValue();
        }
        Object result = new HashMap();
        result.put(".value", getValue());
        result.put(".priority", this.priority.getValue());
        return result;
    }

    public Node updateImmediateChild(ChildKey name, Node node) {
        if (name.isPriorityChildName()) {
            return updatePriority(node);
        }
        return !node.isEmpty() ? EmptyNode.Empty().updateImmediateChild(name, node).updatePriority(this.priority) : this;
    }

    public String getHash() {
        if (this.lazyHash == null) {
            this.lazyHash = Utilities.sha1HexDigest(getHashString());
        }
        return this.lazyHash;
    }

    protected String getPriorityHash() {
        if (this.priority.isEmpty()) {
            return BuildConfig.FLAVOR;
        }
        return "priority:" + this.priority.getHashString() + ":";
    }

    public Iterator<NamedNode> iterator() {
        return Collections.emptyList().iterator();
    }

    public Iterator<NamedNode> reverseIterator() {
        return Collections.emptyList().iterator();
    }

    private static int compareLongDoubleNodes(LongNode longNode, DoubleNode doubleNode) {
        return Double.valueOf((double) ((Long) longNode.getValue()).longValue()).compareTo((Double) doubleNode.getValue());
    }

    public int compareTo(Node other) {
        if (other.isEmpty()) {
            return 1;
        }
        if (other instanceof ChildrenNode) {
            return -1;
        }
        if (!$assertionsDisabled && !other.isLeafNode()) {
            throw new AssertionError("Node is not leaf node!");
        } else if ((this instanceof LongNode) && (other instanceof DoubleNode)) {
            return compareLongDoubleNodes((LongNode) this, (DoubleNode) other);
        } else {
            if ((this instanceof DoubleNode) && (other instanceof LongNode)) {
                return compareLongDoubleNodes((LongNode) other, (DoubleNode) this) * -1;
            }
            return leafCompare((LeafNode) other);
        }
    }

    protected int leafCompare(LeafNode<?> other) {
        LeafType thisLeafType = getLeafType();
        LeafType otherLeafType = other.getLeafType();
        if (thisLeafType.equals(otherLeafType)) {
            return compareLeafValues(other);
        }
        return thisLeafType.compareTo(otherLeafType);
    }

    public String toString() {
        String str = getValue(true).toString();
        return str.length() <= 100 ? str : str.substring(0, 100) + "...";
    }
}
