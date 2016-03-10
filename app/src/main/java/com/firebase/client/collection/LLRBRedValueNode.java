package com.firebase.client.collection;

import com.firebase.client.collection.LLRBNode.Color;

public class LLRBRedValueNode<K, V> extends LLRBValueNode<K, V> {
    LLRBRedValueNode(K key, V value) {
        super(key, value, LLRBEmptyNode.getInstance(), LLRBEmptyNode.getInstance());
    }

    LLRBRedValueNode(K key, V value, LLRBNode<K, V> left, LLRBNode<K, V> right) {
        super(key, value, left, right);
    }

    protected Color getColor() {
        return Color.RED;
    }

    public boolean isRed() {
        return true;
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
        return new LLRBRedValueNode(newKey, newValue, newLeft, newRight);
    }
}
