package com.firebase.client.collection;

import com.firebase.client.collection.ImmutableSortedMap.Builder.KeyTranslator;
import com.firebase.client.collection.LLRBNode.Color;
import com.firebase.client.collection.LLRBNode.NodeVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RBTreeSortedMap<K, V> extends ImmutableSortedMap<K, V> {
    private Comparator<K> comparator;
    private LLRBNode<K, V> root;

    private static class Builder<A, B, C> {
        private final KeyTranslator<A, B> keyTranslator;
        private final List<A> keys;
        private LLRBValueNode<A, C> leaf;
        private LLRBValueNode<A, C> root;
        private final Map<B, C> values;

        static class Base1_2 implements Iterable<BooleanChunk> {
            private final int length;
            private long value;

            public Base1_2(int size) {
                int toCalc = size + 1;
                this.length = (int) Math.floor(Math.log((double) toCalc) / Math.log(2.0d));
                this.value = ((long) toCalc) & (((long) Math.pow(2.0d, (double) this.length)) - 1);
            }

            public Iterator<BooleanChunk> iterator() {
                return new Iterator<BooleanChunk>() {
                    private int current = (Base1_2.this.length - 1);

                    public boolean hasNext() {
                        return this.current >= 0;
                    }

                    public BooleanChunk next() {
                        boolean z = true;
                        long result = Base1_2.this.value & ((long) (1 << this.current));
                        BooleanChunk next = new BooleanChunk();
                        if (result != 0) {
                            z = false;
                        }
                        next.isOne = z;
                        next.chunkSize = (int) Math.pow(2.0d, (double) this.current);
                        this.current--;
                        return next;
                    }

                    public void remove() {
                    }
                };
            }
        }

        static class BooleanChunk {
            public int chunkSize;
            public boolean isOne;

            BooleanChunk() {
            }
        }

        private Builder(List<A> keys, Map<B, C> values, KeyTranslator<A, B> translator) {
            this.keys = keys;
            this.values = values;
            this.keyTranslator = translator;
        }

        private C getValue(A key) {
            return this.values.get(this.keyTranslator.translate(key));
        }

        private LLRBNode<A, C> buildBalancedTree(int start, int size) {
            if (size == 0) {
                return LLRBEmptyNode.getInstance();
            }
            if (size == 1) {
                A key = this.keys.get(start);
                return new LLRBBlackValueNode(key, getValue(key), null, null);
            }
            int half = size / 2;
            int middle = start + half;
            LLRBNode<A, C> left = buildBalancedTree(start, half);
            LLRBNode<A, C> right = buildBalancedTree(middle + 1, half);
            key = this.keys.get(middle);
            return new LLRBBlackValueNode(key, getValue(key), left, right);
        }

        private void buildPennant(Color color, int chunkSize, int start) {
            LLRBValueNode<A, C> node;
            LLRBNode<A, C> treeRoot = buildBalancedTree(start + 1, chunkSize - 1);
            A key = this.keys.get(start);
            if (color == Color.RED) {
                node = new LLRBRedValueNode(key, getValue(key), null, treeRoot);
            } else {
                node = new LLRBBlackValueNode(key, getValue(key), null, treeRoot);
            }
            if (this.root == null) {
                this.root = node;
                this.leaf = node;
                return;
            }
            this.leaf.setLeft(node);
            this.leaf = node;
        }

        public static <A, B, C> RBTreeSortedMap<A, C> buildFrom(List<A> keys, Map<B, C> values, KeyTranslator<A, B> translator, Comparator<A> comparator) {
            Builder<A, B, C> builder = new Builder(keys, values, translator);
            Collections.sort(keys, comparator);
            Iterator<BooleanChunk> iter = new Base1_2(keys.size()).iterator();
            int index = keys.size();
            while (iter.hasNext()) {
                BooleanChunk next = (BooleanChunk) iter.next();
                index -= next.chunkSize;
                if (next.isOne) {
                    builder.buildPennant(Color.BLACK, next.chunkSize, index);
                } else {
                    builder.buildPennant(Color.BLACK, next.chunkSize, index);
                    index -= next.chunkSize;
                    builder.buildPennant(Color.RED, next.chunkSize, index);
                }
            }
            return new RBTreeSortedMap(builder.root == null ? LLRBEmptyNode.getInstance() : builder.root, comparator);
        }
    }

    RBTreeSortedMap(Comparator<K> comparator) {
        this.root = LLRBEmptyNode.getInstance();
        this.comparator = comparator;
    }

    private RBTreeSortedMap(LLRBNode<K, V> root, Comparator<K> comparator) {
        this.root = root;
        this.comparator = comparator;
    }

    LLRBNode<K, V> getRoot() {
        return this.root;
    }

    private LLRBNode<K, V> getNode(K key) {
        LLRBNode<K, V> node = this.root;
        while (!node.isEmpty()) {
            int cmp = this.comparator.compare(key, node.getKey());
            if (cmp < 0) {
                node = node.getLeft();
            } else if (cmp == 0) {
                return node;
            } else {
                node = node.getRight();
            }
        }
        return null;
    }

    public boolean containsKey(K key) {
        return getNode(key) != null;
    }

    public V get(K key) {
        LLRBNode<K, V> node = getNode(key);
        return node != null ? node.getValue() : null;
    }

    public ImmutableSortedMap<K, V> remove(K key) {
        if (!containsKey(key)) {
            return this;
        }
        return new RBTreeSortedMap(this.root.remove(key, this.comparator).copy(null, null, Color.BLACK, null, null), this.comparator);
    }

    public ImmutableSortedMap<K, V> insert(K key, V value) {
        return new RBTreeSortedMap(this.root.insert(key, value, this.comparator).copy(null, null, Color.BLACK, null, null), this.comparator);
    }

    public K getMinKey() {
        return this.root.getMin().getKey();
    }

    public K getMaxKey() {
        return this.root.getMax().getKey();
    }

    public int size() {
        return this.root.count();
    }

    public boolean isEmpty() {
        return this.root.isEmpty();
    }

    public void inOrderTraversal(NodeVisitor<K, V> visitor) {
        this.root.inOrderTraversal(visitor);
    }

    public Iterator<Entry<K, V>> iterator() {
        return new ImmutableSortedMapIterator(this.root, null, this.comparator, false);
    }

    public Iterator<Entry<K, V>> iteratorFrom(K key) {
        return new ImmutableSortedMapIterator(this.root, key, this.comparator, false);
    }

    public Iterator<Entry<K, V>> reverseIteratorFrom(K key) {
        return new ImmutableSortedMapIterator(this.root, key, this.comparator, true);
    }

    public Iterator<Entry<K, V>> reverseIterator() {
        return new ImmutableSortedMapIterator(this.root, null, this.comparator, true);
    }

    public K getPredecessorKey(K key) {
        LLRBNode<K, V> node = this.root;
        LLRBNode<K, V> rightParent = null;
        while (!node.isEmpty()) {
            int cmp = this.comparator.compare(key, node.getKey());
            if (cmp == 0) {
                if (!node.getLeft().isEmpty()) {
                    node = node.getLeft();
                    while (!node.getRight().isEmpty()) {
                        node = node.getRight();
                    }
                    return node.getKey();
                } else if (rightParent != null) {
                    return rightParent.getKey();
                } else {
                    return null;
                }
            } else if (cmp < 0) {
                node = node.getLeft();
            } else {
                rightParent = node;
                node = node.getRight();
            }
        }
        throw new IllegalArgumentException("Couldn't find predecessor key of non-present key: " + key);
    }

    public K getSuccessorKey(K key) {
        LLRBNode<K, V> node = this.root;
        LLRBNode<K, V> leftParent = null;
        while (!node.isEmpty()) {
            int cmp = this.comparator.compare(node.getKey(), key);
            if (cmp == 0) {
                if (!node.getRight().isEmpty()) {
                    node = node.getRight();
                    while (!node.getLeft().isEmpty()) {
                        node = node.getLeft();
                    }
                    return node.getKey();
                } else if (leftParent != null) {
                    return leftParent.getKey();
                } else {
                    return null;
                }
            } else if (cmp < 0) {
                node = node.getRight();
            } else {
                leftParent = node;
                node = node.getLeft();
            }
        }
        throw new IllegalArgumentException("Couldn't find successor key of non-present key: " + key);
    }

    public Comparator<K> getComparator() {
        return this.comparator;
    }

    public static <A, B, C> RBTreeSortedMap<A, C> buildFrom(List<A> keys, Map<B, C> values, KeyTranslator<A, B> translator, Comparator<A> comparator) {
        return Builder.buildFrom(keys, values, translator, comparator);
    }

    public static <A, B> RBTreeSortedMap<A, B> fromMap(Map<A, B> values, Comparator<A> comparator) {
        return Builder.buildFrom(new ArrayList(values.keySet()), values, com.firebase.client.collection.ImmutableSortedMap.Builder.identityTranslator(), comparator);
    }
}
