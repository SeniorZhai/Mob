package com.firebase.client.collection;

import com.firebase.client.collection.LLRBNode.Color;

public class LLRBBlackValueNode<K, V> extends LLRBValueNode<K, V> {
    LLRBBlackValueNode(K key, V value, LLRBNode<K, V> left, LLRBNode<K, V> right) {
        super(key, value, left, right);
    }

    protected Color getColor() {
        return Color.BLACK;
    }

    public boolean isRed() {
        return false;
    }

    protected LLRBValueNode<K, V> copy(K key, V value, LLRBNode<K, V> left, LLRBNode<K, V> right) {
        K newKey;
        V newValue;
        LLRBNode<K, V> newLeft;
        LLRBNode<K, V> newRight;
        if (key == null) {
            newKey = getKey();
        } else {
            newKey = key;
        }
        if (value == null) {
            newValue = getValue();
        } else {
            newValue = value;
        }
        if (left == null) {
            newLeft = getLeft();
        } else {
            newLeft = left;
        }
        if (right == null) {
            newRight = getRight();
        } else {
            newRight = right;
        }
        return new LLRBBlackValueNode(newKey, newValue, newLeft, newRight);
    }
}
