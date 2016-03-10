package com.firebase.client.utilities.tuple;

import com.firebase.client.core.Path;
import com.firebase.client.snapshot.Node;

public class NodeAndPath {
    private Node node;
    private Path path;

    public NodeAndPath(Node node, Path path) {
        this.node = node;
        this.path = path;
    }

    public Node getNode() {
        return this.node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Path getPath() {
        return this.path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
