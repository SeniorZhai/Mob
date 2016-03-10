package com.firebase.client.core.utilities;

public interface Predicate<T> {
    public static final Predicate<Object> TRUE = new Predicate<Object>() {
        public boolean evaluate(Object object) {
            return true;
        }
    };

    boolean evaluate(T t);
}
