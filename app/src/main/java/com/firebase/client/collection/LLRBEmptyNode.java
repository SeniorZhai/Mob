package com.firebase.client.collection;

import com.firebase.client.collection.LLRBNode.Color;
import com.firebase.client.collection.LLRBNode.NodeVisitor;
import com.firebase.client.collection.LLRBNode.ShortCircuitingNodeVisitor;
import java.util.Comparator;

public class LLRBEmptyNode<K, V> implements LLRBNode<K, V> {
    private static final LLRBEmptyNode INSTANCE = new LLRBEmptyNode();

    public static <K, V> LLRBEmptyNode<K, V> getInstance() {
        return INSTANCE;
    }

    private LLRBEmptyNode() {
    }

    public LLRBNode<K, V> copy(K k, V v, Color color, LLRBNode<K, V> lLRBNode, LLRBNode<K, V> lLRBNode2) {
        return this;
    }

    public LLRBNode<K, V> insert(K key, V value, Comparator<K> comparator) {
        return new LLRBRedValueNode(key, value);
    }

    public LLRBNode<K, V> remove(K k, Comparator<K> comparator) {
        return this;
    }

    public boolean isEmpty() {
        return true;
    }

    public boolean isRed() {
        return false;
    }

    public K getKey() {
        return null;
    }

    public V getValue() {
        return null;
    }

    public LLRBNode<K, V> getLeft() {
        return this;
    }

    public LLRBNode<K, V> getRight() {
        return this;
    }

    public LLRBNode<K, V> getMin() {
        return this;
    }

    public LLRBNode<K, V> getMax() {
        return this;
    }

    public int count() {
        return 0;
    }

    public void inOrderTraversal(NodeVisitor<K, V> nodeVisitor) {
    }

    public boolean shortCircuitingInOrderTraversal(ShortCircuitingNodeVisitor<K, V> shortCircuitingNodeVisitor) {
        return true;
    }

    public boolean shortCircuitingReverseOrderTraversal(ShortCircuitingNodeVisitor<K, V> shortCircuitingNodeVisitor) {
        return true;
    }
}
