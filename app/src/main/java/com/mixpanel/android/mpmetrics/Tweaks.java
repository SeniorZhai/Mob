package com.mixpanel.android.mpmetrics;

import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tweaks {
    public static final int BOOLEAN_TYPE = 1;
    public static final int DOUBLE_TYPE = 2;
    private static final String LOGTAG = "MixpanelAPI.Tweaks";
    public static final int LONG_TYPE = 3;
    public static final int STRING_TYPE = 4;
    private final List<OnTweakDeclaredListener> mTweakDeclaredListeners = new ArrayList();
    private final Map<String, TweakValue> mTweakValues = new HashMap();

    public interface OnTweakDeclaredListener {
        void onTweakDeclared();
    }

    public static class TweakValue {
        private final Object defaultValue;
        private final Number maximum;
        private final Number minimum;
        public final int type;
        private final Object value;

        private TweakValue(int aType, Object aDefaultValue, Number aMin, Number aMax, Object value) {
            this.type = aType;
            this.defaultValue = aDefaultValue;
            this.minimum = aMin;
            this.maximum = aMax;
            this.value = value;
        }

        public TweakValue updateValue(Object newValue) {
            return new TweakValue(this.type, this.defaultValue, this.minimum, this.maximum, newValue);
        }

        public String getStringValue() {
            String ret = null;
            try {
                ret = (String) this.defaultValue;
            } catch (ClassCastException e) {
            }
            try {
                return (String) this.value;
            } catch (ClassCastException e2) {
                return ret;
            }
        }

        public Number getNumberValue() {
            Number ret = Integer.valueOf(0);
            if (this.defaultValue != null) {
                try {
                    ret = (Number) this.defaultValue;
                } catch (ClassCastException e) {
                }
            }
            if (this.value == null) {
                return ret;
            }
            try {
                return (Number) this.value;
            } catch (ClassCastException e2) {
                return ret;
            }
        }

        public Boolean getBooleanValue() {
            Boolean ret = Boolean.valueOf(false);
            if (this.defaultValue != null) {
                try {
                    ret = (Boolean) this.defaultValue;
                } catch (ClassCastException e) {
                }
            }
            if (this.value == null) {
                return ret;
            }
            try {
                return (Boolean) this.value;
            } catch (ClassCastException e2) {
                return ret;
            }
        }
    }

    public synchronized void addOnTweakDeclaredListener(OnTweakDeclaredListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener cannot be null");
        }
        this.mTweakDeclaredListeners.add(listener);
    }

    public synchronized void set(String tweakName, Object value) {
        if (this.mTweakValues.containsKey(tweakName)) {
            this.mTweakValues.put(tweakName, ((TweakValue) this.mTweakValues.get(tweakName)).updateValue(value));
        } else {
            Log.w(LOGTAG, "Attempt to set a tweak \"" + tweakName + "\" which has never been defined.");
        }
    }

    public synchronized Map<String, TweakValue> getAllValues() {
        return new HashMap(this.mTweakValues);
    }

    Tweaks() {
    }

    Tweak<String> stringTweak(final String tweakName, String defaultValue) {
        declareTweak(tweakName, defaultValue, STRING_TYPE);
        return new Tweak<String>() {
            public String get() {
                return Tweaks.this.getValue(tweakName).getStringValue();
            }
        };
    }

    Tweak<Double> doubleTweak(final String tweakName, double defaultValue) {
        declareTweak(tweakName, Double.valueOf(defaultValue), DOUBLE_TYPE);
        return new Tweak<Double>() {
            public Double get() {
                return Double.valueOf(Tweaks.this.getValue(tweakName).getNumberValue().doubleValue());
            }
        };
    }

    Tweak<Float> floatTweak(final String tweakName, float defaultValue) {
        declareTweak(tweakName, Float.valueOf(defaultValue), DOUBLE_TYPE);
        return new Tweak<Float>() {
            public Float get() {
                return Float.valueOf(Tweaks.this.getValue(tweakName).getNumberValue().floatValue());
            }
        };
    }

    Tweak<Long> longTweak(final String tweakName, long defaultValue) {
        declareTweak(tweakName, Long.valueOf(defaultValue), LONG_TYPE);
        return new Tweak<Long>() {
            public Long get() {
                return Long.valueOf(Tweaks.this.getValue(tweakName).getNumberValue().longValue());
            }
        };
    }

    Tweak<Integer> intTweak(final String tweakName, int defaultValue) {
        declareTweak(tweakName, Integer.valueOf(defaultValue), LONG_TYPE);
        return new Tweak<Integer>() {
            public Integer get() {
                return Integer.valueOf(Tweaks.this.getValue(tweakName).getNumberValue().intValue());
            }
        };
    }

    Tweak<Byte> byteTweak(final String tweakName, byte defaultValue) {
        declareTweak(tweakName, Byte.valueOf(defaultValue), LONG_TYPE);
        return new Tweak<Byte>() {
            public Byte get() {
                return Byte.valueOf(Tweaks.this.getValue(tweakName).getNumberValue().byteValue());
            }
        };
    }

    Tweak<Short> shortTweak(final String tweakName, short defaultValue) {
        declareTweak(tweakName, Short.valueOf(defaultValue), LONG_TYPE);
        return new Tweak<Short>() {
            public Short get() {
                return Short.valueOf(Tweaks.this.getValue(tweakName).getNumberValue().shortValue());
            }
        };
    }

    Tweak<Boolean> booleanTweak(final String tweakName, boolean defaultValue) {
        declareTweak(tweakName, Boolean.valueOf(defaultValue), BOOLEAN_TYPE);
        return new Tweak<Boolean>() {
            public Boolean get() {
                return Tweaks.this.getValue(tweakName).getBooleanValue();
            }
        };
    }

    private synchronized TweakValue getValue(String tweakName) {
        return (TweakValue) this.mTweakValues.get(tweakName);
    }

    private void declareTweak(String tweakName, Object defaultValue, int tweakType) {
        if (this.mTweakValues.containsKey(tweakName)) {
            Log.w(LOGTAG, "Attempt to define a tweak \"" + tweakName + "\" twice with the same name");
            return;
        }
        this.mTweakValues.put(tweakName, new TweakValue(tweakType, defaultValue, null, null, defaultValue));
        int listenerSize = this.mTweakDeclaredListeners.size();
        for (int i = 0; i < listenerSize; i += BOOLEAN_TYPE) {
            ((OnTweakDeclaredListener) this.mTweakDeclaredListeners.get(i)).onTweakDeclared();
        }
    }
}
