package com.google.android.gms.analytics;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import com.facebook.internal.AnalyticsEvents;
import com.google.android.gms.analytics.internal.zzae;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class StandardExceptionParser implements ExceptionParser {
    private final TreeSet<String> zzIG = new TreeSet();

    public StandardExceptionParser(Context context, Collection<String> additionalPackages) {
        setIncludedPackages(context, additionalPackages);
    }

    protected StackTraceElement getBestStackTraceElement(Throwable t) {
        StackTraceElement[] stackTrace = t.getStackTrace();
        if (stackTrace == null || stackTrace.length == 0) {
            return null;
        }
        for (StackTraceElement stackTraceElement : stackTrace) {
            String className = stackTraceElement.getClassName();
            Iterator it = this.zzIG.iterator();
            while (it.hasNext()) {
                if (className.startsWith((String) it.next())) {
                    return stackTraceElement;
                }
            }
        }
        return stackTrace[0];
    }

    protected Throwable getCause(Throwable th) {
        while (th.getCause() != null) {
            th = th.getCause();
        }
        return th;
    }

    public String getDescription(String threadName, Throwable t) {
        return getDescription(getCause(t), getBestStackTraceElement(getCause(t)), threadName);
    }

    protected String getDescription(Throwable cause, StackTraceElement element, String threadName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(cause.getClass().getSimpleName());
        if (element != null) {
            String[] split = element.getClassName().split("\\.");
            String str = AnalyticsEvents.PARAMETER_SHARE_OUTCOME_UNKNOWN;
            if (split != null && split.length > 0) {
                str = split[split.length - 1];
            }
            stringBuilder.append(String.format(" (@%s:%s:%s)", new Object[]{str, element.getMethodName(), Integer.valueOf(element.getLineNumber())}));
        }
        if (threadName != null) {
            stringBuilder.append(String.format(" {%s}", new Object[]{threadName}));
        }
        return stringBuilder.toString();
    }

    public void setIncludedPackages(Context context, Collection<String> additionalPackages) {
        this.zzIG.clear();
        Set<String> hashSet = new HashSet();
        if (additionalPackages != null) {
            hashSet.addAll(additionalPackages);
        }
        if (context != null) {
            try {
                String packageName = context.getApplicationContext().getPackageName();
                this.zzIG.add(packageName);
                ActivityInfo[] activityInfoArr = context.getApplicationContext().getPackageManager().getPackageInfo(packageName, 1).activities;
                if (activityInfoArr != null) {
                    for (ActivityInfo activityInfo : activityInfoArr) {
                        hashSet.add(activityInfo.packageName);
                    }
                }
            } catch (NameNotFoundException e) {
                zzae.zzaA("No package found");
            }
        }
        for (String packageName2 : hashSet) {
            Iterator it = this.zzIG.iterator();
            Object obj = 1;
            while (it.hasNext()) {
                String str = (String) it.next();
                if (packageName2.startsWith(str)) {
                    obj = null;
                } else {
                    if (str.startsWith(packageName2)) {
                        this.zzIG.remove(str);
                    }
                    if (obj != null) {
                        this.zzIG.add(packageName2);
                    }
                }
            }
            if (obj != null) {
                this.zzIG.add(packageName2);
            }
        }
    }
}
