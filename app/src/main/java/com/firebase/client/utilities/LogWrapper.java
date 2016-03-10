package com.firebase.client.utilities;

import com.firebase.client.Logger;
import com.firebase.client.Logger.Level;
import java.io.PrintWriter;
import java.io.StringWriter;

public class LogWrapper {
    static final /* synthetic */ boolean $assertionsDisabled = (!LogWrapper.class.desiredAssertionStatus());
    private final String component;
    private final Logger logger;
    private final String prefix;

    private static String exceptionStacktrace(Throwable e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    public LogWrapper(Logger logger, String component) {
        this(logger, component, null);
    }

    public LogWrapper(Logger logger, String component, String prefix) {
        this.logger = logger;
        this.component = component;
        this.prefix = prefix;
    }

    public void error(String message, Throwable e) {
        this.logger.onLogMessage(Level.ERROR, this.component, toLog(message) + "\n" + exceptionStacktrace(e), now());
    }

    public void warn(String message) {
        warn(message, null);
    }

    public void warn(String message, Throwable e) {
        String logMsg = toLog(message);
        if (e != null) {
            logMsg = logMsg + "\n" + exceptionStacktrace(e);
        }
        this.logger.onLogMessage(Level.WARN, this.component, logMsg, now());
    }

    public void info(String message) {
        this.logger.onLogMessage(Level.INFO, this.component, toLog(message), now());
    }

    public void debug(String message) {
        debug(message, null);
    }

    public boolean logsDebug() {
        return this.logger.getLogLevel().ordinal() <= Level.DEBUG.ordinal();
    }

    public void debug(String message, Throwable e) {
        String logMsg = toLog(message);
        if (e != null) {
            logMsg = logMsg + "\n" + exceptionStacktrace(e);
        }
        if ($assertionsDisabled || logsDebug()) {
            this.logger.onLogMessage(Level.DEBUG, this.component, logMsg, now());
            return;
        }
        throw new AssertionError();
    }

    private long now() {
        return System.currentTimeMillis();
    }

    private String toLog(String message) {
        return this.prefix == null ? message : this.prefix + " - " + message;
    }
}
