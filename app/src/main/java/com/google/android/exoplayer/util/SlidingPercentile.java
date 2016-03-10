package com.google.android.exoplayer.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SlidingPercentile {
    private static final Comparator<Sample> INDEX_COMPARATOR = new Comparator<Sample>() {
        public int compare(Sample a, Sample b) {
            return a.index - b.index;
        }
    };
    private static final int MAX_RECYCLED_SAMPLES = 5;
    private static final int SORT_ORDER_BY_INDEX = 1;
    private static final int SORT_ORDER_BY_VALUE = 0;
    private static final int SORT_ORDER_NONE = -1;
    private static final Comparator<Sample> VALUE_COMPARATOR = new Comparator<Sample>() {
        public int compare(Sample a, Sample b) {
            if (a.value < b.value) {
                return SlidingPercentile.SORT_ORDER_NONE;
            }
            return b.value < a.value ? SlidingPercentile.SORT_ORDER_BY_INDEX : SlidingPercentile.SORT_ORDER_BY_VALUE;
        }
    };
    private int currentSortOrder = SORT_ORDER_NONE;
    private final int maxWeight;
    private int nextSampleIndex;
    private int recycledSampleCount;
    private final Sample[] recycledSamples = new Sample[MAX_RECYCLED_SAMPLES];
    private final ArrayList<Sample> samples = new ArrayList();
    private int totalWeight;

    private static class Sample {
        public int index;
        public float value;
        public int weight;

        private Sample() {
        }
    }

    public SlidingPercentile(int maxWeight) {
        this.maxWeight = maxWeight;
    }

    public void addSample(int weight, float value) {
        Sample newSample;
        ensureSortedByIndex();
        if (this.recycledSampleCount > 0) {
            Sample[] sampleArr = this.recycledSamples;
            int i = this.recycledSampleCount + SORT_ORDER_NONE;
            this.recycledSampleCount = i;
            newSample = sampleArr[i];
        } else {
            newSample = new Sample();
        }
        int i2 = this.nextSampleIndex;
        this.nextSampleIndex = i2 + SORT_ORDER_BY_INDEX;
        newSample.index = i2;
        newSample.weight = weight;
        newSample.value = value;
        this.samples.add(newSample);
        this.totalWeight += weight;
        while (this.totalWeight > this.maxWeight) {
            int excessWeight = this.totalWeight - this.maxWeight;
            Sample oldestSample = (Sample) this.samples.get(SORT_ORDER_BY_VALUE);
            if (oldestSample.weight <= excessWeight) {
                this.totalWeight -= oldestSample.weight;
                this.samples.remove(SORT_ORDER_BY_VALUE);
                if (this.recycledSampleCount < MAX_RECYCLED_SAMPLES) {
                    sampleArr = this.recycledSamples;
                    i = this.recycledSampleCount;
                    this.recycledSampleCount = i + SORT_ORDER_BY_INDEX;
                    sampleArr[i] = oldestSample;
                }
            } else {
                oldestSample.weight -= excessWeight;
                this.totalWeight -= excessWeight;
            }
        }
    }

    public float getPercentile(float percentile) {
        ensureSortedByValue();
        float desiredWeight = percentile * ((float) this.totalWeight);
        int accumulatedWeight = SORT_ORDER_BY_VALUE;
        for (int i = SORT_ORDER_BY_VALUE; i < this.samples.size(); i += SORT_ORDER_BY_INDEX) {
            Sample currentSample = (Sample) this.samples.get(i);
            accumulatedWeight += currentSample.weight;
            if (((float) accumulatedWeight) >= desiredWeight) {
                return currentSample.value;
            }
        }
        return this.samples.isEmpty() ? Float.NaN : ((Sample) this.samples.get(this.samples.size() + SORT_ORDER_NONE)).value;
    }

    private void ensureSortedByIndex() {
        if (this.currentSortOrder != SORT_ORDER_BY_INDEX) {
            Collections.sort(this.samples, INDEX_COMPARATOR);
            this.currentSortOrder = SORT_ORDER_BY_INDEX;
        }
    }

    private void ensureSortedByValue() {
        if (this.currentSortOrder != 0) {
            Collections.sort(this.samples, VALUE_COMPARATOR);
            this.currentSortOrder = SORT_ORDER_BY_VALUE;
        }
    }
}
