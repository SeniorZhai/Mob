package com.firebase.client.core.operation;

import com.firebase.client.core.Path;
import com.firebase.client.core.operation.Operation.OperationType;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.Node;

public class Overwrite extends Operation {
    private final Node snapshot;

    public Overwrite(OperationSource source, Path path, Node snapshot) {
        super(OperationType.Overwrite, source, path);
        this.snapshot = snapshot;
    }

    public Node getSnapshot() {
        return this.snapshot;
    }

    public Operation operationForChild(ChildKey childKey) {
        if (this.path.isEmpty()) {
            return new Overwrite(this.source, Path.getEmptyPath(), this.snapshot.getImmediateChild(childKey));
        }
        return new Overwrite(this.source, this.path.popFront(), this.snapshot);
    }

    public String toString() {
        return String.format("Overwrite { path=%s, source=%s, snapshot=%s }", new Object[]{getPath(), getSource(), this.snapshot});
    }
}
