package com.firebase.client.collection;

import java.util.Comparator;

public class StandardComparator<A extends Comparable<A>> implements Comparator<A> {
    private static StandardComparator INSTANCE = new StandardComparator();

    private StandardComparator() {
    }

    public static <T extends Comparable<T>> StandardComparator<T> getComparator(Class<T> cls) {
        return INSTANCE;
    }

    public int compare(A o1, A o2) {
        return o1.compareTo(o2);
    }
}
