package com.firebase.client.collection;

import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Stack;

public class ImmutableSortedMapIterator<K, V> implements Iterator<Entry<K, V>> {
    private final boolean isReverse;
    private final Stack<LLRBValueNode<K, V>> nodeStack = new Stack();

    ImmutableSortedMapIterator(LLRBNode<K, V> root, K startKey, Comparator<K> comparator, boolean isReverse) {
        this.isReverse = isReverse;
        LLRBNode<K, V> node = root;
        while (!node.isEmpty()) {
            int cmp = startKey != null ? isReverse ? comparator.compare(startKey, node.getKey()) : comparator.compare(node.getKey(), startKey) : 1;
            if (cmp < 0) {
                if (isReverse) {
                    node = node.getLeft();
                } else {
                    node = node.getRight();
                }
            } else if (cmp == 0) {
                this.nodeStack.push((LLRBValueNode) node);
                return;
            } else {
                this.nodeStack.push((LLRBValueNode) node);
                if (isReverse) {
                    node = node.getRight();
                } else {
                    node = node.getLeft();
                }
            }
        }
    }

    public boolean hasNext() {
        return this.nodeStack.size() > 0;
    }

    public Entry<K, V> next() {
        try {
            LLRBValueNode<K, V> node = (LLRBValueNode) this.nodeStack.pop();
            Entry<K, V> entry = new SimpleEntry(node.getKey(), node.getValue());
            LLRBNode<K, V> next;
            if (this.isReverse) {
                for (next = node.getLeft(); !next.isEmpty(); next = next.getRight()) {
                    this.nodeStack.push((LLRBValueNode) next);
                }
            } else {
                for (next = node.getRight(); !next.isEmpty(); next = next.getLeft()) {
                    this.nodeStack.push((LLRBValueNode) next);
                }
            }
            return entry;
        } catch (EmptyStackException e) {
            throw new NoSuchElementException();
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("remove called on immutable collection");
    }
}
