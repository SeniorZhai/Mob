package com.fasterxml.jackson.core.sym;

import com.fasterxml.jackson.core.util.InternCache;
import io.fabric.sdk.android.BuildConfig;
import java.util.Arrays;

public final class CharsToNameCanonicalizer {
    protected static final int DEFAULT_TABLE_SIZE = 64;
    public static final int HASH_MULT = 33;
    static final int MAX_COLL_CHAIN_FOR_REUSE = 63;
    static final int MAX_COLL_CHAIN_LENGTH = 255;
    static final int MAX_ENTRIES_FOR_REUSE = 12000;
    protected static final int MAX_TABLE_SIZE = 65536;
    static final CharsToNameCanonicalizer sBootstrapSymbolTable = new CharsToNameCanonicalizer();
    protected Bucket[] _buckets;
    protected final boolean _canonicalize;
    protected boolean _dirty;
    private final int _hashSeed;
    protected int _indexMask;
    protected final boolean _intern;
    protected int _longestCollisionList;
    protected CharsToNameCanonicalizer _parent;
    protected int _size;
    protected int _sizeThreshold;
    protected String[] _symbols;

    static final class Bucket {
        private final int _length;
        private final Bucket _next;
        private final String _symbol;

        public Bucket(String str, Bucket bucket) {
            this._symbol = str;
            this._next = bucket;
            this._length = bucket == null ? 1 : bucket._length + 1;
        }

        public String getSymbol() {
            return this._symbol;
        }

        public Bucket getNext() {
            return this._next;
        }

        public int length() {
            return this._length;
        }

        public String find(char[] cArr, int i, int i2) {
            String str = this._symbol;
            Bucket bucket = this._next;
            while (true) {
                if (str.length() == i2) {
                    int i3 = 0;
                    while (str.charAt(i3) == cArr[i + i3]) {
                        i3++;
                        if (i3 >= i2) {
                            break;
                        }
                    }
                    if (i3 == i2) {
                        return str;
                    }
                }
                if (bucket == null) {
                    return null;
                }
                str = bucket.getSymbol();
                bucket = bucket.getNext();
            }
        }
    }

    public static CharsToNameCanonicalizer createRoot() {
        long currentTimeMillis = System.currentTimeMillis();
        return createRoot((((int) (currentTimeMillis >>> 32)) + ((int) currentTimeMillis)) | 1);
    }

    protected static CharsToNameCanonicalizer createRoot(int i) {
        return sBootstrapSymbolTable.makeOrphan(i);
    }

    private CharsToNameCanonicalizer() {
        this._canonicalize = true;
        this._intern = true;
        this._dirty = true;
        this._hashSeed = 0;
        this._longestCollisionList = 0;
        initTables(DEFAULT_TABLE_SIZE);
    }

    private void initTables(int i) {
        this._symbols = new String[i];
        this._buckets = new Bucket[(i >> 1)];
        this._indexMask = i - 1;
        this._size = 0;
        this._longestCollisionList = 0;
        this._sizeThreshold = _thresholdSize(i);
    }

    private static int _thresholdSize(int i) {
        return i - (i >> 2);
    }

    private CharsToNameCanonicalizer(CharsToNameCanonicalizer charsToNameCanonicalizer, boolean z, boolean z2, String[] strArr, Bucket[] bucketArr, int i, int i2, int i3) {
        this._parent = charsToNameCanonicalizer;
        this._canonicalize = z;
        this._intern = z2;
        this._symbols = strArr;
        this._buckets = bucketArr;
        this._size = i;
        this._hashSeed = i2;
        int length = strArr.length;
        this._sizeThreshold = _thresholdSize(length);
        this._indexMask = length - 1;
        this._longestCollisionList = i3;
        this._dirty = false;
    }

    public CharsToNameCanonicalizer makeChild(boolean z, boolean z2) {
        String[] strArr;
        Bucket[] bucketArr;
        int i;
        int i2;
        int i3;
        synchronized (this) {
            strArr = this._symbols;
            bucketArr = this._buckets;
            i = this._size;
            i2 = this._hashSeed;
            i3 = this._longestCollisionList;
        }
        return new CharsToNameCanonicalizer(this, z, z2, strArr, bucketArr, i, i2, i3);
    }

    private CharsToNameCanonicalizer makeOrphan(int i) {
        return new CharsToNameCanonicalizer(null, true, true, this._symbols, this._buckets, this._size, i, this._longestCollisionList);
    }

    private void mergeChild(CharsToNameCanonicalizer charsToNameCanonicalizer) {
        if (charsToNameCanonicalizer.size() > MAX_ENTRIES_FOR_REUSE || charsToNameCanonicalizer._longestCollisionList > MAX_COLL_CHAIN_FOR_REUSE) {
            synchronized (this) {
                initTables(DEFAULT_TABLE_SIZE);
                this._dirty = false;
            }
        } else if (charsToNameCanonicalizer.size() > size()) {
            synchronized (this) {
                this._symbols = charsToNameCanonicalizer._symbols;
                this._buckets = charsToNameCanonicalizer._buckets;
                this._size = charsToNameCanonicalizer._size;
                this._sizeThreshold = charsToNameCanonicalizer._sizeThreshold;
                this._indexMask = charsToNameCanonicalizer._indexMask;
                this._longestCollisionList = charsToNameCanonicalizer._longestCollisionList;
                this._dirty = false;
            }
        }
    }

    public void release() {
        if (maybeDirty() && this._parent != null) {
            this._parent.mergeChild(this);
            this._dirty = false;
        }
    }

    public int size() {
        return this._size;
    }

    public int bucketCount() {
        return this._symbols.length;
    }

    public boolean maybeDirty() {
        return this._dirty;
    }

