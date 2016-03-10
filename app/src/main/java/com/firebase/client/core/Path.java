package com.firebase.client.core;

import com.firebase.client.FirebaseException;
import com.firebase.client.snapshot.ChildKey;
import io.fabric.sdk.android.BuildConfig;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Path implements Iterable<ChildKey> {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final Path EMPTY_PATH = new Path(BuildConfig.FLAVOR);
    private final int end;
    private final ChildKey[] pieces;
    private final int start;

    static {
        boolean z;
        if (Path.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        $assertionsDisabled = z;
    }

    public static Path getRelative(Path from, Path to) {
        ChildKey outerFront = from.getFront();
        ChildKey innerFront = to.getFront();
        if (outerFront == null) {
            return to;
        }
        if (outerFront.equals(innerFront)) {
            return getRelative(from.popFront(), to.popFront());
        }
        throw new FirebaseException("INTERNAL ERROR: " + to + " is not contained in " + from);
    }

    public static Path getEmptyPath() {
        return EMPTY_PATH;
    }

    public Path(ChildKey... segments) {
        this.pieces = (ChildKey[]) Arrays.copyOf(segments, segments.length);
        this.start = 0;
        this.end = segments.length;
        ChildKey[] arr$ = segments;
        int len$ = arr$.length;
        int i$ = 0;
        while (i$ < len$) {
            ChildKey name = arr$[i$];
            if ($assertionsDisabled || name != null) {
                i$++;
            } else {
                throw new AssertionError("Can't construct a path with a null value!");
            }
        }
    }

    public Path(String pathString) {
        String segment;
        String[] segments = pathString.split("/");
        int count = 0;
        for (String segment2 : segments) {
            if (segment2.length() > 0) {
                count++;
            }
        }
        this.pieces = new ChildKey[count];
        String[] arr$ = segments;
        int len$ = arr$.length;
        int i$ = 0;
        int j = 0;
        while (i$ < len$) {
            int j2;
            segment2 = arr$[i$];
            if (segment2.length() > 0) {
                j2 = j + 1;
                this.pieces[j] = ChildKey.fromString(segment2);
            } else {
                j2 = j;
            }
            i$++;
            j = j2;
        }
        this.start = 0;
        this.end = this.pieces.length;
    }

    private Path(ChildKey[] pieces, int start, int end) {
        this.pieces = pieces;
        this.start = start;
        this.end = end;
    }

    public Path child(Path path) {
        int newSize = size() + path.size();
        ChildKey[] newPieces = new ChildKey[newSize];
        System.arraycopy(this.pieces, this.start, newPieces, 0, size());
        System.arraycopy(path.pieces, path.start, newPieces, size(), path.size());
        return new Path(newPieces, 0, newSize);
    }

    public Path child(ChildKey child) {
        int size = size();
        ChildKey[] newPieces = new ChildKey[(size + 1)];
        System.arraycopy(this.pieces, this.start, newPieces, 0, size);
        newPieces[size] = child;
        return new Path(newPieces, 0, size + 1);
    }

    public String toString() {
        if (isEmpty()) {
            return "/";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = this.start; i < this.end; i++) {
            builder.append("/");
            builder.append(this.pieces[i].asString());
        }
        return builder.toString();
    }

    public String wireFormat() {
        if (isEmpty()) {
            return "/";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = this.start; i < this.end; i++) {
            if (i > this.start) {
                builder.append("/");
            }
            builder.append(this.pieces[i].asString());
        }
        return builder.toString();
    }

    public ChildKey getFront() {
        if (isEmpty()) {
            return null;
        }
        return this.pieces[this.start];
    }

    public Path popFront() {
        int newStart = this.start;
        if (!isEmpty()) {
            newStart++;
        }
        return new Path(this.pieces, newStart, this.end);
    }

    public Path getParent() {
        if (isEmpty()) {
            return null;
        }
        return new Path(this.pieces, this.start, this.end - 1);
    }

    public ChildKey getBack() {
        if (isEmpty()) {
            return null;
        }
        return this.pieces[this.end - 1];
    }

    public boolean isEmpty() {
        return this.start >= this.end;
    }

    public int size() {
        return this.end - this.start;
    }

    public Iterator<ChildKey> iterator() {
        return new Iterator<ChildKey>() {
            int offset = Path.this.start;

            public boolean hasNext() {
                return this.offset < Path.this.end;
            }

            public ChildKey next() {
                if (hasNext()) {
                    ChildKey child = Path.this.pieces[this.offset];
                    this.offset++;
                    return child;
                }
                throw new NoSuchElementException("No more elements.");
            }

            public void remove() {
                throw new UnsupportedOperationException("Can't remove component from immutable Path!");
            }
        };
    }

    public boolean contains(Path other) {
        if (size() > other.size()) {
            return false;
        }
        int i = this.start;
        int j = other.start;
        while (i < this.end) {
            if (!this.pieces[i].equals(other.pieces[j])) {
                return false;
            }
            i++;
            j++;
        }
        return true;
    }

    public boolean equals(Object other) {
        if (!(other instanceof Path)) {
            return false;
        }
        if (this == other) {
            return true;
        }
        Path otherPath = (Path) other;
        if (size() != otherPath.size()) {
            return false;
        }
        int i = this.start;
        int j = otherPath.start;
        while (i < this.end && j < otherPath.end) {
            if (!this.pieces[i].equals(otherPath.pieces[j])) {
                return false;
            }
            i++;
            j++;
        }
        return true;
    }

    public int hashCode() {
        int hashCode = 0;
        for (int i = this.start; i < this.end; i++) {
            hashCode = (hashCode * 37) + this.pieces[i].hashCode();
        }
        return hashCode;
    }
}
