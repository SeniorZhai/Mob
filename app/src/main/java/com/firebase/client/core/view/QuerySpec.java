package com.firebase.client.core.view;

import com.firebase.client.core.Path;
import com.firebase.client.snapshot.Index;
import java.util.Map;

public class QuerySpec {
    private final QueryParams params;
    private final Path path;

    public static QuerySpec defaultQueryAtPath(Path path) {
        return new QuerySpec(path, QueryParams.DEFAULT_PARAMS);
    }

    public QuerySpec(Path path, QueryParams params) {
        this.path = path;
        this.params = params;
    }

    public Path getPath() {
        return this.path;
    }

    public QueryParams getParams() {
        return this.params;
    }

    public static QuerySpec fromPathAndQueryObject(Path path, Map<String, Object> map) {
        return new QuerySpec(path, QueryParams.fromQueryObject(map));
    }

    public Index getIndex() {
        return this.params.getIndex();
    }

    public boolean isDefault() {
        return this.params.isDefault();
    }

    public boolean loadsAllData() {
        return this.params.loadsAllData();
    }

    public String toString() {
        return this.path + ":" + this.params;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QuerySpec that = (QuerySpec) o;
        if (!this.path.equals(that.path)) {
            return false;
        }
        if (this.params.equals(that.params)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (this.path.hashCode() * 31) + this.params.hashCode();
    }
}
