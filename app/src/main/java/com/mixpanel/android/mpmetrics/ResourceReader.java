package com.mixpanel.android.mpmetrics;

import android.R.drawable;
import android.R.id;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import io.fabric.sdk.android.services.common.AbstractSpiCall;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class ResourceReader implements ResourceIds {
    private static final String LOGTAG = "MixpanelAPI.RsrcReader";
    private final Context mContext;
    private final Map<String, Integer> mIdNameToId = new HashMap();
    private final SparseArray<String> mIdToIdName = new SparseArray();

    public static class Drawables extends ResourceReader {
        private final String mResourcePackageName;

        protected Drawables(String resourcePackageName, Context context) {
            super(context);
            this.mResourcePackageName = resourcePackageName;
            initialize();
        }

        protected Class<?> getSystemClass() {
            return drawable.class;
        }

        protected String getLocalClassName(Context context) {
            return this.mResourcePackageName + ".R$drawable";
        }
    }

    public static class Ids extends ResourceReader {
        private final String mResourcePackageName;

        public Ids(String resourcePackageName, Context context) {
            super(context);
            this.mResourcePackageName = resourcePackageName;
            initialize();
        }

        protected Class<?> getSystemClass() {
            return id.class;
        }

        protected String getLocalClassName(Context context) {
            return this.mResourcePackageName + ".R$id";
        }
    }

    protected abstract String getLocalClassName(Context context);

    protected abstract Class<?> getSystemClass();

    protected ResourceReader(Context context) {
        this.mContext = context;
    }

    public boolean knownIdName(String name) {
        return this.mIdNameToId.containsKey(name);
    }

    public int idFromName(String name) {
        return ((Integer) this.mIdNameToId.get(name)).intValue();
    }

    public String nameForId(int id) {
        return (String) this.mIdToIdName.get(id);
    }

    private static void readClassIds(Class<?> platformIdClass, String namespace, Map<String, Integer> namesToIds) {
        try {
            Field[] fields = platformIdClass.getFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == Integer.TYPE) {
                    String namespacedName;
                    String name = field.getName();
                    int value = field.getInt(null);
                    if (namespace == null) {
                        namespacedName = name;
                    } else {
                        namespacedName = namespace + ":" + name;
                    }
                    namesToIds.put(namespacedName, Integer.valueOf(value));
                }
            }
        } catch (IllegalAccessException e) {
            Log.e(LOGTAG, "Can't read built-in id names from " + platformIdClass.getName(), e);
        }
    }

    protected void initialize() {
        this.mIdNameToId.clear();
        this.mIdToIdName.clear();
        readClassIds(getSystemClass(), AbstractSpiCall.ANDROID_CLIENT_TYPE, this.mIdNameToId);
        String localClassName = getLocalClassName(this.mContext);
        try {
            readClassIds(Class.forName(localClassName), null, this.mIdNameToId);
        } catch (ClassNotFoundException e) {
            Log.w(LOGTAG, "Can't load names for Android view ids from '" + localClassName + "', ids by name will not be available in the events editor.");
            Log.i(LOGTAG, "You may be missing a Resources class for your package due to your proguard configuration, or you may be using an applicationId in your build that isn't the same as the package declared in your AndroidManifest.xml file.\nIf you're using proguard, you can fix this issue by adding the following to your proguard configuration:\n\n-keep class **.R$* {\n    <fields>;\n}\n\nIf you're not using proguard, or if your proguard configuration already contains the directive above, you can add the following to your AndroidManifest.xml file to explicitly point the Mixpanel library to the appropriate library for your resources class:\n\n<meta-data android:name=\"com.mixpanel.android.MPConfig.ResourcePackageName\" android:value=\"YOUR_PACKAGE_NAME\" />\n\nwhere YOUR_PACKAGE_NAME is the same string you use for the \"package\" attribute in your <manifest> tag.");
        }
        for (Entry<String, Integer> idMapping : this.mIdNameToId.entrySet()) {
            this.mIdToIdName.put(((Integer) idMapping.getValue()).intValue(), idMapping.getKey());
        }
    }
}
