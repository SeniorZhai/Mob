package com.mixpanel.android.viewcrawler;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.mixpanel.android.viewcrawler.Pathfinder.Accumulator;
import com.mixpanel.android.viewcrawler.Pathfinder.PathElement;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;

@TargetApi(16)
abstract class ViewVisitor implements Accumulator {
    private static final String LOGTAG = "MixpanelAPI.ViewVisitor";
    private final List<PathElement> mPath;
    private final Pathfinder mPathfinder = new Pathfinder();

    public interface OnEventListener {
        void OnEvent(View view, String str, boolean z);
    }

    public interface OnLayoutErrorListener {
        void onLayoutError(LayoutErrorMessage layoutErrorMessage);
    }

    private static abstract class EventTriggeringVisitor extends ViewVisitor {
        private final boolean mDebounce;
        private final String mEventName;
        private final OnEventListener mListener;

        public EventTriggeringVisitor(List<PathElement> path, String eventName, OnEventListener listener, boolean debounce) {
            super(path);
            this.mListener = listener;
            this.mEventName = eventName;
            this.mDebounce = debounce;
        }

        protected void fireEvent(View found) {
            this.mListener.OnEvent(found, this.mEventName, this.mDebounce);
        }

        protected String getEventName() {
            return this.mEventName;
        }
    }

    public static class AddAccessibilityEventVisitor extends EventTriggeringVisitor {
        private final int mEventType;
        private final WeakHashMap<View, TrackingAccessibilityDelegate> mWatching = new WeakHashMap();

        private class TrackingAccessibilityDelegate extends AccessibilityDelegate {
            private AccessibilityDelegate mRealDelegate;

            public TrackingAccessibilityDelegate(AccessibilityDelegate realDelegate) {
                this.mRealDelegate = realDelegate;
            }

            public AccessibilityDelegate getRealDelegate() {
                return this.mRealDelegate;
            }

            public boolean willFireEvent(String eventName) {
                if (AddAccessibilityEventVisitor.this.getEventName() == eventName) {
                    return true;
                }
                if (this.mRealDelegate instanceof TrackingAccessibilityDelegate) {
                    return ((TrackingAccessibilityDelegate) this.mRealDelegate).willFireEvent(eventName);
                }
                return false;
            }

            public void removeFromDelegateChain(TrackingAccessibilityDelegate other) {
                if (this.mRealDelegate == other) {
                    this.mRealDelegate = other.getRealDelegate();
                } else if (this.mRealDelegate instanceof TrackingAccessibilityDelegate) {
                    this.mRealDelegate.removeFromDelegateChain(other);
                }
            }

            public void sendAccessibilityEvent(View host, int eventType) {
                if (eventType == AddAccessibilityEventVisitor.this.mEventType) {
                    AddAccessibilityEventVisitor.this.fireEvent(host);
                }
                if (this.mRealDelegate != null) {
                    this.mRealDelegate.sendAccessibilityEvent(host, eventType);
                }
            }
        }

        public /* bridge */ /* synthetic */ void visit(View view) {
            super.visit(view);
        }

        public AddAccessibilityEventVisitor(List<PathElement> path, int accessibilityEventType, String eventName, OnEventListener listener) {
            super(path, eventName, listener, false);
            this.mEventType = accessibilityEventType;
        }

        public void cleanup() {
            for (Entry<View, TrackingAccessibilityDelegate> entry : this.mWatching.entrySet()) {
                View v = (View) entry.getKey();
                AccessibilityDelegate toCleanup = (TrackingAccessibilityDelegate) entry.getValue();
                AccessibilityDelegate currentViewDelegate = getOldDelegate(v);
                if (currentViewDelegate == toCleanup) {
                    v.setAccessibilityDelegate(toCleanup.getRealDelegate());
                } else if (currentViewDelegate instanceof TrackingAccessibilityDelegate) {
                    ((TrackingAccessibilityDelegate) currentViewDelegate).removeFromDelegateChain(toCleanup);
                }
            }
            this.mWatching.clear();
        }

