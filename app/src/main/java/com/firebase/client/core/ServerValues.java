package com.firebase.client.core;

import com.firebase.client.core.SparseSnapshotTree.SparseSnapshotTreeVisitor;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.ChildrenNode;
import com.firebase.client.snapshot.ChildrenNode.ChildVisitor;
import com.firebase.client.snapshot.Node;
import com.firebase.client.snapshot.NodeUtilities;
import com.firebase.client.snapshot.PriorityUtilities;
import com.firebase.client.utilities.Clock;
import com.mobcrush.mobcrush.Constants;
import io.fabric.sdk.android.BuildConfig;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class ServerValues {
    public static final String NAME_SUBKEY_SERVERVALUE = ".sv";

    public static Map<String, Object> generateServerValues(Clock clock) {
        Map<String, Object> values = new HashMap();
        values.put(Constants.CHAT_MESSAGE_TIMESTAMP, Long.valueOf(clock.millis()));
        return values;
    }

    public static Object resolveDeferredValue(Object value, Map<String, Object> serverValues) {
        if (!(value instanceof Map)) {
            return value;
        }
        Map mapValue = (Map) value;
        if (!mapValue.containsKey(NAME_SUBKEY_SERVERVALUE)) {
            return value;
        }
        String serverValueKey = (String) mapValue.get(NAME_SUBKEY_SERVERVALUE);
        if (serverValues.containsKey(serverValueKey)) {
            return serverValues.get(serverValueKey);
        }
        return value;
    }

    public static SparseSnapshotTree resolveDeferredValueTree(SparseSnapshotTree tree, final Map<String, Object> serverValues) {
        final SparseSnapshotTree resolvedTree = new SparseSnapshotTree();
        tree.forEachTree(new Path(BuildConfig.FLAVOR), new SparseSnapshotTreeVisitor() {
            public void visitTree(Path prefixPath, Node tree) {
                resolvedTree.remember(prefixPath, ServerValues.resolveDeferredValueSnapshot(tree, serverValues));
            }
        });
        return resolvedTree;
    }

    public static Node resolveDeferredValueSnapshot(Node data, final Map<String, Object> serverValues) {
        Object priorityVal = data.getPriority().getValue();
        if (priorityVal instanceof Map) {
            Map priorityMapValue = (Map) priorityVal;
            if (priorityMapValue.containsKey(NAME_SUBKEY_SERVERVALUE)) {
                priorityVal = serverValues.get((String) priorityMapValue.get(NAME_SUBKEY_SERVERVALUE));
            }
        }
        Node priority = PriorityUtilities.parsePriority(priorityVal);
        if (data.isLeafNode()) {
            Object value = resolveDeferredValue(data.getValue(), serverValues);
            if (value.equals(data.getValue()) && priority.equals(data.getPriority())) {
                return data;
            }
            return NodeUtilities.NodeFromJSON(value, priority);
        } else if (data.isEmpty()) {
            return data;
        } else {
            ChildrenNode childNode = (ChildrenNode) data;
            final SnapshotHolder holder = new SnapshotHolder(childNode);
            childNode.forEachChild(new ChildVisitor() {
                public void visitChild(ChildKey name, Node child) {
                    Node newChildNode = ServerValues.resolveDeferredValueSnapshot(child, serverValues);
                    if (newChildNode != child) {
                        holder.update(new Path(name.asString()), newChildNode);
                    }
                }
            });
            if (holder.getRootNode().getPriority().equals(priority)) {
                return holder.getRootNode();
            }
            return holder.getRootNode().updatePriority(priority);
        }
    }

    public static CompoundWrite resolveDeferredValueMerge(CompoundWrite merge, Map<String, Object> serverValues) {
        CompoundWrite write = CompoundWrite.emptyWrite();
        Iterator i$ = merge.iterator();
        while (i$.hasNext()) {
            Entry<Path, Node> entry = (Entry) i$.next();
            write = write.addWrite((Path) entry.getKey(), resolveDeferredValueSnapshot((Node) entry.getValue(), serverValues));
        }
        return write;
    }
}
