package com.firebase.client.snapshot;

import com.firebase.client.core.Path;
import io.fabric.sdk.android.BuildConfig;
import java.util.Collections;
import java.util.Iterator;

public class EmptyNode extends ChildrenNode implements Node {
    private static final EmptyNode empty = new EmptyNode();

    private EmptyNode() {
    }

    public static EmptyNode Empty() {
        return empty;
    }

    public boolean isLeafNode() {
        return false;
    }

    public Node getPriority() {
        return this;
    }

    public Node getChild(Path path) {
        return this;
    }

    public Node getImmediateChild(ChildKey name) {
        return this;
    }

    public Node updateImmediateChild(ChildKey name, Node node) {
        return (node.isEmpty() || name.isPriorityChildName()) ? this : new ChildrenNode().updateImmediateChild(name, node);
    }

    public Node updateChild(Path path, Node node) {
        if (path.isEmpty()) {
            return node;
        }
        ChildKey name = path.getFront();
        return updateImmediateChild(name, getImmediateChild(name).updateChild(path.popFront(), node));
    }

    public EmptyNode updatePriority(Node priority) {
        return this;
    }

    public boolean hasChild(ChildKey name) {
        return false;
    }

    public boolean isEmpty() {
        return true;
    }

    public int getChildCount() {
        return 0;
    }

    public Object getValue() {
        return null;
    }

    public Object getValue(boolean useExportFormat) {
        return null;
    }

    public ChildKey getPredecessorChildKey(ChildKey childKey) {
        return null;
    }

    public ChildKey getSuccessorChildKey(ChildKey childKey) {
        return null;
    }

    public String getHash() {
        return BuildConfig.FLAVOR;
    }

    public String getHashString() {
        return BuildConfig.FLAVOR;
    }

    public Iterator<NamedNode> iterator() {
        return Collections.emptyList().iterator();
    }

    public Iterator<NamedNode> reverseIterator() {
        return Collections.emptyList().iterator();
    }

    public int compareTo(Node o) {
        return o.isEmpty() ? 0 : -1;
    }

    public boolean equals(Object o) {
        if (o instanceof EmptyNode) {
            return true;
        }
        boolean z = (o instanceof Node) && ((Node) o).isEmpty() && getPriority().equals(((Node) o).getPriority());
        return z;
    }

    public int hashCode() {
        return 0;
    }

    public String toString() {
        return "<Empty Node>";
    }
}
