package com.firebase.client.snapshot;

import com.firebase.client.FirebaseException;
import com.firebase.client.collection.ImmutableSortedMap.Builder;
import com.firebase.client.core.ServerValues;
import io.fabric.sdk.android.BuildConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeUtilities {
    public static Node NodeFromJSON(Object value) throws FirebaseException {
        return NodeFromJSON(value, PriorityUtilities.NullPriority());
    }

    public static Node NodeFromJSON(Object value, Node priority) throws FirebaseException {
        try {
            Map mapValue;
            if (value instanceof Map) {
                mapValue = (Map) value;
                if (mapValue.containsKey(".priority")) {
                    priority = PriorityUtilities.parsePriority(mapValue.get(".priority"));
                }
                if (mapValue.containsKey(".value")) {
                    value = mapValue.get(".value");
                }
            }
            if (value == null) {
                return EmptyNode.Empty();
            }
            if (value instanceof String) {
                return new StringNode((String) value, priority);
            }
            if (value instanceof Long) {
                return new LongNode((Long) value, priority);
            }
            if (value instanceof Integer) {
                return new LongNode(Long.valueOf((long) ((Integer) value).intValue()), priority);
            }
            if (value instanceof Double) {
                return new DoubleNode((Double) value, priority);
            }
            if (value instanceof Boolean) {
                return new BooleanNode((Boolean) value, priority);
            }
            if ((value instanceof Map) || (value instanceof List)) {
                Map<ChildKey, Node> childData;
                String key;
                Node childNode;
                if (value instanceof Map) {
                    mapValue = (Map) value;
                    if (mapValue.containsKey(ServerValues.NAME_SUBKEY_SERVERVALUE)) {
                        return new DeferredValueNode(mapValue, priority);
                    }
                    childData = new HashMap(mapValue.size());
                    for (String key2 : mapValue.keySet()) {
                        if (!key2.startsWith(".")) {
                            childNode = NodeFromJSON(mapValue.get(key2));
                            if (!childNode.isEmpty()) {
                                childData.put(ChildKey.fromString(key2), childNode);
                            }
                        }
                    }
                } else {
                    List listValue = (List) value;
                    childData = new HashMap(listValue.size());
                    for (int i = 0; i < listValue.size(); i++) {
                        key2 = BuildConfig.FLAVOR + i;
                        childNode = NodeFromJSON(listValue.get(i));
                        if (!childNode.isEmpty()) {
                            childData.put(ChildKey.fromString(key2), childNode);
                        }
                    }
                }
                if (childData.isEmpty()) {
                    return EmptyNode.Empty();
                }
                return new ChildrenNode(Builder.fromMap(childData, ChildrenNode.NAME_ONLY_COMPARATOR), priority);
            }
            throw new FirebaseException("Failed to parse node with class " + value.getClass().toString());
        } catch (ClassCastException e) {
            throw new FirebaseException("Failed to parse node", e);
        }
    }

    public static int nameAndPriorityCompare(ChildKey aKey, Node aPriority, ChildKey bKey, Node bPriority) {
        int priCmp = aPriority.compareTo(bPriority);
        return priCmp != 0 ? priCmp : aKey.compareTo(bKey);
    }
}