        public void accumulate(View found) {
            AccessibilityDelegate realDelegate = getOldDelegate(found);
            if (!(realDelegate instanceof TrackingAccessibilityDelegate) || !((TrackingAccessibilityDelegate) realDelegate).willFireEvent(getEventName())) {
                TrackingAccessibilityDelegate newDelegate = new TrackingAccessibilityDelegate(realDelegate);
                found.setAccessibilityDelegate(newDelegate);
                this.mWatching.put(found, newDelegate);
            }
        }

        protected String name() {
            return getEventName() + " event when (" + this.mEventType + ")";
        }

        private AccessibilityDelegate getOldDelegate(View v) {
            AccessibilityDelegate ret = null;
            try {
                return (AccessibilityDelegate) v.getClass().getMethod("getAccessibilityDelegate", new Class[0]).invoke(v, new Object[0]);
            } catch (NoSuchMethodException e) {
                return ret;
            } catch (IllegalAccessException e2) {
                return ret;
            } catch (InvocationTargetException e3) {
                Log.w(ViewVisitor.LOGTAG, "getAccessibilityDelegate threw an exception when called.", e3);
                return ret;
            }
        }
    }

    public static class AddTextChangeListener extends EventTriggeringVisitor {
        private final Map<TextView, TextWatcher> mWatching = new HashMap();

        private class TrackingTextWatcher implements TextWatcher {
            private final View mBoundTo;

            public TrackingTextWatcher(View boundTo) {
                this.mBoundTo = boundTo;
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                AddTextChangeListener.this.fireEvent(this.mBoundTo);
            }
        }

        public /* bridge */ /* synthetic */ void visit(View view) {
            super.visit(view);
        }

        public AddTextChangeListener(List<PathElement> path, String eventName, OnEventListener listener) {
            super(path, eventName, listener, true);
        }

        public void cleanup() {
            for (Entry<TextView, TextWatcher> entry : this.mWatching.entrySet()) {
                ((TextView) entry.getKey()).removeTextChangedListener((TextWatcher) entry.getValue());
            }
            this.mWatching.clear();
        }

        public void accumulate(View found) {
            if (found instanceof TextView) {
                TextView foundTextView = (TextView) found;
                TextWatcher watcher = new TrackingTextWatcher(foundTextView);
                TextWatcher oldWatcher = (TextWatcher) this.mWatching.get(foundTextView);
                if (oldWatcher != null) {
                    foundTextView.removeTextChangedListener(oldWatcher);
                }
                foundTextView.addTextChangedListener(watcher);
                this.mWatching.put(foundTextView, watcher);
            }
        }

        protected String name() {
            return getEventName() + " on Text Change";
        }
    }

    private static class CycleDetector {
        private CycleDetector() {
        }

        public boolean hasCycle(TreeMap<View, List<View>> dependencyGraph) {
            List<View> dfsStack = new ArrayList();
            while (!dependencyGraph.isEmpty()) {
                if (!detectSubgraphCycle(dependencyGraph, (View) dependencyGraph.firstKey(), dfsStack)) {
                    return false;
                }
            }
            return true;
        }

        private boolean detectSubgraphCycle(TreeMap<View, List<View>> dependencyGraph, View currentNode, List<View> dfsStack) {
            if (dfsStack.contains(currentNode)) {
                return false;
            }
            if (dependencyGraph.containsKey(currentNode)) {
                List<View> dependencies = (List) dependencyGraph.remove(currentNode);
                dfsStack.add(currentNode);
                int size = dependencies.size();
                for (int i = 0; i < size; i++) {
                    if (!detectSubgraphCycle(dependencyGraph, (View) dependencies.get(i), dfsStack)) {
                        return false;
                    }
                }
                dfsStack.remove(currentNode);
            }
            return true;
        }
    }

    public static class LayoutErrorMessage {
        private final String mErrorType;
        private final String mName;

        public LayoutErrorMessage(String errorType, String name) {
            this.mErrorType = errorType;
            this.mName = name;
        }

        public String getErrorType() {
            return this.mErrorType;
        }

        public String getName() {
            return this.mName;
        }
    }

    public static class LayoutRule {
        public final int anchor;
        public final int verb;
        public final int viewId;

        public LayoutRule(int vi, int v, int a) {
            this.viewId = vi;
            this.verb = v;
            this.anchor = a;
        }
    }

