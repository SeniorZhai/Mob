package com.firebase.client.utilities;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.firebase.client.Logger;
import com.firebase.client.Logger.Level;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultLogger implements Logger {
    private final Set<String> enabledComponents;
    private final Level minLevel;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$firebase$client$Logger$Level = new int[Level.values().length];

        static {
            try {
                $SwitchMap$com$firebase$client$Logger$Level[Level.ERROR.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$firebase$client$Logger$Level[Level.WARN.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$firebase$client$Logger$Level[Level.INFO.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$firebase$client$Logger$Level[Level.DEBUG.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public DefaultLogger(Level level, List<String> enabledComponents) {
        if (enabledComponents != null) {
            this.enabledComponents = new HashSet(enabledComponents);
        } else {
            this.enabledComponents = null;
        }
        this.minLevel = level;
    }

    public Level getLogLevel() {
        return this.minLevel;
    }

    public void onLogMessage(Level level, String tag, String message, long msTimestamp) {
        if (shouldLog(level, tag)) {
            String toLog = buildLogMessage(level, tag, message, msTimestamp);
            switch (AnonymousClass1.$SwitchMap$com$firebase$client$Logger$Level[level.ordinal()]) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    error(tag, toLog);
                    return;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    warn(tag, toLog);
                    return;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    info(tag, toLog);
                    return;
                case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                    debug(tag, toLog);
                    return;
                default:
                    throw new RuntimeException("Should not reach here!");
            }
        }
    }

    protected String buildLogMessage(Level level, String tag, String message, long msTimestamp) {
        return new Date(msTimestamp).toString() + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + "[" + level + "] " + tag + ": " + message;
    }

    protected void error(String tag, String toLog) {
        System.err.println(toLog);
    }

    protected void warn(String tag, String toLog) {
        System.out.println(toLog);
    }

    protected void info(String tag, String toLog) {
        System.out.println(toLog);
    }

    protected void debug(String tag, String toLog) {
        System.out.println(toLog);
    }

    protected boolean shouldLog(Level level, String tag) {
        return level.ordinal() >= this.minLevel.ordinal() && (this.enabledComponents == null || level.ordinal() > Level.DEBUG.ordinal() || this.enabledComponents.contains(tag));
    }
}
