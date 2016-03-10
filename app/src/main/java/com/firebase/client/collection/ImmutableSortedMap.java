package com.firebase.client.collection;

import com.firebase.client.collection.LLRBNode.NodeVisitor;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class ImmutableSortedMap<K, V> implements Iterable<Entry<K, V>> {

    public static class Builder {
        static final int ARRAY_TO_RB_TREE_SIZE_THRESHOLD = 25;
        private static final KeyTranslator IDENTITY_TRANSLATOR = new KeyTranslator() {
            public Object translate(Object key) {
                return key;
            }
        };

        public interface KeyTranslator<C, D> {
            D translate(C c);
        }

        public static <K, V> ImmutableSortedMap<K, V> emptyMap(Comparator<K> comparator) {
            return new ArraySortedMap(comparator);
        }

        public static <A> KeyTranslator<A, A> identityTranslator() {
            return IDENTITY_TRANSLATOR;
        }

        public static <A, B> ImmutableSortedMap<A, B> fromMap(Map<A, B> values, Comparator<A> comparator) {
            if (values.size() < ARRAY_TO_RB_TREE_SIZE_THRESHOLD) {
                return ArraySortedMap.fromMap(values, comparator);
            }
            return RBTreeSortedMap.fromMap(values, comparator);
        }

        public static <A, B, C> ImmutableSortedMap<A, C> buildFrom(List<A> keys, Map<B, C> values, KeyTranslator<A, B> translator, Comparator<A> comparator) {
            if (keys.size() < ARRAY_TO_RB_TREE_SIZE_THRESHOLD) {
                return ArraySortedMap.buildFrom(keys, values, translator, comparator);
            }
            return RBTreeSortedMap.buildFrom(keys, values, translator, comparator);
        }
    }

    public abstract boolean containsKey(K k);

    public abstract V get(K k);

    public abstract Comparator<K> getComparator();

    public abstract K getMaxKey();

    public abstract K getMinKey();

    public abstract K getPredecessorKey(K k);

    public abstract K getSuccessorKey(K k);

    public abstract void inOrderTraversal(NodeVisitor<K, V> nodeVisitor);

    public abstract ImmutableSortedMap<K, V> insert(K k, V v);

    public abstract boolean isEmpty();

    public abstract Iterator<Entry<K, V>> iterator();

    public abstract Iterator<Entry<K, V>> iteratorFrom(K k);

    public abstract ImmutableSortedMap<K, V> remove(K k);

    public abstract Iterator<Entry<K, V>> reverseIterator();

    public abstract Iterator<Entry<K, V>> reverseIteratorFrom(K k);

    public abstract int size();

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImmutableSortedMap)) {
            return false;
        }
        ImmutableSortedMap<K, V> that = (ImmutableSortedMap) o;
        if (!getComparator().equals(that.getComparator())) {
            return false;
        }
        if (size() != that.size()) {
            return false;
        }
        Iterator<Entry<K, V>> thisIterator = iterator();
        Iterator<Entry<K, V>> thatIterator = that.iterator();
        while (thisIterator.hasNext()) {
            if (!((Entry) thisIterator.next()).equals(thatIterator.next())) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int result = getComparator().hashCode();
        Iterator i$ = iterator();
        while (i$.hasNext()) {
            result = (result * 31) + ((Entry) i$.next()).hashCode();
        }
        return result;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getSimpleName());
        b.append("{");
        boolean first = true;
        Iterator i$ = iterator();
        while (i$.hasNext()) {
            Entry<K, V> entry = (Entry) i$.next();
            if (first) {
                first = false;
            } else {
                b.append(", ");
            }
            b.append("(");
            b.append(entry.getKey());
            b.append("=>");
            b.append(entry.getValue());
            b.append(")");
        }
        b.append("};");
        return b.toString();
    }
}
