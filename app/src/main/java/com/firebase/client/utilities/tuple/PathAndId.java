package com.firebase.client.utilities.tuple;

import com.firebase.client.core.Path;

public class PathAndId {
    private long id;
    private Path path;

    public PathAndId(Path path, long id) {
        this.path = path;
        this.id = id;
    }

    public Path getPath() {
        return this.path;
    }

    public long getId() {
        return this.id;
    }
}
