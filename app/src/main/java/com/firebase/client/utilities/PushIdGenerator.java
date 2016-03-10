package com.firebase.client.utilities;

import java.util.Random;

public class PushIdGenerator {
    static final /* synthetic */ boolean $assertionsDisabled = (!PushIdGenerator.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    private static final String PUSH_CHARS = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz";
    private static long lastPushTime = 0;
    private static final int[] lastRandChars = new int[12];
    private static final Random randGen = new Random();

    public static synchronized String generatePushChildName(long now) {
        String stringBuilder;
        synchronized (PushIdGenerator.class) {
            int i;
            boolean duplicateTime = now == lastPushTime ? true : $assertionsDisabled;
            lastPushTime = now;
            char[] timeStampChars = new char[8];
            StringBuilder result = new StringBuilder(20);
            for (i = 7; i >= 0; i--) {
                timeStampChars[i] = PUSH_CHARS.charAt((int) (now % 64));
                now /= 64;
            }
            if ($assertionsDisabled || now == 0) {
                result.append(timeStampChars);
                if (duplicateTime) {
                    incrementArray();
                } else {
                    for (i = 0; i < 12; i++) {
                        lastRandChars[i] = randGen.nextInt(64);
                    }
                }
                for (i = 0; i < 12; i++) {
                    result.append(PUSH_CHARS.charAt(lastRandChars[i]));
                }
                if ($assertionsDisabled || result.length() == 20) {
                    stringBuilder = result.toString();
                } else {
                    throw new AssertionError();
                }
            }
            throw new AssertionError();
        }
        return stringBuilder;
    }

    private static void incrementArray() {
        int i = 11;
        while (i >= 0) {
            if (lastRandChars[i] != 63) {
                lastRandChars[i] = lastRandChars[i] + 1;
                return;
            } else {
                lastRandChars[i] = 0;
                i--;
            }
        }
    }
}
