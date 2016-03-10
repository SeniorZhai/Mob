package com.firebase.client.core.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.firebase.client.core.view.filter.IndexedFilter;
import com.firebase.client.core.view.filter.LimitedFilter;
import com.firebase.client.core.view.filter.NodeFilter;
import com.firebase.client.core.view.filter.RangedFilter;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.Index;
import com.firebase.client.snapshot.Node;
import com.firebase.client.snapshot.NodeUtilities;
import com.firebase.client.snapshot.PriorityIndex;
import com.helpshift.HSFunnel;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QueryParams {
    static final /* synthetic */ boolean $assertionsDisabled = (!QueryParams.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    public static final QueryParams DEFAULT_PARAMS = new QueryParams();
    private static final String INDEX = "i";
    private static final String INDEX_END_NAME = "en";
    private static final String INDEX_END_VALUE = "ep";
    private static final String INDEX_START_NAME = "sn";
    private static final String INDEX_START_VALUE = "sp";
    private static final String LIMIT = "l";
    private static final String VIEW_FROM = "vf";
    private static final ObjectMapper mapperInstance = new ObjectMapper();
    private Index index = PriorityIndex.getInstance();
    private ChildKey indexEndName = null;
    private Node indexEndValue = null;
    private ChildKey indexStartName = null;
    private Node indexStartValue = null;
    private String jsonSerialization = null;
    private Integer limit;
    private ViewFrom viewFrom;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$firebase$client$core$view$QueryParams$ViewFrom = new int[ViewFrom.values().length];

        static {
            try {
                $SwitchMap$com$firebase$client$core$view$QueryParams$ViewFrom[ViewFrom.LEFT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$firebase$client$core$view$QueryParams$ViewFrom[ViewFrom.RIGHT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    private enum ViewFrom {
        LEFT,
        RIGHT
    }

    static {
        mapperInstance.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public boolean hasStart() {
        return this.indexStartValue != null ? true : $assertionsDisabled;
    }

    public Node getIndexStartValue() {
        if (hasStart()) {
            return this.indexStartValue;
        }
        throw new IllegalArgumentException("Cannot get index start value if start has not been set");
    }

    public ChildKey getIndexStartName() {
        if (!hasStart()) {
            throw new IllegalArgumentException("Cannot get index start name if start has not been set");
        } else if (this.indexStartName != null) {
            return this.indexStartName;
        } else {
            return ChildKey.getMinName();
        }
    }

    public boolean hasEnd() {
        return this.indexEndValue != null ? true : $assertionsDisabled;
    }

    public Node getIndexEndValue() {
        if (hasEnd()) {
            return this.indexEndValue;
        }
        throw new IllegalArgumentException("Cannot get index end value if start has not been set");
    }

    public ChildKey getIndexEndName() {
        if (!hasEnd()) {
            throw new IllegalArgumentException("Cannot get index end name if start has not been set");
        } else if (this.indexEndName != null) {
            return this.indexEndName;
        } else {
            return ChildKey.getMaxName();
        }
    }

    public boolean hasLimit() {
        return this.limit != null ? true : $assertionsDisabled;
    }

    public boolean hasAnchoredLimit() {
        return (!hasLimit() || this.viewFrom == null) ? $assertionsDisabled : true;
    }

    public int getLimit() {
        if (hasLimit()) {
            return this.limit.intValue();
        }
        throw new IllegalArgumentException("Cannot get limit if limit has not been set");
    }

    public Index getIndex() {
        return this.index;
    }

    private QueryParams copy() {
        QueryParams params = new QueryParams();
        params.limit = this.limit;
        params.indexStartValue = this.indexStartValue;
        params.indexStartName = this.indexStartName;
        params.indexEndValue = this.indexEndValue;
        params.indexEndName = this.indexEndName;
        params.viewFrom = this.viewFrom;
        params.index = this.index;
        return params;
    }

    public QueryParams limit(int limit) {
        QueryParams copy = copy();
        copy.limit = Integer.valueOf(limit);
        copy.viewFrom = null;
        return copy;
    }

    public QueryParams limitToFirst(int limit) {
        QueryParams copy = copy();
        copy.limit = Integer.valueOf(limit);
        copy.viewFrom = ViewFrom.LEFT;
        return copy;
    }

    public QueryParams limitToLast(int limit) {
        QueryParams copy = copy();
        copy.limit = Integer.valueOf(limit);
        copy.viewFrom = ViewFrom.RIGHT;
        return copy;
    }

    public QueryParams startAt(Node indexStartValue, ChildKey indexStartName) {
        if ($assertionsDisabled || indexStartValue.isLeafNode() || indexStartValue.isEmpty()) {
            QueryParams copy = copy();
            copy.indexStartValue = indexStartValue;
            copy.indexStartName = indexStartName;
            return copy;
        }
        throw new AssertionError();
    }

    public QueryParams endAt(Node indexEndValue, ChildKey indexEndName) {
        if ($assertionsDisabled || indexEndValue.isLeafNode() || indexEndValue.isEmpty()) {
            QueryParams copy = copy();
            copy.indexEndValue = indexEndValue;
            copy.indexEndName = indexEndName;
            return copy;
        }
        throw new AssertionError();
    }

    public QueryParams orderBy(Index index) {
        QueryParams copy = copy();
        copy.index = index;
        return copy;
    }

    public boolean isViewFromLeft() {
        if (this.viewFrom != null) {
            return this.viewFrom == ViewFrom.LEFT ? true : $assertionsDisabled;
        } else {
            return hasStart();
        }
    }

    public Map<String, Object> getWireProtocolParams() {
        Map<String, Object> queryObject = new HashMap();
        if (hasStart()) {
            queryObject.put(INDEX_START_VALUE, this.indexStartValue.getValue());
            if (this.indexStartName != null) {
                queryObject.put(INDEX_START_NAME, this.indexStartName.asString());
            }
        }
        if (hasEnd()) {
            queryObject.put(INDEX_END_VALUE, this.indexEndValue.getValue());
            if (this.indexEndName != null) {
                queryObject.put(INDEX_END_NAME, this.indexEndName.asString());
            }
        }
        if (this.limit != null) {
            queryObject.put(LIMIT, this.limit);
            ViewFrom viewFromToAdd = this.viewFrom;
            if (viewFromToAdd == null) {
                if (hasStart()) {
                    viewFromToAdd = ViewFrom.LEFT;
                } else {
                    viewFromToAdd = ViewFrom.RIGHT;
                }
            }
            switch (AnonymousClass1.$SwitchMap$com$firebase$client$core$view$QueryParams$ViewFrom[viewFromToAdd.ordinal()]) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    queryObject.put(VIEW_FROM, LIMIT);
                    break;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    queryObject.put(VIEW_FROM, HSFunnel.REVIEWED_APP);
                    break;
            }
        }
        if (!this.index.equals(PriorityIndex.getInstance())) {
            queryObject.put(INDEX, this.index.getQueryDefinition());
        }
        return queryObject;
    }

    public boolean loadsAllData() {
        return (hasStart() || hasEnd() || hasLimit()) ? $assertionsDisabled : true;
    }

    public boolean isDefault() {
        return (loadsAllData() && this.index.equals(PriorityIndex.getInstance())) ? true : $assertionsDisabled;
    }

    public boolean isValid() {
        return (hasStart() && hasEnd() && hasLimit() && !hasAnchoredLimit()) ? $assertionsDisabled : true;
    }

    public String toJSON() {
        if (this.jsonSerialization == null) {
            try {
                this.jsonSerialization = mapperInstance.writeValueAsString(getWireProtocolParams());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this.jsonSerialization;
    }

    public static QueryParams fromQueryObject(Map<String, Object> map) {
        QueryParams params = new QueryParams();
        params.limit = (Integer) map.get(LIMIT);
        if (map.containsKey(INDEX_START_VALUE)) {
            params.indexStartValue = NodeUtilities.NodeFromJSON(map.get(INDEX_START_VALUE));
            String indexStartName = (String) map.get(INDEX_START_NAME);
            if (indexStartName != null) {
                params.indexStartName = ChildKey.fromString(indexStartName);
            }
        }
        if (map.containsKey(INDEX_END_VALUE)) {
            params.indexEndValue = NodeUtilities.NodeFromJSON(map.get(INDEX_END_VALUE));
            String indexEndName = (String) map.get(INDEX_END_NAME);
            if (indexEndName != null) {
                params.indexEndName = ChildKey.fromString(indexEndName);
            }
        }
        String viewFrom = (String) map.get(VIEW_FROM);
        if (viewFrom != null) {
            params.viewFrom = viewFrom.equals(LIMIT) ? ViewFrom.LEFT : ViewFrom.RIGHT;
        }
        String indexStr = (String) map.get(INDEX);
        if (indexStr != null) {
            params.index = Index.fromQueryDefinition(indexStr);
        }
        return params;
    }

    public NodeFilter getNodeFilter() {
        if (loadsAllData()) {
            return new IndexedFilter(getIndex());
        }
        if (hasLimit()) {
            return new LimitedFilter(this);
        }
        return new RangedFilter(this);
    }

    public String toString() {
        return getWireProtocolParams().toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return $assertionsDisabled;
        }
        QueryParams that = (QueryParams) o;
        return (this.limit == null ? that.limit != null : !this.limit.equals(that.limit)) ? $assertionsDisabled : (this.index == null ? that.index != null : !this.index.equals(that.index)) ? $assertionsDisabled : (this.indexEndName == null ? that.indexEndName != null : !this.indexEndName.equals(that.indexEndName)) ? $assertionsDisabled : (this.indexEndValue == null ? that.indexEndValue != null : !this.indexEndValue.equals(that.indexEndValue)) ? $assertionsDisabled : (this.indexStartName == null ? that.indexStartName != null : !this.indexStartName.equals(that.indexStartName)) ? $assertionsDisabled : (this.indexStartValue == null ? that.indexStartValue != null : !this.indexStartValue.equals(that.indexStartValue)) ? $assertionsDisabled : isViewFromLeft() != that.isViewFromLeft() ? $assertionsDisabled : true;
    }

    public int hashCode() {
        int result;
        int hashCode;
        int i = 0;
        if (this.limit != null) {
            result = this.limit.intValue();
        } else {
            result = 0;
        }
        int i2 = ((result * 31) + (isViewFromLeft() ? 1231 : 1237)) * 31;
        if (this.indexStartValue != null) {
            hashCode = this.indexStartValue.hashCode();
        } else {
            hashCode = 0;
        }
        i2 = (i2 + hashCode) * 31;
        if (this.indexStartName != null) {
            hashCode = this.indexStartName.hashCode();
        } else {
            hashCode = 0;
        }
        i2 = (i2 + hashCode) * 31;
        if (this.indexEndValue != null) {
            hashCode = this.indexEndValue.hashCode();
        } else {
            hashCode = 0;
        }
        i2 = (i2 + hashCode) * 31;
        if (this.indexEndName != null) {
            hashCode = this.indexEndName.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (i2 + hashCode) * 31;
        if (this.index != null) {
            i = this.index.hashCode();
        }
        return hashCode + i;
    }
}
