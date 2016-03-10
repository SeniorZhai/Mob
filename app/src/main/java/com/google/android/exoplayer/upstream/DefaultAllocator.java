package com.google.android.exoplayer.upstream;

import com.google.android.exoplayer.util.Assertions;
import java.util.Arrays;

public final class DefaultAllocator implements Allocator {
    private static final int AVAILABLE_EXTRA_CAPACITY = 100;
    private int allocatedCount;
    private Allocation[] availableAllocations;
    private int availableCount;
    private final int individualAllocationSize;
    private final byte[] initialAllocationBlock;

    public DefaultAllocator(int individualAllocationSize) {
        this(individualAllocationSize, 0);
    }

    public DefaultAllocator(int individualAllocationSize, int initialAllocationCount) {
        boolean z;
        boolean z2 = true;
        if (individualAllocationSize > 0) {
            z = true;
        } else {
            z = false;
        }
        Assertions.checkArgument(z);
        if (initialAllocationCount < 0) {
            z2 = false;
        }
        Assertions.checkArgument(z2);
        this.individualAllocationSize = individualAllocationSize;
        this.availableCount = initialAllocationCount;
        this.availableAllocations = new Allocation[(initialAllocationCount + AVAILABLE_EXTRA_CAPACITY)];
        if (initialAllocationCount > 0) {
            this.initialAllocationBlock = new byte[(initialAllocationCount * individualAllocationSize)];
            for (int i = 0; i < initialAllocationCount; i++) {
                this.availableAllocations[i] = new Allocation(this.initialAllocationBlock, i * individualAllocationSize);
            }
            return;
        }
        this.initialAllocationBlock = null;
    }

    public synchronized Allocation allocate() {
        Allocation allocation;
        this.allocatedCount++;
        if (this.availableCount > 0) {
            Allocation[] allocationArr = this.availableAllocations;
            int i = this.availableCount - 1;
            this.availableCount = i;
            allocation = allocationArr[i];
            this.availableAllocations[this.availableCount] = null;
        } else {
            allocation = new Allocation(new byte[this.individualAllocationSize], 0);
        }
        return allocation;
    }

    public synchronized void release(Allocation allocation) {
        boolean z = allocation.data == this.initialAllocationBlock || allocation.data.length == this.individualAllocationSize;
        Assertions.checkArgument(z);
        this.allocatedCount--;
        if (this.availableCount == this.availableAllocations.length) {
            this.availableAllocations = (Allocation[]) Arrays.copyOf(this.availableAllocations, this.availableAllocations.length * 2);
        }
        Allocation[] allocationArr = this.availableAllocations;
        int i = this.availableCount;
        this.availableCount = i + 1;
        allocationArr[i] = allocation;
        notifyAll();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void trim(int r12) {
        /*
        r11 = this;
        monitor-enter(r11);
        r8 = r11.individualAllocationSize;	 Catch:{ all -> 0x004e }
        r6 = com.google.android.exoplayer.util.Util.ceilDivide(r12, r8);	 Catch:{ all -> 0x004e }
        r8 = 0;
        r9 = r11.allocatedCount;	 Catch:{ all -> 0x004e }
        r9 = r6 - r9;
        r7 = java.lang.Math.max(r8, r9);	 Catch:{ all -> 0x004e }
        r8 = r11.availableCount;	 Catch:{ all -> 0x004e }
        if (r7 < r8) goto L_0x0016;
    L_0x0014:
        monitor-exit(r11);
        return;
    L_0x0016:
        r8 = r11.initialAllocationBlock;	 Catch:{ all -> 0x004e }
        if (r8 == 0) goto L_0x0059;
    L_0x001a:
        r4 = 0;
        r8 = r11.availableCount;	 Catch:{ all -> 0x004e }
        r1 = r8 + -1;
        r2 = r1;
        r5 = r4;
    L_0x0021:
        if (r5 > r2) goto L_0x0051;
    L_0x0023:
        r8 = r11.availableAllocations;	 Catch:{ all -> 0x004e }
        r3 = r8[r5];	 Catch:{ all -> 0x004e }
        r8 = r3.data;	 Catch:{ all -> 0x004e }
        r9 = r11.initialAllocationBlock;	 Catch:{ all -> 0x004e }
        if (r8 != r9) goto L_0x0033;
    L_0x002d:
        r4 = r5 + 1;
        r1 = r2;
    L_0x0030:
        r2 = r1;
        r5 = r4;
        goto L_0x0021;
    L_0x0033:
        r8 = r11.availableAllocations;	 Catch:{ all -> 0x004e }
        r0 = r8[r5];	 Catch:{ all -> 0x004e }
        r8 = r0.data;	 Catch:{ all -> 0x004e }
        r9 = r11.initialAllocationBlock;	 Catch:{ all -> 0x004e }
        if (r8 == r9) goto L_0x0041;
    L_0x003d:
        r1 = r2 + -1;
        r4 = r5;
        goto L_0x0030;
    L_0x0041:
        r8 = r11.availableAllocations;	 Catch:{ all -> 0x004e }
        r4 = r5 + 1;
        r8[r5] = r0;	 Catch:{ all -> 0x004e }
        r8 = r11.availableAllocations;	 Catch:{ all -> 0x004e }
        r1 = r2 + -1;
        r8[r2] = r3;	 Catch:{ all -> 0x004e }
        goto L_0x0030;
    L_0x004e:
        r8 = move-exception;
        monitor-exit(r11);
        throw r8;
    L_0x0051:
        r7 = java.lang.Math.max(r7, r5);	 Catch:{ all -> 0x004e }
        r8 = r11.availableCount;	 Catch:{ all -> 0x004e }
        if (r7 >= r8) goto L_0x0014;
    L_0x0059:
        r8 = r11.availableAllocations;	 Catch:{ all -> 0x004e }
        r9 = r11.availableCount;	 Catch:{ all -> 0x004e }
        r10 = 0;
        java.util.Arrays.fill(r8, r7, r9, r10);	 Catch:{ all -> 0x004e }
        r11.availableCount = r7;	 Catch:{ all -> 0x004e }
        goto L_0x0014;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.exoplayer.upstream.DefaultAllocator.trim(int):void");
    }

    public synchronized int getTotalBytesAllocated() {
        return this.allocatedCount * this.individualAllocationSize;
    }

    public synchronized void blockWhileTotalBytesAllocatedExceeds(int limit) throws InterruptedException {
        while (getTotalBytesAllocated() > limit) {
            wait();
        }
    }

    public int getIndividualAllocationLength() {
        return this.individualAllocationSize;
    }
}
