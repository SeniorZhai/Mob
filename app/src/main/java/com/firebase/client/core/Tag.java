package com.firebase.client.core;

public class Tag {
    private final long tagNumber;

    public Tag(long tagNumber) {
        this.tagNumber = tagNumber;
    }

    public long getTagNumber() {
        return this.tagNumber;
    }

    public String toString() {
        return "Tag{tagNumber=" + this.tagNumber + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (this.tagNumber != ((Tag) o).tagNumber) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (int) (this.tagNumber ^ (this.tagNumber >>> 32));
    }
}
