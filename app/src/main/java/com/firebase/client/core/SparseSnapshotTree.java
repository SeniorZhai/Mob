package com.firebase.client.core;

import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.ChildrenNode;
import com.firebase.client.snapshot.ChildrenNode.ChildVisitor;
import com.firebase.client.snapshot.Node;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class SparseSnapshotTree {
    private Map<ChildKey, SparseSnapshotTree> children = null;
    private Node value = null;

    public interface SparseSnapshotTreeVisitor {
        void visitTree(Path path, Node node);
    }

    public interface SparseSnapshotChildVisitor {
        void visitChild(ChildKey childKey, SparseSnapshotTree sparseSnapshotTree);
    }

    public void remember(Path path, Node data) {
        if (path.isEmpty()) {
            this.value = data;
            this.children = null;
        } else if (this.value != null) {
            this.value = this.value.updateChild(path, data);
        } else {
            if (this.children == null) {
                this.children = new HashMap();
            }
            ChildKey childKey = path.getFront();
            if (!this.children.containsKey(childKey)) {
                this.children.put(childKey, new SparseSnapshotTree());
            }
            ((SparseSnapshotTree) this.children.get(childKey)).remember(path.popFront(), data);
        }
    }

    public boolean forget(final Path path) {
        if (path.isEmpty()) {
            this.value = null;
            this.children = null;
            return true;
        } else if (this.value != null) {
            if (this.value.isLeafNode()) {
                return false;
            }
            ChildrenNode childrenNode = this.value;
            this.value = null;
            childrenNode.forEachChild(new ChildVisitor() {
                public void visitChild(ChildKey name, Node child) {
                    SparseSnapshotTree.this.remember(path.child(name), child);
                }
            });
            return forget(path);
        } else if (this.children == null) {
            return true;
        } else {
            ChildKey childKey = path.getFront();
            Path childPath = path.popFront();
            if (this.children.containsKey(childKey) && ((SparseSnapshotTree) this.children.get(childKey)).forget(childPath)) {
                this.children.remove(childKey);
            }
            if (!this.children.isEmpty()) {
                return false;
            }
            this.children = null;
            return true;
        }
    }

    public void forEachTree(final Path prefixPath, final SparseSnapshotTreeVisitor visitor) {
        if (this.value != null) {
            visitor.visitTree(prefixPath, this.value);
        } else {
            forEachChild(new SparseSnapshotChildVisitor() {
                public void visitChild(ChildKey key, SparseSnapshotTree tree) {
                    tree.forEachTree(prefixPath.child(key), visitor);
                }
            });
        }
    }

    public void forEachChild(SparseSnapshotChildVisitor visitor) {
        if (this.children != null) {
            for (Entry<ChildKey, SparseSnapshotTree> entry : this.children.entrySet()) {
                visitor.visitChild((ChildKey) entry.getKey(), (SparseSnapshotTree) entry.getValue());
            }
        }
    }
}
