package com.mixpanel.android.viewcrawler;

class PropertyDescription {
    public final Caller accessor;
    private final String mMutatorName;
    public final String name;
    public final Class<?> targetClass;

    public PropertyDescription(String name, Class<?> targetClass, Caller accessor, String mutatorName) {
        this.name = name;
        this.targetClass = targetClass;
        this.accessor = accessor;
        this.mMutatorName = mutatorName;
    }

    public Caller makeMutator(Object[] methodArgs) throws NoSuchMethodException {
        if (this.mMutatorName == null) {
            return null;
        }
        return new Caller(this.targetClass, this.mMutatorName, methodArgs, Void.TYPE);
    }

    public String toString() {
        return "[PropertyDescription " + this.name + "," + this.targetClass + ", " + this.accessor + "/" + this.mMutatorName + "]";
    }
}
