package com.firebase.client.snapshot;

import com.firebase.client.FirebaseException;

public class PriorityUtilities {
    public static Node NullPriority() {
        return EmptyNode.Empty();
    }

    public static boolean isValidPriority(Node priority) {
        return priority.getPriority().isEmpty() && (priority.isEmpty() || (priority instanceof DoubleNode) || (priority instanceof StringNode) || (priority instanceof DeferredValueNode));
    }

    public static Node parsePriority(Object value) {
        Node priority = NodeUtilities.NodeFromJSON(value);
        if (priority instanceof LongNode) {
            priority = new DoubleNode(Double.valueOf((double) ((Long) priority.getValue()).longValue()), NullPriority());
        }
        if (isValidPriority(priority)) {
            return priority;
        }
        throw new FirebaseException("Invalid Firebase priority (must be a string, double, ServerValue, or null)");
    }
}
