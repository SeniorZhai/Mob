package com.firebase.client.core.operation;

import com.firebase.client.core.Path;
import com.firebase.client.core.operation.Operation.OperationType;
import com.firebase.client.snapshot.ChildKey;

public class AckUserWrite extends Operation {
    private final boolean revert;

    public AckUserWrite(Path path, boolean revert) {
        super(OperationType.AckUserWrite, OperationSource.USER, path);
        this.revert = revert;
    }

    public boolean isRevert() {
        return this.revert;
    }

    public Operation operationForChild(ChildKey childKey) {
        if (this.path.isEmpty()) {
            return this;
        }
        return new AckUserWrite(this.path.popFront(), this.revert);
    }

    public String toString() {
        return String.format("AckUserWrite { path=%s, revert=%s }", new Object[]{getPath(), Boolean.valueOf(this.revert)});
    }
}
