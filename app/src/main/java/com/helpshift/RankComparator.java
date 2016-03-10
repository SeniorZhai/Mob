package com.helpshift;

import java.util.Comparator;
import java.util.HashMap;

/* compiled from: HSSearch */
class RankComparator implements Comparator<String> {
    HashMap<String, Double> base;

    public RankComparator(HashMap<String, Double> base) {
        this.base = base;
    }

    public int compare(String d1, String d2) {
        if (((Double) this.base.get(d1)).doubleValue() >= ((Double) this.base.get(d2)).doubleValue()) {
            return -1;
        }
        return 1;
    }
}
