package com.firebase.client.core;

import com.firebase.client.core.utilities.Predicate;
import com.firebase.client.core.view.CacheNode;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.EmptyNode;
import com.firebase.client.snapshot.Index;
import com.firebase.client.snapshot.NamedNode;
import com.firebase.client.snapshot.Node;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class WriteTree {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final Predicate<UserWriteRecord> DEFAULT_FILTER = new Predicate<UserWriteRecord>() {
        public boolean evaluate(UserWriteRecord write) {
            return write.isVisible();
        }
    };
    private List<UserWriteRecord> allWrites = new ArrayList();
    private Long lastWriteId = Long.valueOf(-1);
    private CompoundWrite visibleWrites = CompoundWrite.emptyWrite();

    static {
        boolean z;
        if (WriteTree.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        $assertionsDisabled = z;
    }

    public WriteTreeRef childWrites(Path path) {
        return new WriteTreeRef(path, this);
    }

    public void addOverwrite(Path path, Node snap, Long writeId, boolean visible) {
        if ($assertionsDisabled || writeId.longValue() > this.lastWriteId.longValue()) {
            this.allWrites.add(new UserWriteRecord(writeId.longValue(), path, snap, visible));
            if (visible) {
                this.visibleWrites = this.visibleWrites.addWrite(path, snap);
            }
            this.lastWriteId = writeId;
            return;
        }
        throw new AssertionError();
    }

    public void addMerge(Path path, CompoundWrite changedChildren, Long writeId) {
        if ($assertionsDisabled || writeId.longValue() > this.lastWriteId.longValue()) {
            this.allWrites.add(new UserWriteRecord(writeId.longValue(), path, changedChildren));
            this.visibleWrites = this.visibleWrites.addWrites(path, changedChildren);
            this.lastWriteId = writeId;
            return;
        }
        throw new AssertionError();
    }

    public UserWriteRecord getWrite(long writeId) {
        for (UserWriteRecord record : this.allWrites) {
            if (record.getWriteId() == writeId) {
                return record;
            }
        }
        return null;
    }

    public List<UserWriteRecord> purgeAllWrites() {
        List<UserWriteRecord> purgedWrites = new ArrayList(this.allWrites);
        this.visibleWrites = CompoundWrite.emptyWrite();
        this.allWrites = new ArrayList();
        return purgedWrites;
    }

    public Path removeWrite(long writeId) {
        Iterator i$;
        UserWriteRecord writeToRemove = null;
        int idx = 0;
        for (UserWriteRecord record : this.allWrites) {
            if (record.getWriteId() == writeId) {
                writeToRemove = record;
                break;
            }
            idx++;
        }
        if ($assertionsDisabled || writeToRemove != null) {
            this.allWrites.remove(writeToRemove);
            boolean removedWriteWasVisible = writeToRemove.isVisible();
            boolean removedWriteOverlapsWithOtherWrites = false;
            int i = this.allWrites.size() - 1;
            while (removedWriteWasVisible && i >= 0) {
                UserWriteRecord currentWrite = (UserWriteRecord) this.allWrites.get(i);
                if (currentWrite.isVisible()) {
                    if (i >= idx && recordContainsPath(currentWrite, writeToRemove.getPath())) {
                        removedWriteWasVisible = false;
                    } else if (writeToRemove.getPath().contains(currentWrite.getPath())) {
                        removedWriteOverlapsWithOtherWrites = true;
                    }
                }
                i--;
            }
            if (!removedWriteWasVisible) {
                return null;
            }
            if (removedWriteOverlapsWithOtherWrites) {
                resetTree();
                return writeToRemove.getPath();
            }
            if (writeToRemove.isOverwrite()) {
                this.visibleWrites = this.visibleWrites.removeWrite(writeToRemove.getPath());
            } else {
                i$ = writeToRemove.getMerge().iterator();
                while (i$.hasNext()) {
                    this.visibleWrites = this.visibleWrites.removeWrite(writeToRemove.getPath().child((Path) ((Entry) i$.next()).getKey()));
                }
            }
            return writeToRemove.getPath();
        }
        throw new AssertionError("removeWrite called with nonexistent writeId");
    }

    public Node getCompleteWriteData(Path path) {
        return this.visibleWrites.getCompleteNode(path);
    }

    public Node calcCompleteEventCache(Path treePath, Node completeServerCache) {
        return calcCompleteEventCache(treePath, completeServerCache, new ArrayList());
    }

    public Node calcCompleteEventCache(Path treePath, Node completeServerCache, List<Long> writeIdsToExclude) {
        return calcCompleteEventCache(treePath, completeServerCache, writeIdsToExclude, false);
    }

    public Node calcCompleteEventCache(final Path treePath, Node completeServerCache, final List<Long> writeIdsToExclude, final boolean includeHiddenWrites) {
        if (!writeIdsToExclude.isEmpty() || includeHiddenWrites) {
            CompoundWrite merge = this.visibleWrites.childCompoundWrite(treePath);
            if (!includeHiddenWrites && merge.isEmpty()) {
                return completeServerCache;
            }
            if (!includeHiddenWrites && completeServerCache == null && !merge.hasCompleteWrite(Path.getEmptyPath())) {
                return null;
            }
            return layerTree(this.allWrites, new Predicate<UserWriteRecord>() {
                public boolean evaluate(UserWriteRecord write) {
                    return (write.isVisible() || includeHiddenWrites) && !writeIdsToExclude.contains(Long.valueOf(write.getWriteId())) && (write.getPath().contains(treePath) || treePath.contains(write.getPath()));
                }
            }, treePath).apply(completeServerCache != null ? completeServerCache : EmptyNode.Empty());
        }
        Node shadowingNode = this.visibleWrites.getCompleteNode(treePath);
        if (shadowingNode != null) {
            return shadowingNode;
        }
        CompoundWrite subMerge = this.visibleWrites.childCompoundWrite(treePath);
        if (subMerge.isEmpty()) {
            return completeServerCache;
        }
        if (completeServerCache == null && !subMerge.hasCompleteWrite(Path.getEmptyPath())) {
            return null;
        }
        Node layeredCache;
        if (completeServerCache != null) {
            layeredCache = completeServerCache;
        } else {
            layeredCache = EmptyNode.Empty();
        }
        return subMerge.apply(layeredCache);
    }

    public Node calcCompleteEventChildren(Path treePath, Node completeServerChildren) {
        Node completeChildren = EmptyNode.Empty();
        Node<NamedNode> topLevelSet = this.visibleWrites.getCompleteNode(treePath);
        if (topLevelSet != null) {
            if (!topLevelSet.isLeafNode()) {
                for (NamedNode childEntry : topLevelSet) {
                    completeChildren = completeChildren.updateImmediateChild(childEntry.getName(), childEntry.getNode());
                }
            }
            return completeChildren;
        }
        CompoundWrite merge = this.visibleWrites.childCompoundWrite(treePath);
        for (NamedNode entry : completeServerChildren) {
            completeChildren = completeChildren.updateImmediateChild(entry.getName(), merge.childCompoundWrite(new Path(entry.getName())).apply(entry.getNode()));
        }
        for (NamedNode node : merge.getCompleteChildren()) {
            completeChildren = completeChildren.updateImmediateChild(node.getName(), node.getNode());
        }
        return completeChildren;
    }

    public Node calcEventCacheAfterServerOverwrite(Path treePath, Path childPath, Node existingEventSnap, Node existingServerSnap) {
        if (!$assertionsDisabled && existingEventSnap == null && existingServerSnap == null) {
            throw new AssertionError("Either existingEventSnap or existingServerSnap must exist");
        }
        Path path = treePath.child(childPath);
        if (this.visibleWrites.hasCompleteWrite(path)) {
            return null;
        }
        CompoundWrite childMerge = this.visibleWrites.childCompoundWrite(path);
        if (childMerge.isEmpty()) {
            return existingServerSnap.getChild(childPath);
        }
        return childMerge.apply(existingServerSnap.getChild(childPath));
    }

    public Node calcCompleteChild(Path treePath, ChildKey childKey, CacheNode existingServerSnap) {
        Path path = treePath.child(childKey);
        Node shadowingNode = this.visibleWrites.getCompleteNode(path);
        if (shadowingNode != null) {
            return shadowingNode;
        }
        if (existingServerSnap.isCompleteForChild(childKey)) {
            return this.visibleWrites.childCompoundWrite(path).apply(existingServerSnap.getNode().getImmediateChild(childKey));
        }
        return null;
    }

    public Node shadowingWrite(Path path) {
        return this.visibleWrites.getCompleteNode(path);
    }

    public NamedNode calcNextNodeAfterPost(Path treePath, Node completeServerData, NamedNode post, boolean reverse, Index index) {
        CompoundWrite merge = this.visibleWrites.childCompoundWrite(treePath);
        Node shadowingNode = merge.getCompleteNode(Path.getEmptyPath());
        Node toIterate;
        if (shadowingNode != null) {
            toIterate = shadowingNode;
        } else if (completeServerData == null) {
            return null;
        } else {
            toIterate = merge.apply(completeServerData);
        }
        NamedNode currentNext = null;
        for (NamedNode node : toIterate) {
            if (index.compare(node, post, reverse) > 0 && (currentNext == null || index.compare(node, currentNext, reverse) < 0)) {
                currentNext = node;
            }
        }
        return currentNext;
    }

    private boolean recordContainsPath(UserWriteRecord writeRecord, Path path) {
        if (writeRecord.isOverwrite()) {
            return writeRecord.getPath().contains(path);
        }
        Iterator i$ = writeRecord.getMerge().iterator();
        while (i$.hasNext()) {
            if (writeRecord.getPath().child((Path) ((Entry) i$.next()).getKey()).contains(path)) {
                return true;
            }
        }
        return false;
    }

    private void resetTree() {
        this.visibleWrites = layerTree(this.allWrites, DEFAULT_FILTER, Path.getEmptyPath());
        if (this.allWrites.size() > 0) {
            this.lastWriteId = Long.valueOf(((UserWriteRecord) this.allWrites.get(this.allWrites.size() - 1)).getWriteId());
        } else {
            this.lastWriteId = Long.valueOf(-1);
        }
    }

    private static CompoundWrite layerTree(List<UserWriteRecord> writes, Predicate<UserWriteRecord> filter, Path treeRoot) {
        CompoundWrite compoundWrite = CompoundWrite.emptyWrite();
        for (UserWriteRecord write : writes) {
            if (filter.evaluate(write)) {
                Path writePath = write.getPath();
                if (write.isOverwrite()) {
                    if (treeRoot.contains(writePath)) {
                        compoundWrite = compoundWrite.addWrite(Path.getRelative(treeRoot, writePath), write.getOverwrite());
                    } else if (writePath.contains(treeRoot)) {
                        compoundWrite = compoundWrite.addWrite(Path.getEmptyPath(), write.getOverwrite().getChild(Path.getRelative(writePath, treeRoot)));
                    }
                } else if (treeRoot.contains(writePath)) {
                    compoundWrite = compoundWrite.addWrites(Path.getRelative(treeRoot, writePath), write.getMerge());
                } else if (writePath.contains(treeRoot)) {
                    Path relativePath = Path.getRelative(writePath, treeRoot);
                    if (relativePath.isEmpty()) {
                        compoundWrite = compoundWrite.addWrites(Path.getEmptyPath(), write.getMerge());
                    } else {
                        Node deepNode = write.getMerge().getCompleteNode(relativePath);
                        if (deepNode != null) {
                            compoundWrite = compoundWrite.addWrite(Path.getEmptyPath(), deepNode);
                        }
                    }
                }
            }
        }
        return compoundWrite;
    }
}
