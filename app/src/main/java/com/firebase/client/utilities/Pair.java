package com.firebase.client.utilities;

public class Pair<T, U> {
    private final T first;
    private final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return this.first;
    }

    public U getSecond() {
        return this.second;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pair pair = (Pair) o;
        if (this.first == null ? pair.first != null : !this.first.equals(pair.first)) {
            return false;
        }
        if (this.second != null) {
            if (this.second.equals(pair.second)) {
                return true;
            }
        } else if (pair.second == null) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result;
        int i = 0;
        if (this.first != null) {
            result = this.first.hashCode();
        } else {
            result = 0;
        }
        int i2 = result * 31;
        if (this.second != null) {
            i = this.second.hashCode();
        }
        return i2 + i;
    }

    public String toString() {
        return "Pair(" + this.first + "," + this.second + ")";
    }
}
