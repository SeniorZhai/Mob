package com.mixpanel.android.viewcrawler;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Pair;
import bolts.MeasurementEvent;
import com.google.android.gms.analytics.ecommerce.Promotion;
import com.helpshift.HSFunnel;
import com.helpshift.constants.MessageColumns;
import com.helpshift.res.values.HSConsts;
import com.helpshift.storage.ProfilesDBHelper;
import com.mixpanel.android.mpmetrics.ResourceIds;
import com.mixpanel.android.util.JSONUtils;
import com.mixpanel.android.viewcrawler.ImageStore.CantGetImageException;
import com.mixpanel.android.viewcrawler.Pathfinder.PathElement;
import com.mixpanel.android.viewcrawler.ViewVisitor.AddAccessibilityEventVisitor;
import com.mixpanel.android.viewcrawler.ViewVisitor.AddTextChangeListener;
import com.mixpanel.android.viewcrawler.ViewVisitor.LayoutRule;
import com.mixpanel.android.viewcrawler.ViewVisitor.LayoutUpdateVisitor;
import com.mixpanel.android.viewcrawler.ViewVisitor.OnEventListener;
import com.mixpanel.android.viewcrawler.ViewVisitor.OnLayoutErrorListener;
import com.mixpanel.android.viewcrawler.ViewVisitor.PropertySetVisitor;
import com.mixpanel.android.viewcrawler.ViewVisitor.ViewDetectorVisitor;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import com.mobcrush.mobcrush.network.Network;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class EditProtocol {
    private static final String LOGTAG = "MixpanelAPI.EProtocol";
    private static final List<PathElement> NEVER_MATCH_PATH = Collections.emptyList();
    private static final Class<?>[] NO_PARAMS = new Class[0];
    private final ImageStore mImageStore;
    private final OnLayoutErrorListener mLayoutErrorListener;
    private final ResourceIds mResourceIds;

    public static class BadInstructionsException extends Exception {
        private static final long serialVersionUID = -4062004792184145311L;

        public BadInstructionsException(String message) {
            super(message);
        }

        public BadInstructionsException(String message, Throwable e) {
            super(message, e);
        }
    }

    public static class CantGetEditAssetsException extends Exception {
        public CantGetEditAssetsException(String message) {
            super(message);
        }

        public CantGetEditAssetsException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class Edit {
        public final List<String> imageUrls;
        public final ViewVisitor visitor;

        private Edit(ViewVisitor aVisitor, List<String> someUrls) {
            this.visitor = aVisitor;
            this.imageUrls = someUrls;
        }
    }

    public static class InapplicableInstructionsException extends BadInstructionsException {
        private static final long serialVersionUID = 3977056710817909104L;

        public InapplicableInstructionsException(String message) {
            super(message);
        }
    }

    public EditProtocol(ResourceIds resourceIds, ImageStore imageStore, OnLayoutErrorListener layoutErrorListener) {
        this.mResourceIds = resourceIds;
        this.mImageStore = imageStore;
        this.mLayoutErrorListener = layoutErrorListener;
    }

    public ViewVisitor readEventBinding(JSONObject source, OnEventListener listener) throws BadInstructionsException {
        try {
            String eventName = source.getString(MeasurementEvent.MEASUREMENT_EVENT_NAME_KEY);
            String eventType = source.getString("event_type");
            List<PathElement> path = readPath(source.getJSONArray("path"), this.mResourceIds);
            if (path.size() == 0) {
                throw new InapplicableInstructionsException("event '" + eventName + "' will not be bound to any element in the UI.");
            } else if (Promotion.ACTION_CLICK.equals(eventType)) {
                return new AddAccessibilityEventVisitor(path, 1, eventName, listener);
            } else {
                if ("selected".equals(eventType)) {
                    return new AddAccessibilityEventVisitor(path, 4, eventName, listener);
                }
                if ("text_changed".equals(eventType)) {
                    return new AddTextChangeListener(path, eventName, listener);
                }
                if ("detected".equals(eventType)) {
                    return new ViewDetectorVisitor(path, eventName, listener);
                }
                throw new BadInstructionsException("Mixpanel can't track event type \"" + eventType + "\"");
            }
        } catch (JSONException e) {
            throw new BadInstructionsException("Can't interpret instructions due to JSONException", e);
        }
    }

    public Edit readEdit(JSONObject source) throws BadInstructionsException, CantGetEditAssetsException {
        List<String> assetsLoaded = new ArrayList();
        String targetClassName;
        try {
            List<PathElement> path = readPath(source.getJSONArray("path"), this.mResourceIds);
            if (path.size() == 0) {
                throw new InapplicableInstructionsException("Edit will not be bound to any element in the UI.");
            }
            int i;
            ViewVisitor propertySetVisitor;
            if (source.getString("change_type").equals("property")) {
                targetClassName = source.getJSONObject("property").getString("classname");
                if (targetClassName == null) {
                    throw new BadInstructionsException("Can't bind an edit property without a target class");
                }
                Class<?> targetClass = Class.forName(targetClassName);
                PropertyDescription prop = readPropertyDescription(targetClass, source.getJSONObject("property"));
                JSONArray argsAndTypes = source.getJSONArray("args");
                Object[] methodArgs = new Object[argsAndTypes.length()];
                for (i = 0; i < argsAndTypes.length(); i++) {
                    JSONArray argPlusType = argsAndTypes.getJSONArray(i);
                    methodArgs[i] = convertArgument(argPlusType.get(0), argPlusType.getString(1), assetsLoaded);
                }
                Caller mutator = prop.makeMutator(methodArgs);
                if (mutator == null) {
                    throw new BadInstructionsException("Can't update a read-only property " + prop.name + " (add a mutator to make this work)");
                }
                propertySetVisitor = new PropertySetVisitor(path, mutator, prop.accessor);
            } else if (source.getString("change_type").equals(TtmlNode.TAG_LAYOUT)) {
                JSONArray args = source.getJSONArray("args");
                ArrayList<LayoutRule> newParams = new ArrayList();
                int length = args.length();
                for (i = 0; i < length; i++) {
                    Integer anchor_id;
                    JSONObject layout_info = args.optJSONObject(i);
                    String view_id_name = layout_info.getString("view_id_name");
                    String anchor_id_name = layout_info.getString("anchor_id_name");
                    Integer view_id = reconcileIds(-1, view_id_name, this.mResourceIds);
                    if (anchor_id_name.equals(HSConsts.STATUS_NEW)) {
                        anchor_id = Integer.valueOf(0);
                    } else if (anchor_id_name.equals("-1")) {
                        anchor_id = Integer.valueOf(-1);
                    } else {
                        anchor_id = reconcileIds(-1, anchor_id_name, this.mResourceIds);
                    }
                    if (view_id == null || anchor_id == null) {
                        Log.w(LOGTAG, "View (" + view_id_name + ") or anchor (" + anchor_id_name + ") not found.");
                    } else {
                        newParams.add(new LayoutRule(view_id.intValue(), layout_info.getInt("verb"), anchor_id.intValue()));
                    }
                }
                propertySetVisitor = new LayoutUpdateVisitor(path, newParams, source.getString(ProfilesDBHelper.COLUMN_NAME), this.mLayoutErrorListener);
            } else {
                throw new BadInstructionsException("Can't figure out the edit type");
            }
            return new Edit(visitor, assetsLoaded);
        } catch (ClassNotFoundException e) {
            throw new BadInstructionsException("Can't find class for visit path: " + targetClassName, e);
        } catch (NoSuchMethodException e2) {
            throw new BadInstructionsException("Can't create property mutator", e2);
        } catch (JSONException e3) {
            throw new BadInstructionsException("Can't interpret instructions due to JSONException", e3);
        }
    }

    public ViewSnapshot readSnapshotConfig(JSONObject source) throws BadInstructionsException {
        List<PropertyDescription> properties = new ArrayList();
        try {
            JSONArray classes = source.getJSONObject(Network.CONFIG).getJSONArray("classes");
            for (int classIx = 0; classIx < classes.length(); classIx++) {
                JSONObject classDesc = classes.getJSONObject(classIx);
                Class<?> targetClass = Class.forName(classDesc.getString(ProfilesDBHelper.COLUMN_NAME));
                JSONArray propertyDescs = classDesc.getJSONArray("properties");
                for (int i = 0; i < propertyDescs.length(); i++) {
                    properties.add(readPropertyDescription(targetClass, propertyDescs.getJSONObject(i)));
                }
            }
            return new ViewSnapshot(properties, this.mResourceIds);
        } catch (JSONException e) {
            throw new BadInstructionsException("Can't read snapshot configuration", e);
        } catch (ClassNotFoundException e2) {
            throw new BadInstructionsException("Can't resolve types for snapshot configuration", e2);
        }
    }

    public Pair<String, Object> readTweak(JSONObject tweakDesc) throws BadInstructionsException {
        try {
            Object valueOf;
            String tweakName = tweakDesc.getString(ProfilesDBHelper.COLUMN_NAME);
            String type = tweakDesc.getString(MessageColumns.TYPE);
            if ("number".equals(type)) {
                String encoding = tweakDesc.getString("encoding");
                if (HSFunnel.LIBRARY_OPENED_DECOMP.equals(encoding)) {
                    valueOf = Double.valueOf(tweakDesc.getDouble("value"));
                } else if (HSFunnel.SUPPORT_LAUNCH.equals(encoding)) {
                    valueOf = Long.valueOf(tweakDesc.getLong("value"));
                } else {
                    throw new BadInstructionsException("number must have encoding of type \"l\" for long or \"d\" for double in: " + tweakDesc);
                }
            } else if ("boolean".equals(type)) {
                valueOf = Boolean.valueOf(tweakDesc.getBoolean("value"));
            } else if ("string".equals(type)) {
                valueOf = tweakDesc.getString("value");
            } else {
                throw new BadInstructionsException("Unrecognized tweak type " + type + " in: " + tweakDesc);
            }
            return new Pair(tweakName, valueOf);
        } catch (JSONException e) {
            throw new BadInstructionsException("Can't read tweak update", e);
        }
    }

    List<PathElement> readPath(JSONArray pathDesc, ResourceIds idNameToId) throws JSONException {
        List<PathElement> path = new ArrayList();
        for (int i = 0; i < pathDesc.length(); i++) {
            int prefix;
            JSONObject targetView = pathDesc.getJSONObject(i);
            String prefixCode = JSONUtils.optionalStringKey(targetView, "prefix");
            String targetViewClass = JSONUtils.optionalStringKey(targetView, "view_class");
            int targetIndex = targetView.optInt("index", -1);
            String targetDescription = JSONUtils.optionalStringKey(targetView, "contentDescription");
            int targetExplicitId = targetView.optInt(DBLikedChannelsHelper.KEY_ID, -1);
            String targetIdName = JSONUtils.optionalStringKey(targetView, "mp_id_name");
            String targetTag = JSONUtils.optionalStringKey(targetView, "tag");
            if ("shortest".equals(prefixCode)) {
                prefix = 1;
            } else if (prefixCode == null) {
                prefix = 0;
            } else {
                Log.w(LOGTAG, "Unrecognized prefix type \"" + prefixCode + "\". No views will be matched");
                return NEVER_MATCH_PATH;
            }
            Integer targetIdOrNull = reconcileIds(targetExplicitId, targetIdName, idNameToId);
            if (targetIdOrNull == null) {
                return NEVER_MATCH_PATH;
            }
            path.add(new PathElement(prefix, targetViewClass, targetIndex, targetIdOrNull.intValue(), targetDescription, targetTag));
        }
        return path;
    }

    private Integer reconcileIds(int explicitId, String idName, ResourceIds idNameToId) {
        int idFromName;
        if (idName == null) {
            idFromName = -1;
        } else if (idNameToId.knownIdName(idName)) {
            idFromName = idNameToId.idFromName(idName);
        } else {
            Log.w(LOGTAG, "Path element contains an id name not known to the system. No views will be matched.\nMake sure that you're not stripping your packages R class out with proguard.\nid name was \"" + idName + "\"");
            return null;
        }
        if (-1 != idFromName && -1 != explicitId && idFromName != explicitId) {
            Log.e(LOGTAG, "Path contains both a named and an explicit id, and they don't match. No views will be matched.");
            return null;
        } else if (-1 != idFromName) {
            return Integer.valueOf(idFromName);
        } else {
            return Integer.valueOf(explicitId);
        }
    }

    private PropertyDescription readPropertyDescription(Class<?> targetClass, JSONObject propertyDesc) throws BadInstructionsException {
        try {
            String mutatorName;
            String propName = propertyDesc.getString(ProfilesDBHelper.COLUMN_NAME);
            Caller accessor = null;
            if (propertyDesc.has("get")) {
                JSONObject accessorConfig = propertyDesc.getJSONObject("get");
                accessor = new Caller(targetClass, accessorConfig.getString("selector"), NO_PARAMS, Class.forName(accessorConfig.getJSONObject("result").getString(MessageColumns.TYPE)));
            }
            if (propertyDesc.has("set")) {
                mutatorName = propertyDesc.getJSONObject("set").getString("selector");
            } else {
                mutatorName = null;
            }
            return new PropertyDescription(propName, targetClass, accessor, mutatorName);
        } catch (NoSuchMethodException e) {
            throw new BadInstructionsException("Can't create property reader", e);
        } catch (JSONException e2) {
            throw new BadInstructionsException("Can't read property JSON", e2);
        } catch (ClassNotFoundException e3) {
            throw new BadInstructionsException("Can't read property JSON, relevant arg/return class not found", e3);
        }
    }

    private Object convertArgument(Object jsonArgument, String type, List<String> assetsLoaded) throws BadInstructionsException, CantGetEditAssetsException {
        try {
            if ("java.lang.CharSequence".equals(type) || "boolean".equals(type) || "java.lang.Boolean".equals(type)) {
                return jsonArgument;
            }
            if ("int".equals(type) || "java.lang.Integer".equals(type)) {
                return Integer.valueOf(((Number) jsonArgument).intValue());
            }
            if ("float".equals(type) || "java.lang.Float".equals(type)) {
                return Float.valueOf(((Number) jsonArgument).floatValue());
            }
            if ("android.graphics.drawable.Drawable".equals(type)) {
                return readBitmapDrawable((JSONObject) jsonArgument, assetsLoaded);
            }
            if ("android.graphics.drawable.BitmapDrawable".equals(type)) {
                return readBitmapDrawable((JSONObject) jsonArgument, assetsLoaded);
            }
            if ("android.graphics.drawable.ColorDrawable".equals(type)) {
                return new ColorDrawable(((Number) jsonArgument).intValue());
            }
            throw new BadInstructionsException("Don't know how to interpret type " + type + " (arg was " + jsonArgument + ")");
        } catch (ClassCastException e) {
            throw new BadInstructionsException("Couldn't interpret <" + jsonArgument + "> as " + type);
        }
    }

    private Drawable readBitmapDrawable(JSONObject description, List<String> assetsLoaded) throws BadInstructionsException, CantGetEditAssetsException {
        try {
            if (description.isNull(SettingsJsonConstants.APP_URL_KEY)) {
                throw new BadInstructionsException("Can't construct a BitmapDrawable with a null url");
            }
            int bottom;
            int top;
            int right;
            int left;
            boolean useBounds;
            String url = description.getString(SettingsJsonConstants.APP_URL_KEY);
            if (description.isNull("dimensions")) {
                bottom = 0;
                top = 0;
                right = 0;
                left = 0;
                useBounds = false;
            } else {
                JSONObject dimensions = description.getJSONObject("dimensions");
                left = dimensions.getInt("left");
                right = dimensions.getInt("right");
                top = dimensions.getInt("top");
                bottom = dimensions.getInt("bottom");
                useBounds = true;
            }
            Bitmap image = this.mImageStore.getImage(url);
            assetsLoaded.add(url);
            Drawable ret = new BitmapDrawable(Resources.getSystem(), image);
            if (useBounds) {
                ret.setBounds(left, top, right, bottom);
            }
            return ret;
        } catch (CantGetImageException e) {
            throw new CantGetEditAssetsException(e.getMessage(), e.getCause());
        } catch (JSONException e2) {
            throw new BadInstructionsException("Couldn't read drawable description", e2);
        }
    }
}
