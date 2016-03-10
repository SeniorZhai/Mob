package com.firebase.client.core.persistence;

import com.firebase.client.collection.ImmutableSortedMap;
import com.firebase.client.core.Path;
import com.firebase.client.core.utilities.ImmutableTree;
import com.firebase.client.core.utilities.ImmutableTree.TreeVisitor;
import com.firebase.client.core.utilities.Predicate;
import com.firebase.client.snapshot.ChildKey;
import java.util.Set;

public class PruneForest {
    private static final Predicate<Boolean> KEEP_PREDICATE = new Predicate<Boolean>() {
        public boolean evaluate(Boolean prune) {
            return !prune.booleanValue();
        }
    };
    private static final ImmutableTree<Boolean> KEEP_TREE = new ImmutableTree(Boolean.valueOf(false));
    private static final Predicate<Boolean> PRUNE_PREDICATE = new Predicate<Boolean>() {
        public boolean evaluate(Boolean prune) {
            return prune.booleanValue();
        }
    };
    private static final ImmutableTree<Boolean> PRUNE_TREE = new ImmutableTree(Boolean.valueOf(true));
    private final ImmutableTree<Boolean> pruneForest;

    public PruneForest() {
        this.pruneForest = ImmutableTree.emptyInstance();
    }

    private PruneForest(ImmutableTree<Boolean> pruneForest) {
        this.pruneForest = pruneForest;
    }

    public boolean prunesAnything() {
        return this.pruneForest.containsMatchingValue(PRUNE_PREDICATE);
    }

    public boolean shouldPruneUnkeptDescendants(Path path) {
        Boolean shouldPrune = (Boolean) this.pruneForest.leafMostValue(path);
        return shouldPrune != null && shouldPrune.booleanValue();
    }

    public boolean shouldKeep(Path path) {
        Boolean shouldPrune = (Boolean) this.pruneForest.leafMostValue(path);
        return (shouldPrune == null || shouldPrune.booleanValue()) ? false : true;
    }

    public boolean affectsPath(Path path) {
        return (this.pruneForest.rootMostValue(path) == null && this.pruneForest.subtree(path).isEmpty()) ? false : true;
    }

    public PruneForest child(ChildKey key) {
        ImmutableTree<Boolean> childPruneTree = this.pruneForest.getChild(key);
        if (childPruneTree == null) {
            childPruneTree = new ImmutableTree(this.pruneForest.getValue());
        } else if (childPruneTree.getValue() == null && this.pruneForest.getValue() != null) {
            childPruneTree = childPruneTree.set(Path.getEmptyPath(), this.pruneForest.getValue());
        }
        return new PruneForest(childPruneTree);
    }

    public PruneForest child(Path path) {
        return path.isEmpty() ? this : child(path.getFront()).child(path.popFront());
    }

    public <T> T foldKeptNodes(T startValue, final TreeVisitor<Void, T> treeVisitor) {
        return this.pruneForest.fold(startValue, new TreeVisitor<Boolean, T>() {
            public T onNodeValue(Path relativePath, Boolean prune, T accum) {
                if (prune.booleanValue()) {
                    return accum;
                }
                return treeVisitor.onNodeValue(relativePath, null, accum);
            }
        });
    }

    public PruneForest prune(Path path) {
        if (this.pruneForest.rootMostValueMatching(path, KEEP_PREDICATE) != null) {
            throw new IllegalArgumentException("Can't prune path that was kept previously!");
        } else if (this.pruneForest.rootMostValueMatching(path, PRUNE_PREDICATE) != null) {
            return this;
        } else {
            this(this.pruneForest.setTree(path, PRUNE_TREE));
            return this;
        }
    }

    public PruneForest keep(Path path) {
        if (this.pruneForest.rootMostValueMatching(path, KEEP_PREDICATE) != null) {
            return this;
        }
        this(this.pruneForest.setTree(path, KEEP_TREE));
        return this;
    }

    public PruneForest keepAll(Path path, Set<ChildKey> children) {
        return this.pruneForest.rootMostValueMatching(path, KEEP_PREDICATE) != null ? this : doAll(path, children, KEEP_TREE);
    }

    public PruneForest pruneAll(Path path, Set<ChildKey> children) {
        if (this.pruneForest.rootMostValueMatching(path, KEEP_PREDICATE) == null) {
            return this.pruneForest.rootMostValueMatching(path, PRUNE_PREDICATE) != null ? this : doAll(path, children, PRUNE_TREE);
        } else {
            throw new IllegalArgumentException("Can't prune path that was kept previously!");
        }
    }

    private PruneForest doAll(Path path, Set<ChildKey> children, ImmutableTree<Boolean> keepOrPruneTree) {
        ImmutableTree<Boolean> subtree = this.pruneForest.subtree(path);
        ImmutableSortedMap<ChildKey, ImmutableTree<Boolean>> childrenMap = subtree.getChildren();
        for (ChildKey key : children) {
            childrenMap = childrenMap.insert(key, keepOrPruneTree);
        }
        return new PruneForest(this.pruneForest.setTree(path, new ImmutableTree(subtree.getValue(), childrenMap)));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PruneForest)) {
            return false;
        }
        if (this.pruneForest.equals(((PruneForest) o).pruneForest)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.pruneForest.hashCode();
    }

    public String toString() {
        return "{PruneForest:" + this.pruneForest.toString() + "}";
    }
}
