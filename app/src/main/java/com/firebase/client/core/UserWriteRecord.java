package com.firebase.client.core;

import com.firebase.client.snapshot.Node;

public class UserWriteRecord {
    private final CompoundWrite merge;
    private final Node overwrite;
    private final Path path;
    private final boolean visible;
    private final long writeId;

    public UserWriteRecord(long writeId, Path path, Node overwrite, boolean visible) {
        this.writeId = writeId;
        this.path = path;
        this.overwrite = overwrite;
        this.merge = null;
        this.visible = visible;
    }

    public UserWriteRecord(long writeId, Path path, CompoundWrite merge) {
        this.writeId = writeId;
        this.path = path;
        this.overwrite = null;
        this.merge = merge;
        this.visible = true;
    }

    public long getWriteId() {
        return this.writeId;
    }

    public Path getPath() {
        return this.path;
    }

    public Node getOverwrite() {
        if (this.overwrite != null) {
            return this.overwrite;
        }
        throw new IllegalArgumentException("Can't access overwrite when write is a merge!");
    }

    public CompoundWrite getMerge() {
        if (this.merge != null) {
            return this.merge;
        }
        throw new IllegalArgumentException("Can't access merge when write is an overwrite!");
    }

    public boolean isMerge() {
        return this.merge != null;
    }

    public boolean isOverwrite() {
        return this.overwrite != null;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserWriteRecord record = (UserWriteRecord) o;
        if (this.writeId != record.writeId) {
            return false;
        }
        if (!this.path.equals(record.path)) {
            return false;
        }
        if (this.visible != record.visible) {
            return false;
        }
        if (!this.overwrite == null ? this.overwrite.equals(record.overwrite) : record.overwrite == null) {
            return false;
        }
        if (this.merge != null) {
            if (this.merge.equals(record.merge)) {
                return true;
            }
        } else if (record.merge == null) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        int hashCode2 = ((((Long.valueOf(this.writeId).hashCode() * 31) + Boolean.valueOf(this.visible).hashCode()) * 31) + this.path.hashCode()) * 31;
        if (this.overwrite != null) {
            hashCode = this.overwrite.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (hashCode2 + hashCode) * 31;
        if (this.merge != null) {
            i = this.merge.hashCode();
        }
        return hashCode + i;
    }

    public String toString() {
        return "UserWriteRecord{id=" + this.writeId + " path=" + this.path + " visible=" + this.visible + " overwrite=" + this.overwrite + " merge=" + this.merge + "}";
    }
}
