package com.firebase.client.utilities.tuple;

import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.Node;
import com.firebase.client.snapshot.NodeUtilities;

public class NameAndPriority implements Comparable<NameAndPriority> {
    private ChildKey name;
    private Node priority;

    public NameAndPriority(ChildKey name, Node priority) {
        this.name = name;
        this.priority = priority;
    }

    public ChildKey getName() {
        return this.name;
    }

    public Node getPriority() {
        return this.priority;
    }

    public int compareTo(NameAndPriority o) {
        return NodeUtilities.nameAndPriorityCompare(this.name, this.priority, o.name, o.priority);
    }
}
