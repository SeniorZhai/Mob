package com.facebook.share.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public abstract class ShareOpenGraphValueContainer<P extends ShareOpenGraphValueContainer, E extends Builder> implements ShareModel {
    private final Bundle bundle;

    public static abstract class Builder<P extends ShareOpenGraphValueContainer, E extends Builder> implements ShareModelBuilder<P, E> {
        private Bundle bundle = new Bundle();

        public E putBoolean(String key, boolean value) {
            this.bundle.putBoolean(key, value);
            return this;
        }

        public E putBooleanArray(String key, @Nullable boolean[] value) {
            this.bundle.putBooleanArray(key, value);
            return this;
        }

        public E putDouble(String key, double value) {
            this.bundle.putDouble(key, value);
            return this;
        }

        public E putDoubleArray(String key, @Nullable double[] value) {
            this.bundle.putDoubleArray(key, value);
            return this;
        }

        public E putInt(String key, int value) {
            this.bundle.putInt(key, value);
            return this;
        }

        public E putIntArray(String key, @Nullable int[] value) {
            this.bundle.putIntArray(key, value);
            return this;
        }

        public E putLong(String key, long value) {
            this.bundle.putLong(key, value);
            return this;
        }

        public E putLongArray(String key, @Nullable long[] value) {
            this.bundle.putLongArray(key, value);
            return this;
        }

        public E putObject(String key, @Nullable ShareOpenGraphObject value) {
            this.bundle.putParcelable(key, value);
            return this;
        }

        public E putObjectArrayList(String key, @Nullable ArrayList<ShareOpenGraphObject> value) {
            this.bundle.putParcelableArrayList(key, value);
            return this;
        }

        public E putPhoto(String key, @Nullable SharePhoto value) {
            this.bundle.putParcelable(key, value);
            return this;
        }

        public E putPhotoArrayList(String key, @Nullable ArrayList<SharePhoto> value) {
            this.bundle.putParcelableArrayList(key, value);
            return this;
        }

        public E putString(String key, @Nullable String value) {
            this.bundle.putString(key, value);
            return this;
        }

        public E putStringArrayList(String key, @Nullable ArrayList<String> value) {
            this.bundle.putStringArrayList(key, value);
            return this;
        }

        public E readFrom(P model) {
            if (model != null) {
                this.bundle.putAll(model.getBundle());
            }
            return this;
        }
    }

    protected ShareOpenGraphValueContainer(Builder<P, E> builder) {
        this.bundle = (Bundle) builder.bundle.clone();
    }

    ShareOpenGraphValueContainer(Parcel in) {
        this.bundle = in.readBundle(Builder.class.getClassLoader());
    }

    @Nullable
    public Object get(String key) {
        return this.bundle.get(key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return this.bundle.getBoolean(key, defaultValue);
    }

    @Nullable
    public boolean[] getBooleanArray(String key) {
        return this.bundle.getBooleanArray(key);
    }

    public double getDouble(String key, double defaultValue) {
        return this.bundle.getDouble(key, defaultValue);
    }

    @Nullable
    public double[] getDoubleArray(String key) {
        return this.bundle.getDoubleArray(key);
    }

    public int getInt(String key, int defaultValue) {
        return this.bundle.getInt(key, defaultValue);
    }

    @Nullable
    public int[] getIntArray(String key) {
        return this.bundle.getIntArray(key);
    }

    public long getLong(String key, long defaultValue) {
        return this.bundle.getLong(key, defaultValue);
    }

    @Nullable
    public long[] getLongArray(String key) {
        return this.bundle.getLongArray(key);
    }

    public ShareOpenGraphObject getObject(String key) {
        Object value = this.bundle.get(key);
        return value instanceof ShareOpenGraphObject ? (ShareOpenGraphObject) value : null;
    }

    @Nullable
    public ArrayList<ShareOpenGraphObject> getObjectArrayList(String key) {
        ArrayList<Parcelable> items = this.bundle.getParcelableArrayList(key);
        if (items == null) {
            return null;
        }
        ArrayList<ShareOpenGraphObject> list = new ArrayList();
        Iterator i$ = items.iterator();
        while (i$.hasNext()) {
            Parcelable item = (Parcelable) i$.next();
            if (item instanceof ShareOpenGraphObject) {
                list.add((ShareOpenGraphObject) item);
            }
        }
        return list;
    }

    @Nullable
    public SharePhoto getPhoto(String key) {
        Parcelable value = this.bundle.getParcelable(key);
        return value instanceof SharePhoto ? (SharePhoto) value : null;
    }

    @Nullable
    public ArrayList<SharePhoto> getPhotoArrayList(String key) {
        ArrayList<Parcelable> items = this.bundle.getParcelableArrayList(key);
        if (items == null) {
            return null;
        }
        ArrayList<SharePhoto> list = new ArrayList();
        Iterator i$ = items.iterator();
        while (i$.hasNext()) {
            Parcelable item = (Parcelable) i$.next();
            if (item instanceof SharePhoto) {
                list.add((SharePhoto) item);
            }
        }
        return list;
    }

    @Nullable
    public String getString(String key) {
        return this.bundle.getString(key);
    }

    @Nullable
    public ArrayList<String> getStringArrayList(String key) {
        return this.bundle.getStringArrayList(key);
    }

    public Bundle getBundle() {
        return (Bundle) this.bundle.clone();
    }

    public Set<String> keySet() {
        return this.bundle.keySet();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeBundle(this.bundle);
    }
}
