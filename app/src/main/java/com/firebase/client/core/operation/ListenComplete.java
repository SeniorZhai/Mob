package com.firebase.client.core.operation;

import com.firebase.client.core.Path;
import com.firebase.client.core.operation.Operation.OperationType;
import com.firebase.client.snapshot.ChildKey;

public class ListenComplete extends Operation {
    static final /* synthetic */ boolean $assertionsDisabled = (!ListenComplete.class.desiredAssertionStatus());

    public ListenComplete(OperationSource source, Path path) {
        super(OperationType.ListenComplete, source, path);
        if (!$assertionsDisabled && source.isFromUser()) {
            throw new AssertionError("Can't have a listen complete from a user source");
        }
    }

    public Operation operationForChild(ChildKey childKey) {
        if (this.path.isEmpty()) {
            return new ListenComplete(this.source, Path.getEmptyPath());
        }
        return new ListenComplete(this.source, this.path.popFront());
    }

    public String toString() {
        return String.format("ListenComplete { path=%s, source=%s }", new Object[]{getPath(), getSource()});
    }
}
