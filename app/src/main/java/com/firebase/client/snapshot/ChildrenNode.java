package com.firebase.client.snapshot;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.firebase.client.collection.ImmutableSortedMap;
import com.firebase.client.collection.ImmutableSortedMap.Builder;
import com.firebase.client.collection.LLRBNode.NodeVisitor;
import com.firebase.client.core.Path;
import com.firebase.client.utilities.Utilities;
import io.fabric.sdk.android.BuildConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ChildrenNode implements Node {
    static final /* synthetic */ boolean $assertionsDisabled = (!ChildrenNode.class.desiredAssertionStatus());
    public static Comparator<ChildKey> NAME_ONLY_COMPARATOR = new Comparator<ChildKey>() {
        public int compare(ChildKey o1, ChildKey o2) {
            return o1.compareTo(o2);
        }
    };
    private final ImmutableSortedMap<ChildKey, Node> children;
    private String lazyHash;
    private final Node priority;

    public static abstract class ChildVisitor extends NodeVisitor<ChildKey, Node> {
        public abstract void visitChild(ChildKey childKey, Node node);

        public void visitEntry(ChildKey key, Node value) {
            visitChild(key, value);
        }
    }

    private static class NamedNodeIterator implements Iterator<NamedNode> {
        private final Iterator<Entry<ChildKey, Node>> iterator;

        public NamedNodeIterator(Iterator<Entry<ChildKey, Node>> iterator) {
            this.iterator = iterator;
        }

        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        public NamedNode next() {
            Entry<ChildKey, Node> entry = (Entry) this.iterator.next();
            return new NamedNode((ChildKey) entry.getKey(), (Node) entry.getValue());
        }

        public void remove() {
            this.iterator.remove();
        }
    }

    protected ChildrenNode() {
        this.lazyHash = null;
        this.children = Builder.emptyMap(NAME_ONLY_COMPARATOR);
        this.priority = PriorityUtilities.NullPriority();
    }

    protected ChildrenNode(ImmutableSortedMap<ChildKey, Node> children, Node priority) {
        this.lazyHash = null;
        if (!children.isEmpty() || priority.isEmpty()) {
            this.priority = priority;
            this.children = children;
            return;
        }
        throw new IllegalArgumentException("Can't create empty ChildrenNode with priority!");
    }

    public boolean hasChild(ChildKey name) {
        return !getImmediateChild(name).isEmpty();
    }

    public boolean isEmpty() {
        return this.children.isEmpty();
    }

    public int getChildCount() {
        return this.children.size();
    }

    public Object getValue() {
        return getValue(false);
    }

    public Object getValue(boolean useExportFormat) {
        if (isEmpty()) {
            return null;
        }
        int numKeys = 0;
        int maxKey = 0;
        boolean allIntegerKeys = true;
        Map<String, Object> result = new HashMap();
        Iterator i$ = this.children.iterator();
        while (i$.hasNext()) {
            Entry<ChildKey, Node> entry = (Entry) i$.next();
            String key = ((ChildKey) entry.getKey()).asString();
            result.put(key, ((Node) entry.getValue()).getValue(useExportFormat));
            numKeys++;
            if (allIntegerKeys) {
                if (key.length() <= 1 || key.charAt(0) != '0') {
                    Integer keyAsInt = Utilities.tryParseInt(key);
                    if (keyAsInt == null || keyAsInt.intValue() < 0) {
                        allIntegerKeys = false;
                    } else if (keyAsInt.intValue() > maxKey) {
                        maxKey = keyAsInt.intValue();
                    }
                } else {
                    allIntegerKeys = false;
                }
            }
        }
        if (useExportFormat || !allIntegerKeys || maxKey >= numKeys * 2) {
            if (useExportFormat && !this.priority.isEmpty()) {
                result.put(".priority", this.priority.getValue());
            }
            return result;
        }
        Object arrayResult = new ArrayList(maxKey + 1);
        for (int i = 0; i <= maxKey; i++) {
            arrayResult.add(result.get(BuildConfig.FLAVOR + i));
        }
        return arrayResult;
    }

    public ChildKey getPredecessorChildKey(ChildKey childKey) {
        return (ChildKey) this.children.getPredecessorKey(childKey);
    }

    public ChildKey getSuccessorChildKey(ChildKey childKey) {
        return (ChildKey) this.children.getSuccessorKey(childKey);
    }

    public String getHashString() {
        StringBuilder toHash = new StringBuilder(this.priority.isEmpty() ? BuildConfig.FLAVOR : "priority:" + this.priority.getHashString() + ":");
        List<NamedNode> nodes = new ArrayList();
        boolean sawPriority = false;
        Iterator i$ = iterator();
        while (i$.hasNext()) {
            NamedNode node = (NamedNode) i$.next();
            nodes.add(node);
            sawPriority = sawPriority || !node.getNode().getPriority().isEmpty();
        }
        if (sawPriority) {
            Collections.sort(nodes, PriorityIndex.getInstance());
        }
        for (NamedNode node2 : nodes) {
            String hashString = node2.getNode().getHash();
            if (!hashString.equals(BuildConfig.FLAVOR)) {
                toHash.append(":");
                toHash.append(node2.getName().asString());
                toHash.append(":");
                toHash.append(hashString);
            }
        }
        return toHash.toString();
    }

    public String getHash() {
        if (this.lazyHash == null) {
            String hashString = getHashString();
            this.lazyHash = hashString.isEmpty() ? BuildConfig.FLAVOR : Utilities.sha1HexDigest(hashString);
        }
        return this.lazyHash;
    }

    public boolean isLeafNode() {
        return false;
    }

    public Node getPriority() {
        return this.priority;
    }

    public Node updatePriority(Node priority) {
        if (this.children.isEmpty()) {
            return EmptyNode.Empty();
        }
        return new ChildrenNode(this.children, priority);
    }

    public Node getImmediateChild(ChildKey name) {
        if (name.isPriorityChildName() && !this.priority.isEmpty()) {
            return this.priority;
        }
        if (this.children.containsKey(name)) {
            return (Node) this.children.get(name);
        }
        return EmptyNode.Empty();
    }

    public Node getChild(Path path) {
        ChildKey front = path.getFront();
        return front == null ? this : getImmediateChild(front).getChild(path.popFront());
    }

    public void forEachChild(ChildVisitor visitor) {
        this.children.inOrderTraversal(visitor);
    }

    public ChildKey getFirstChildKey() {
        return (ChildKey) this.children.getMinKey();
    }

    public ChildKey getLastChildKey() {
        return (ChildKey) this.children.getMaxKey();
    }

    public Node updateChild(Path path, Node newChildNode) {
        ChildKey front = path.getFront();
        if (front == null) {
            return newChildNode;
        }
        if (!front.isPriorityChildName()) {
            return updateImmediateChild(front, getImmediateChild(front).updateChild(path.popFront(), newChildNode));
        }
        if ($assertionsDisabled || PriorityUtilities.isValidPriority(newChildNode)) {
            return updatePriority(newChildNode);
        }
        throw new AssertionError();
    }

    public Iterator<NamedNode> iterator() {
        return new NamedNodeIterator(this.children.iterator());
    }

    public Iterator<NamedNode> reverseIterator() {
        return new NamedNodeIterator(this.children.reverseIterator());
    }

    public Node updateImmediateChild(ChildKey key, Node newChildNode) {
        if (key.isPriorityChildName()) {
            return updatePriority(newChildNode);
        }
        ImmutableSortedMap<ChildKey, Node> newChildren = this.children;
        if (newChildren.containsKey(key)) {
            newChildren = newChildren.remove(key);
        }
        if (!newChildNode.isEmpty()) {
            newChildren = newChildren.insert(key, newChildNode);
        }
        if (newChildren.isEmpty()) {
            return EmptyNode.Empty();
        }
        return new ChildrenNode(newChildren, this.priority);
    }

    public int compareTo(Node o) {
        if (isEmpty()) {
            if (o.isEmpty()) {
                return 0;
            }
            return -1;
        } else if (o.isLeafNode()) {
            return 1;
        } else {
            if (o.isEmpty()) {
                return 1;
            }
            if (o == Node.MAX_NODE) {
                return -1;
            }
            return 0;
        }
    }

    public boolean equals(Object otherObj) {
        if (otherObj == null) {
            return false;
        }
        if (otherObj == this) {
            return true;
        }
        if (!(otherObj instanceof ChildrenNode)) {
            return false;
        }
        ChildrenNode other = (ChildrenNode) otherObj;
        if (!getPriority().equals(other.getPriority())) {
            return false;
        }
        if (this.children.size() != other.children.size()) {
            return false;
        }
        Iterator<Entry<ChildKey, Node>> thisIterator = this.children.iterator();
        Iterator<Entry<ChildKey, Node>> otherIterator = other.children.iterator();
        while (thisIterator.hasNext() && otherIterator.hasNext()) {
            Entry<ChildKey, Node> thisNameNode = (Entry) thisIterator.next();
            Entry<ChildKey, Node> otherNamedNode = (Entry) otherIterator.next();
            if (((ChildKey) thisNameNode.getKey()).equals(otherNamedNode.getKey())) {
                if (!((Node) thisNameNode.getValue()).equals(otherNamedNode.getValue())) {
                }
            }
            return false;
        }
        if (!thisIterator.hasNext() && !otherIterator.hasNext()) {
            return true;
        }
        throw new IllegalStateException("Something went wrong internally.");
    }

    public int hashCode() {
        int hashCode = 0;
        Iterator i$ = iterator();
        while (i$.hasNext()) {
            NamedNode entry = (NamedNode) i$.next();
            hashCode = (((hashCode * 31) + entry.getName().hashCode()) * 17) + entry.getNode().hashCode();
        }
        return hashCode;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder, 0);
        return builder.toString();
    }

    private static void addIndentation(StringBuilder builder, int indentation) {
        for (int i = 0; i < indentation; i++) {
            builder.append(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
        }
    }

    private void toString(StringBuilder builder, int indentation) {
        if (this.children.isEmpty() && this.priority.isEmpty()) {
            builder.append("{ }");
            return;
        }
        builder.append("{\n");
        Iterator i$ = this.children.iterator();
        while (i$.hasNext()) {
            Entry<ChildKey, Node> childEntry = (Entry) i$.next();
            addIndentation(builder, indentation + 2);
            builder.append(((ChildKey) childEntry.getKey()).asString());
            builder.append("=");
            if (childEntry.getValue() instanceof ChildrenNode) {
                ((ChildrenNode) childEntry.getValue()).toString(builder, indentation + 2);
            } else {
                builder.append(((Node) childEntry.getValue()).toString());
            }
            builder.append("\n");
        }
        if (!this.priority.isEmpty()) {
            addIndentation(builder, indentation + 2);
            builder.append(".priority=");
            builder.append(this.priority.toString());
            builder.append("\n");
        }
        addIndentation(builder, indentation);
        builder.append("}");
    }
}
