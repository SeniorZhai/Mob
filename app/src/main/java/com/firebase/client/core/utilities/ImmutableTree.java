package com.firebase.client.core.utilities;

import com.firebase.client.collection.ImmutableSortedMap;
import com.firebase.client.collection.ImmutableSortedMap.Builder;
import com.firebase.client.collection.StandardComparator;
import com.firebase.client.core.Path;
import com.firebase.client.snapshot.ChildKey;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class ImmutableTree<T> implements Iterable<Entry<Path, T>> {
    private static final ImmutableTree EMPTY = new ImmutableTree(null, EMPTY_CHILDREN);
    private static final ImmutableSortedMap EMPTY_CHILDREN = Builder.emptyMap(StandardComparator.getComparator(ChildKey.class));
    private final ImmutableSortedMap<ChildKey, ImmutableTree<T>> children;
    private final T value;

    public interface TreeVisitor<T, R> {
        R onNodeValue(Path path, T t, R r);
    }

    public static <V> ImmutableTree<V> emptyInstance() {
        return EMPTY;
    }

    public ImmutableTree(T value, ImmutableSortedMap<ChildKey, ImmutableTree<T>> children) {
        this.value = value;
        this.children = children;
    }

    public ImmutableTree(T value) {
        this(value, EMPTY_CHILDREN);
    }

    public T getValue() {
        return this.value;
    }

    public ImmutableSortedMap<ChildKey, ImmutableTree<T>> getChildren() {
        return this.children;
    }

    public boolean isEmpty() {
        return this.value == null && this.children.isEmpty();
    }

    public Path findRootMostMatchingPath(Path relativePath, Predicate<? super T> predicate) {
        if (this.value != null && predicate.evaluate(this.value)) {
            return Path.getEmptyPath();
        }
        if (relativePath.isEmpty()) {
            return null;
        }
        ImmutableTree<T> child = (ImmutableTree) this.children.get(relativePath.getFront());
        if (child == null) {
            return null;
        }
        Path path = child.findRootMostMatchingPath(relativePath.popFront(), predicate);
        if (path == null) {
            return null;
        }
        return new Path(front).child(path);
    }

    public Path findRootMostPathWithValue(Path relativePath) {
        return findRootMostMatchingPath(relativePath, Predicate.TRUE);
    }

    public T rootMostValue(Path relativePath) {
        return rootMostValueMatching(relativePath, Predicate.TRUE);
    }

    public T rootMostValueMatching(Path relativePath, Predicate<? super T> predicate) {
        if (this.value != null && predicate.evaluate(this.value)) {
            return this.value;
        }
        ImmutableTree<T> currentTree = this;
        Iterator i$ = relativePath.iterator();
        while (i$.hasNext()) {
            currentTree = (ImmutableTree) currentTree.children.get((ChildKey) i$.next());
            if (currentTree == null) {
                return null;
            }
            if (currentTree.value != null && predicate.evaluate(currentTree.value)) {
                return currentTree.value;
            }
        }
        return null;
    }

    public T leafMostValue(Path relativePath) {
        return leafMostValueMatching(relativePath, Predicate.TRUE);
    }

    public T leafMostValueMatching(Path path, Predicate<? super T> predicate) {
        T currentValue = (this.value == null || !predicate.evaluate(this.value)) ? null : this.value;
        ImmutableTree<T> currentTree = this;
        Iterator i$ = path.iterator();
        while (i$.hasNext()) {
            currentTree = (ImmutableTree) currentTree.children.get((ChildKey) i$.next());
            if (currentTree == null) {
                break;
            } else if (currentTree.value != null && predicate.evaluate(currentTree.value)) {
                currentValue = currentTree.value;
            }
        }
        return currentValue;
    }

    public boolean containsMatchingValue(Predicate<? super T> predicate) {
        if (this.value != null && predicate.evaluate(this.value)) {
            return true;
        }
        Iterator i$ = this.children.iterator();
        while (i$.hasNext()) {
            if (((ImmutableTree) ((Entry) i$.next()).getValue()).containsMatchingValue(predicate)) {
                return true;
            }
        }
        return false;
    }

    public ImmutableTree<T> getChild(ChildKey child) {
        ImmutableTree<T> childTree = (ImmutableTree) this.children.get(child);
        return childTree != null ? childTree : emptyInstance();
    }

    public ImmutableTree<T> subtree(Path relativePath) {
        if (relativePath.isEmpty()) {
            return this;
        }
        ImmutableTree<T> childTree = (ImmutableTree) this.children.get(relativePath.getFront());
        if (childTree != null) {
            return childTree.subtree(relativePath.popFront());
        }
        return emptyInstance();
    }

    public ImmutableTree<T> set(Path relativePath, T value) {
        if (relativePath.isEmpty()) {
            return new ImmutableTree(value, this.children);
        }
        ChildKey front = relativePath.getFront();
        ImmutableTree<T> child = (ImmutableTree) this.children.get(front);
        if (child == null) {
            child = emptyInstance();
        }
        return new ImmutableTree(this.value, this.children.insert(front, child.set(relativePath.popFront(), value)));
    }

    public ImmutableTree<T> remove(Path relativePath) {
        if (!relativePath.isEmpty()) {
            ChildKey front = relativePath.getFront();
            ImmutableTree<T> child = (ImmutableTree) this.children.get(front);
            if (child == null) {
                return this;
            }
            ImmutableSortedMap<ChildKey, ImmutableTree<T>> newChildren;
            ImmutableTree<T> newChild = child.remove(relativePath.popFront());
            if (newChild.isEmpty()) {
                newChildren = this.children.remove(front);
            } else {
                newChildren = this.children.insert(front, newChild);
            }
            if (this.value == null && newChildren.isEmpty()) {
                return emptyInstance();
            }
            return new ImmutableTree(this.value, newChildren);
        } else if (this.children.isEmpty()) {
            return emptyInstance();
        } else {
            return new ImmutableTree(null, this.children);
        }
    }

    public T get(Path relativePath) {
        if (relativePath.isEmpty()) {
            return this.value;
        }
        ImmutableTree<T> child = (ImmutableTree) this.children.get(relativePath.getFront());
        if (child != null) {
            return child.get(relativePath.popFront());
        }
        return null;
    }

    public ImmutableTree<T> setTree(Path relativePath, ImmutableTree<T> newTree) {
        if (relativePath.isEmpty()) {
            return newTree;
        }
        ImmutableSortedMap<ChildKey, ImmutableTree<T>> newChildren;
        ChildKey front = relativePath.getFront();
        ImmutableTree<T> child = (ImmutableTree) this.children.get(front);
        if (child == null) {
            child = emptyInstance();
        }
        ImmutableTree<T> newChild = child.setTree(relativePath.popFront(), newTree);
        if (newChild.isEmpty()) {
            newChildren = this.children.remove(front);
        } else {
            newChildren = this.children.insert(front, newChild);
        }
        return new ImmutableTree(this.value, newChildren);
    }

    public void foreach(TreeVisitor<T, Void> visitor) {
        fold(Path.getEmptyPath(), visitor, null);
    }

    public <R> R fold(R accum, TreeVisitor<? super T, R> visitor) {
        return fold(Path.getEmptyPath(), visitor, accum);
    }

    private <R> R fold(Path relativePath, TreeVisitor<? super T, R> visitor, R accum) {
        Iterator i$ = this.children.iterator();
        while (i$.hasNext()) {
            Entry<ChildKey, ImmutableTree<T>> subtree = (Entry) i$.next();
            accum = ((ImmutableTree) subtree.getValue()).fold(relativePath.child((ChildKey) subtree.getKey()), visitor, accum);
        }
        if (this.value != null) {
            return visitor.onNodeValue(relativePath, this.value, accum);
        }
        return accum;
    }

    public Collection<T> values() {
        final ArrayList<T> list = new ArrayList();
        foreach(new TreeVisitor<T, Void>() {
            public Void onNodeValue(Path relativePath, T value, Void accum) {
                list.add(value);
                return null;
            }
        });
        return list;
    }

    public Iterator<Entry<Path, T>> iterator() {
        final List<Entry<Path, T>> list = new ArrayList();
        foreach(new TreeVisitor<T, Void>() {
            public Void onNodeValue(Path relativePath, T value, Void accum) {
                list.add(new SimpleImmutableEntry(relativePath, value));
                return null;
            }
        });
        return list.iterator();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ImmutableTree { value=");
        builder.append(getValue());
        builder.append(", children={");
        Iterator i$ = this.children.iterator();
        while (i$.hasNext()) {
            Entry<ChildKey, ImmutableTree<T>> child = (Entry) i$.next();
            builder.append(((ChildKey) child.getKey()).asString());
            builder.append("=");
            builder.append(child.getValue());
        }
        builder.append("} }");
        return builder.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImmutableTree that = (ImmutableTree) o;
        if (this.children == null ? that.children != null : !this.children.equals(that.children)) {
            return false;
        }
        if (this.value != null) {
            if (this.value.equals(that.value)) {
                return true;
            }
        } else if (that.value == null) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result;
        int i = 0;
        if (this.value != null) {
            result = this.value.hashCode();
        } else {
            result = 0;
        }
        int i2 = result * 31;
        if (this.children != null) {
            i = this.children.hashCode();
        }
        return i2 + i;
    }
}
