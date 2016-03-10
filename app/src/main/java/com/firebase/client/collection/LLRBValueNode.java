package com.firebase.client.collection;

import com.firebase.client.collection.LLRBNode.Color;
import com.firebase.client.collection.LLRBNode.NodeVisitor;
import com.firebase.client.collection.LLRBNode.ShortCircuitingNodeVisitor;
import java.util.Comparator;

public abstract class LLRBValueNode<K, V> implements LLRBNode<K, V> {
    private final K key;
    private LLRBNode<K, V> left;
    private final LLRBNode<K, V> right;
    private final V value;

    protected abstract LLRBValueNode<K, V> copy(K k, V v, LLRBNode<K, V> lLRBNode, LLRBNode<K, V> lLRBNode2);

    protected abstract Color getColor();

    private static Color oppositeColor(LLRBNode node) {
        return node.isRed() ? Color.BLACK : Color.RED;
    }

    LLRBValueNode(K key, V value, LLRBNode<K, V> left, LLRBNode<K, V> right) {
        this.key = key;
        this.value = value;
        if (left == null) {
            left = LLRBEmptyNode.getInstance();
        }
        this.left = left;
        if (right == null) {
            right = LLRBEmptyNode.getInstance();
        }
        this.right = right;
    }

    public LLRBNode<K, V> getLeft() {
        return this.left;
    }

    public LLRBNode<K, V> getRight() {
        return this.right;
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }

    public LLRBValueNode<K, V> copy(K key, V value, Color color, LLRBNode<K, V> left, LLRBNode<K, V> right) {
        K newKey;
        V newValue;
        LLRBNode<K, V> newLeft;
        LLRBNode<K, V> newRight;
        if (key == null) {
            newKey = this.key;
        } else {
            newKey = key;
        }
        if (value == null) {
            newValue = this.value;
        } else {
            newValue = value;
        }
        if (left == null) {
            newLeft = this.left;
        } else {
            newLeft = left;
        }
        if (right == null) {
            newRight = this.right;
        } else {
            newRight = right;
        }
        if (color == Color.RED) {
            return new LLRBRedValueNode(newKey, newValue, newLeft, newRight);
        }
        return new LLRBBlackValueNode(newKey, newValue, newLeft, newRight);
    }

    public LLRBNode<K, V> insert(K key, V value, Comparator<K> comparator) {
        LLRBValueNode<K, V> n;
        int cmp = comparator.compare(key, this.key);
        if (cmp < 0) {
            n = copy(null, null, this.left.insert(key, value, comparator), null);
        } else if (cmp == 0) {
            n = copy(key, value, null, null);
        } else {
            n = copy(null, null, null, this.right.insert(key, value, comparator));
        }
        return n.fixUp();
    }

    public LLRBNode<K, V> remove(K key, Comparator<K> comparator) {
        LLRBValueNode<K, V> n;
        if (comparator.compare(key, this.key) < 0) {
            if (!(n.left.isEmpty() || n.left.isRed() || ((LLRBValueNode) n.left).left.isRed())) {
                n = moveRedLeft();
            }
            n = n.copy(null, null, n.left.remove(key, comparator), null);
        } else {
            if (n.left.isRed()) {
                n = rotateRight();
            }
            if (!(n.right.isEmpty() || n.right.isRed() || ((LLRBValueNode) n.right).left.isRed())) {
                n = n.moveRedRight();
            }
            if (comparator.compare(key, n.key) == 0) {
                if (n.right.isEmpty()) {
                    return LLRBEmptyNode.getInstance();
                }
                LLRBNode<K, V> smallest = n.right.getMin();
                n = n.copy(smallest.getKey(), smallest.getValue(), null, ((LLRBValueNode) n.right).removeMin());
            }
            n = n.copy(null, null, null, n.right.remove(key, comparator));
        }
        return n.fixUp();
    }

    public boolean isEmpty() {
        return false;
    }

    public LLRBNode<K, V> getMin() {
        return this.left.isEmpty() ? this : this.left.getMin();
    }

    public LLRBNode<K, V> getMax() {
        return this.right.isEmpty() ? this : this.right.getMax();
    }

    public int count() {
        return (this.left.count() + 1) + this.right.count();
    }

    public void inOrderTraversal(NodeVisitor<K, V> visitor) {
        this.left.inOrderTraversal(visitor);
        visitor.visitEntry(this.key, this.value);
        this.right.inOrderTraversal(visitor);
    }

    public boolean shortCircuitingInOrderTraversal(ShortCircuitingNodeVisitor<K, V> visitor) {
        if (this.left.shortCircuitingInOrderTraversal(visitor) && visitor.shouldContinue(this.key, this.value)) {
            return this.right.shortCircuitingInOrderTraversal(visitor);
        }
        return false;
    }

    public boolean shortCircuitingReverseOrderTraversal(ShortCircuitingNodeVisitor<K, V> visitor) {
        if (this.right.shortCircuitingReverseOrderTraversal(visitor) && visitor.shouldContinue(this.key, this.value)) {
            return this.left.shortCircuitingReverseOrderTraversal(visitor);
        }
        return false;
    }

    void setLeft(LLRBNode<K, V> left) {
        this.left = left;
    }

    private LLRBNode<K, V> removeMin() {
        if (this.left.isEmpty()) {
            return LLRBEmptyNode.getInstance();
        }
        LLRBValueNode<K, V> n;
        if (!(getLeft().isRed() || getLeft().getLeft().isRed())) {
            n = moveRedLeft();
        }
        return n.copy(null, null, ((LLRBValueNode) n.left).removeMin(), null).fixUp();
    }

    private LLRBValueNode<K, V> moveRedLeft() {
        LLRBValueNode<K, V> n = colorFlip();
        if (n.getRight().getLeft().isRed()) {
            return n.copy(null, null, null, ((LLRBValueNode) n.getRight()).rotateRight()).rotateLeft().colorFlip();
        }
        return n;
    }

    private LLRBValueNode<K, V> moveRedRight() {
        LLRBValueNode<K, V> n = colorFlip();
        if (n.getLeft().getLeft().isRed()) {
            return n.rotateRight().colorFlip();
        }
        return n;
    }

    private LLRBValueNode<K, V> fixUp() {
        if (this.right.isRed() && !n.left.isRed()) {
            LLRBValueNode<K, V> n = rotateLeft();
        }
        if (n.left.isRed() && ((LLRBValueNode) n.left).left.isRed()) {
            n = n.rotateRight();
        }
        if (n.left.isRed() && n.right.isRed()) {
            return n.colorFlip();
        }
        return n;
    }

    private LLRBValueNode<K, V> rotateLeft() {
        return (LLRBValueNode) this.right.copy(null, null, getColor(), copy(null, null, Color.RED, (LLRBNode) null, ((LLRBValueNode) this.right).left), null);
    }

    private LLRBValueNode<K, V> rotateRight() {
        return (LLRBValueNode) this.left.copy(null, null, getColor(), null, copy(null, null, Color.RED, ((LLRBValueNode) this.left).right, (LLRBNode) null));
    }

    private LLRBValueNode<K, V> colorFlip() {
        return copy(null, null, oppositeColor(this), (LLRBNode) this.left.copy(null, null, oppositeColor(this.left), null, null), this.right.copy(null, null, oppositeColor(this.right), null, null));
    }
}
