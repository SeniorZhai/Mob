package com.firebase.client.core.utilities;

import com.firebase.client.snapshot.ChildKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TreeNode<T> {
    public Map<ChildKey, TreeNode<T>> children = new HashMap();
    public T value;

    String toString(String prefix) {
        String result = prefix + "<value>: " + this.value + "\n";
        if (this.children.isEmpty()) {
            return result + prefix + "<empty>";
        }
        for (Entry<ChildKey, TreeNode<T>> entry : this.children.entrySet()) {
            result = result + prefix + entry.getKey() + ":\n" + ((TreeNode) entry.getValue()).toString(prefix + "\t") + "\n";
        }
        return result;
    }
}
