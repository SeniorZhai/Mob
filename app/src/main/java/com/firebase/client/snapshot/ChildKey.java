package com.firebase.client.snapshot;

import com.firebase.client.utilities.Utilities;

public class ChildKey implements Comparable<ChildKey> {
    static final /* synthetic */ boolean $assertionsDisabled = (!ChildKey.class.desiredAssertionStatus());
    private static final ChildKey INFO_CHILD_KEY = new ChildKey(".info");
    private static final ChildKey MAX_KEY = new ChildKey("[MAX_KEY]");
    private static final ChildKey MIN_KEY = new ChildKey("[MIN_KEY]");
    private static final ChildKey PRIORITY_CHILD_KEY = new ChildKey(".priority");
    private final String key;

    private static class IntegerChildKey extends ChildKey {
        private final int intValue;

        public /* bridge */ /* synthetic */ int compareTo(Object x0) {
            return super.compareTo((ChildKey) x0);
        }

        IntegerChildKey(String name, int intValue) {
            super(name);
            this.intValue = intValue;
        }

        protected boolean isInt() {
            return true;
        }

        protected int intValue() {
            return this.intValue;
        }

        public String toString() {
            return "IntegerChildName(\"" + this.key + "\")";
        }
    }

    public static ChildKey getMinName() {
        return MIN_KEY;
    }

    public static ChildKey getMaxName() {
        return MAX_KEY;
    }

    public static ChildKey getPriorityKey() {
        return PRIORITY_CHILD_KEY;
    }

    public static ChildKey getInfoKey() {
        return INFO_CHILD_KEY;
    }

    private ChildKey(String key) {
        this.key = key;
    }

    public String asString() {
        return this.key;
    }

    public boolean isPriorityChildName() {
        return this == PRIORITY_CHILD_KEY;
    }

    protected boolean isInt() {
        return false;
    }

    protected int intValue() {
        return 0;
    }

    public int compareTo(ChildKey other) {
        if (this == other) {
            return 0;
        }
        if (this == MIN_KEY || other == MAX_KEY) {
            return -1;
        }
        if (other == MIN_KEY || this == MAX_KEY) {
            return 1;
        }
        if (isInt()) {
            if (!other.isInt()) {
                return -1;
            }
            int cmp = Utilities.compareInts(intValue(), other.intValue());
            if (cmp == 0) {
                return Utilities.compareInts(this.key.length(), other.key.length());
            }
            return cmp;
        } else if (other.isInt()) {
            return 1;
        } else {
            return this.key.compareTo(other.key);
        }
    }

    public String toString() {
        return "ChildKey(\"" + this.key + "\")";
    }

    public int hashCode() {
        return this.key.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ChildKey)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        return this.key.equals(((ChildKey) obj).key);
    }

    public static ChildKey fromString(String key) {
        Integer intValue = Utilities.tryParseInt(key);
        if (intValue != null) {
            return new IntegerChildKey(key, intValue.intValue());
        }
        if (key.equals(".priority")) {
            return PRIORITY_CHILD_KEY;
        }
        if ($assertionsDisabled || !key.contains("/")) {
            return new ChildKey(key);
        }
        throw new AssertionError();
    }
}
