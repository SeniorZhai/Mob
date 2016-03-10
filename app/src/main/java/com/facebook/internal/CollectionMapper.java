package com.facebook.internal;

import com.facebook.FacebookException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CollectionMapper {

    public interface OnErrorListener {
        void onError(FacebookException facebookException);
    }

    public interface OnMapperCompleteListener extends OnErrorListener {
        void onComplete();
    }

    public interface OnMapValueCompleteListener extends OnErrorListener {
        void onComplete(Object obj);
    }

    public interface Collection<T> {
        Object get(T t);

        Iterator<T> keyIterator();

        void set(T t, Object obj, OnErrorListener onErrorListener);
    }

    public interface ValueMapper {
        void mapValue(Object obj, OnMapValueCompleteListener onMapValueCompleteListener);
    }

    public static <T> void iterate(final Collection<T> collection, ValueMapper valueMapper, final OnMapperCompleteListener onMapperCompleteListener) {
        final Mutable<Boolean> didReturnError = new Mutable(Boolean.valueOf(false));
        final Mutable<Integer> pendingJobCount = new Mutable(Integer.valueOf(1));
        final OnMapperCompleteListener jobCompleteListener = new OnMapperCompleteListener() {
            public void onComplete() {
                if (!((Boolean) didReturnError.value).booleanValue()) {
                    Mutable mutable = pendingJobCount;
                    Integer valueOf = Integer.valueOf(((Integer) pendingJobCount.value).intValue() - 1);
                    mutable.value = valueOf;
                    if (valueOf.intValue() == 0) {
                        onMapperCompleteListener.onComplete();
                    }
                }
            }

            public void onError(FacebookException exception) {
                if (!((Boolean) didReturnError.value).booleanValue()) {
                    didReturnError.value = Boolean.valueOf(true);
                    onMapperCompleteListener.onError(exception);
                }
            }
        };
        Iterator<T> keyIterator = collection.keyIterator();
        List<T> keys = new LinkedList();
        while (keyIterator.hasNext()) {
            keys.add(keyIterator.next());
        }
        for (final T key : keys) {
            Object value = collection.get(key);
            OnMapValueCompleteListener onMapValueCompleteListener = new OnMapValueCompleteListener() {
                public void onComplete(Object mappedValue) {
                    collection.set(key, mappedValue, jobCompleteListener);
                    jobCompleteListener.onComplete();
                }

                public void onError(FacebookException exception) {
                    jobCompleteListener.onError(exception);
                }
            };
            Integer num = (Integer) pendingJobCount.value;
            pendingJobCount.value = Integer.valueOf(((Integer) pendingJobCount.value).intValue() + 1);
            valueMapper.mapValue(value, onMapValueCompleteListener);
        }
        jobCompleteListener.onComplete();
    }

    private CollectionMapper() {
    }
}