    public int hashSeed() {
        return this._hashSeed;
    }

    public int collisionCount() {
        int i = 0;
        for (Bucket bucket : this._buckets) {
            if (bucket != null) {
                i += bucket.length();
            }
        }
        return i;
    }

    public int maxCollisionLength() {
        return this._longestCollisionList;
    }

    public String findSymbol(char[] cArr, int i, int i2, int i3) {
        if (i2 < 1) {
            return BuildConfig.FLAVOR;
        }
        if (!this._canonicalize) {
            return new String(cArr, i, i2);
        }
        int i4;
        int _hashToIndex = _hashToIndex(i3);
        String str = this._symbols[_hashToIndex];
        if (str != null) {
            if (str.length() == i2) {
                i4 = 0;
                while (str.charAt(i4) == cArr[i + i4]) {
                    i4++;
                    if (i4 >= i2) {
                        break;
                    }
                }
                if (i4 == i2) {
                    return str;
                }
            }
            Bucket bucket = this._buckets[_hashToIndex >> 1];
            if (bucket != null) {
                String find = bucket.find(cArr, i, i2);
                if (find != null) {
                    return find;
                }
            }
        }
        if (!this._dirty) {
            copyArrays();
            this._dirty = true;
            i4 = _hashToIndex;
        } else if (this._size >= this._sizeThreshold) {
            rehash();
            i4 = _hashToIndex(calcHash(cArr, i, i2));
        } else {
            i4 = _hashToIndex;
        }
        String str2 = new String(cArr, i, i2);
        if (this._intern) {
            str2 = InternCache.instance.intern(str2);
        }
        this._size++;
        if (this._symbols[i4] == null) {
            this._symbols[i4] = str2;
            return str2;
        }
        i4 >>= 1;
        Bucket bucket2 = new Bucket(str2, this._buckets[i4]);
        this._buckets[i4] = bucket2;
        this._longestCollisionList = Math.max(bucket2.length(), this._longestCollisionList);
        if (this._longestCollisionList <= MAX_COLL_CHAIN_LENGTH) {
            return str2;
        }
        reportTooManyCollisions(MAX_COLL_CHAIN_LENGTH);
        return str2;
    }

    public int _hashToIndex(int i) {
        return ((i >>> 15) + i) & this._indexMask;
    }

    public int calcHash(char[] cArr, int i, int i2) {
        int i3 = this._hashSeed;
        int i4 = 0;
        while (i4 < i2) {
            int i5 = cArr[i4] + (i3 * HASH_MULT);
            i4++;
            i3 = i5;
        }
        return i3 == 0 ? 1 : i3;
    }

    public int calcHash(String str) {
        int length = str.length();
        int i = this._hashSeed;
        int i2 = 0;
        while (i2 < length) {
            int charAt = str.charAt(i2) + (i * HASH_MULT);
            i2++;
            i = charAt;
        }
        return i == 0 ? 1 : i;
    }

    private void copyArrays() {
        Object obj = this._symbols;
        int length = obj.length;
        this._symbols = new String[length];
        System.arraycopy(obj, 0, this._symbols, 0, length);
        obj = this._buckets;
        length = obj.length;
        this._buckets = new Bucket[length];
        System.arraycopy(obj, 0, this._buckets, 0, length);
    }

    private void rehash() {
        int length = this._symbols.length;
        int i = length + length;
        if (i > MAX_TABLE_SIZE) {
            this._size = 0;
            Arrays.fill(this._symbols, null);
            Arrays.fill(this._buckets, null);
            this._dirty = true;
            return;
        }
        int i2;
        String[] strArr = this._symbols;
        Bucket[] bucketArr = this._buckets;
        this._symbols = new String[i];
        this._buckets = new Bucket[(i >> 1)];
        this._indexMask = i - 1;
        this._sizeThreshold = _thresholdSize(i);
        i = 0;
        int i3 = 0;
        for (i2 = 0; i2 < length; i2++) {
            String str = strArr[i2];
            if (str != null) {
                i3++;
                int _hashToIndex = _hashToIndex(calcHash(str));
                if (this._symbols[_hashToIndex] == null) {
                    this._symbols[_hashToIndex] = str;
                } else {
                    _hashToIndex >>= 1;
                    Bucket bucket = new Bucket(str, this._buckets[_hashToIndex]);
                    this._buckets[_hashToIndex] = bucket;
                    i = Math.max(i, bucket.length());
                }
            }
        }
        length >>= 1;
        i2 = 0;
        int i4 = i3;
        i3 = i;
        while (i2 < length) {
            i = i3;
            for (Bucket bucket2 = bucketArr[i2]; bucket2 != null; bucket2 = bucket2.getNext()) {
                i4++;
                String symbol = bucket2.getSymbol();
                int _hashToIndex2 = _hashToIndex(calcHash(symbol));
                if (this._symbols[_hashToIndex2] == null) {
                    this._symbols[_hashToIndex2] = symbol;
                } else {
                    _hashToIndex2 >>= 1;
                    Bucket bucket3 = new Bucket(symbol, this._buckets[_hashToIndex2]);
                    this._buckets[_hashToIndex2] = bucket3;
                    i = Math.max(i, bucket3.length());
                }
            }
            i2++;
            i3 = i;
        }
        this._longestCollisionList = i3;
        if (i4 != this._size) {
            throw new Error("Internal error on SymbolTable.rehash(): had " + this._size + " entries; now have " + i4 + ".");
        }
    }

    protected void reportTooManyCollisions(int i) {
        throw new IllegalStateException("Longest collision chain in symbol table (of size " + this._size + ") now exceeds maximum, " + i + " -- suspect a DoS attack based on hash collisions");
    }
}