    public static class LayoutUpdateVisitor extends ViewVisitor {
        private static final Set<Integer> mHorizontalRules = new HashSet(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(5), Integer.valueOf(7)}));
        private static final Set<Integer> mVerticalRules = new HashSet(Arrays.asList(new Integer[]{Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(6), Integer.valueOf(8)}));
        private boolean mAlive;
        private final List<LayoutRule> mArgs;
        private final CycleDetector mCycleDetector;
        private final String mName;
        private final OnLayoutErrorListener mOnLayoutErrorListener;
        private final WeakHashMap<View, int[]> mOriginalValues = new WeakHashMap();

        public LayoutUpdateVisitor(List<PathElement> path, List<LayoutRule> args, String name, OnLayoutErrorListener onLayoutErrorListener) {
            super(path);
            this.mArgs = args;
            this.mName = name;
            this.mAlive = true;
            this.mOnLayoutErrorListener = onLayoutErrorListener;
            this.mCycleDetector = new CycleDetector();
        }

        public void cleanup() {
            for (Entry<View, int[]> original : this.mOriginalValues.entrySet()) {
                View changedView = (View) original.getKey();
                int[] originalValue = (int[]) original.getValue();
                LayoutParams originalParams = (LayoutParams) changedView.getLayoutParams();
                for (int i = 0; i < originalValue.length; i++) {
                    originalParams.addRule(i, originalValue[i]);
                }
                changedView.setLayoutParams(originalParams);
            }
            this.mAlive = false;
        }

        public void visit(View rootView) {
            if (this.mAlive) {
                getPathfinder().findTargetsInRoot(rootView, getPath(), this);
            }
        }

        public void accumulate(View found) {
            int i;
            ViewGroup parent = (ViewGroup) found;
            SparseArray<View> idToChild = new SparseArray();
            int count = parent.getChildCount();
            for (i = 0; i < count; i++) {
                View child = parent.getChildAt(i);
                int childId = child.getId();
                if (childId > 0) {
                    idToChild.put(childId, child);
                }
            }
            int size = this.mArgs.size();
            for (i = 0; i < size; i++) {
                LayoutRule layoutRule = (LayoutRule) this.mArgs.get(i);
                View currentChild = (View) idToChild.get(layoutRule.viewId);
                if (currentChild != null) {
                    LayoutParams currentParams = (LayoutParams) currentChild.getLayoutParams();
                    int[] currentRules = (int[]) currentParams.getRules().clone();
                    if (currentRules[layoutRule.verb] == layoutRule.anchor) {
                        continue;
                    } else {
                        Set<Integer> rules;
                        if (!this.mOriginalValues.containsKey(currentChild)) {
                            this.mOriginalValues.put(currentChild, currentRules);
                        }
                        currentParams.addRule(layoutRule.verb, layoutRule.anchor);
                        if (mHorizontalRules.contains(Integer.valueOf(layoutRule.verb))) {
                            rules = mHorizontalRules;
                        } else if (mVerticalRules.contains(Integer.valueOf(layoutRule.verb))) {
                            rules = mVerticalRules;
                        } else {
                            rules = null;
                        }
                        if (rules == null || verifyLayout(rules, idToChild)) {
                            currentChild.setLayoutParams(currentParams);
                        } else {
                            cleanup();
                            this.mOnLayoutErrorListener.onLayoutError(new LayoutErrorMessage("circular_dependency", this.mName));
                            return;
                        }
                    }
                }
            }
        }

        private boolean verifyLayout(Set<Integer> rules, SparseArray<View> idToChild) {
            TreeMap<View, List<View>> dependencyGraph = new TreeMap(new Comparator<View>() {
                public int compare(View lhs, View rhs) {
                    if (lhs == rhs) {
                        return 0;
                    }
                    if (lhs == null) {
                        return -1;
                    }
                    if (rhs == null) {
                        return 1;
                    }
                    return rhs.hashCode() - lhs.hashCode();
                }
            });
            int size = idToChild.size();
            for (int i = 0; i < size; i++) {
                View child = (View) idToChild.valueAt(i);
                int[] layoutRules = ((LayoutParams) child.getLayoutParams()).getRules();
                List<View> dependencies = new ArrayList();
                for (Integer intValue : rules) {
                    int dependencyId = layoutRules[intValue.intValue()];
                    if (dependencyId > 0 && dependencyId != child.getId()) {
                        dependencies.add(idToChild.get(dependencyId));
                    }
                }
                dependencyGraph.put(child, dependencies);
            }
            return this.mCycleDetector.hasCycle(dependencyGraph);
        }

        protected String name() {
            return "Layout Update";
        }
    }

    public static class PropertySetVisitor extends ViewVisitor {
        private final Caller mAccessor;
        private final Caller mMutator;
        private final Object[] mOriginalValueHolder = new Object[1];
        private final WeakHashMap<View, Object> mOriginalValues = new WeakHashMap();

        public /* bridge */ /* synthetic */ void visit(View view) {
            super.visit(view);
        }

        public PropertySetVisitor(List<PathElement> path, Caller mutator, Caller accessor) {
            super(path);
            this.mMutator = mutator;
            this.mAccessor = accessor;
        }

        public void cleanup() {
            for (Entry<View, Object> original : this.mOriginalValues.entrySet()) {
                View changedView = (View) original.getKey();
                Object originalValue = original.getValue();
                if (originalValue != null) {
                    this.mOriginalValueHolder[0] = originalValue;
                    this.mMutator.applyMethodWithArguments(changedView, this.mOriginalValueHolder);
                }
            }
        }

        public void accumulate(View found) {
            if (this.mAccessor != null) {
                Object[] setArgs = this.mMutator.getArgs();
                if (1 == setArgs.length) {
                    Bitmap desiredValue = setArgs[0];
                    Bitmap currentValue = this.mAccessor.applyMethod(found);
                    if (desiredValue != currentValue) {
                        if (desiredValue != null) {
                            if ((desiredValue instanceof Bitmap) && (currentValue instanceof Bitmap)) {
                                if (desiredValue.sameAs(currentValue)) {
                                    return;
                                }
                            } else if ((desiredValue instanceof BitmapDrawable) && (currentValue instanceof BitmapDrawable)) {
                                Bitmap desiredBitmap = ((BitmapDrawable) desiredValue).getBitmap();
                                Bitmap currentBitmap = ((BitmapDrawable) currentValue).getBitmap();
                                if (desiredBitmap != null && desiredBitmap.sameAs(currentBitmap)) {
                                    return;
                                }
                            } else if (desiredValue.equals(currentValue)) {
                                return;
                            }
                        }
                        if (!((currentValue instanceof Bitmap) || (currentValue instanceof BitmapDrawable) || this.mOriginalValues.containsKey(found))) {
                            this.mOriginalValueHolder[0] = currentValue;
                            if (this.mMutator.argsAreApplicable(this.mOriginalValueHolder)) {
                                this.mOriginalValues.put(found, currentValue);
                            } else {
                                this.mOriginalValues.put(found, null);
                            }
                        }
                    } else {
                        return;
                    }
                }
            }
            this.mMutator.applyMethod(found);
        }

        protected String name() {
            return "Property Mutator";
        }
    }

    public static class ViewDetectorVisitor extends EventTriggeringVisitor {
        private boolean mSeen = false;

        public /* bridge */ /* synthetic */ void visit(View view) {
            super.visit(view);
        }

        public ViewDetectorVisitor(List<PathElement> path, String eventName, OnEventListener listener) {
            super(path, eventName, listener, false);
        }

        public void cleanup() {
        }

        public void accumulate(View found) {
            if (!(found == null || this.mSeen)) {
                fireEvent(found);
            }
            this.mSeen = found != null;
        }

        protected String name() {
            return getEventName() + " when Detected";
        }
    }

    public abstract void cleanup();

    protected abstract String name();

    public void visit(View rootView) {
        this.mPathfinder.findTargetsInRoot(rootView, this.mPath, this);
    }

    protected ViewVisitor(List<PathElement> path) {
        this.mPath = path;
    }

    protected List<PathElement> getPath() {
        return this.mPath;
    }

    protected Pathfinder getPathfinder() {
        return this.mPathfinder;
    }
}
