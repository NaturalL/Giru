package com.biasee.giru.event.core.util;

import java.util.logging.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;


public class DLog {

    private static final Logger rootLogger = LoggerFactory.getLogger(DLog.class);

    private static final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    protected final Logger logger;
    private String prefix;

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private DLog(Logger logger) {
        this.logger = logger;
        prefix = null;
    }

    private DLog(DLog dLog, String prefix) {
        this.logger = dLog.logger;
        this.prefix = prefix;
    }

    public static DLog getDLog(String module, Class<?> clazz) {
        return new DLog(LoggerFactory.getLogger(module + "." + clazz));
    }

    public static DLog getDLog(Class<?> clazz) {
        return new DLog(LoggerFactory.getLogger(clazz));
    }

    public static DLog getDLog(String name) {
        return new DLog(LoggerFactory.getLogger(name));
    }


    public void info(Throwable throwable, String format, Object... args) {
        if (logger.isInfoEnabled()) {
            doLog(Level.INFO, format, throwable, args);
        }
    }

    public void info(String format, Object... args) {
        if (logger.isInfoEnabled()) {
            doLog(Level.INFO, format, null, args);
        }
    }

    public void info(String msg) {
        if (logger.isInfoEnabled()) {
            doLog(Level.INFO, msg, null);
        }
    }

    public void error(Throwable throwable, String format, Object... args) {
        doLog(Level.SEVERE, format, throwable, args);
    }

    public void error(String format, Object... args) {
        doLog(Level.SEVERE, format, null, args);
    }

    public void error(String msg) {
        doLog(Level.SEVERE, msg, null);
    }

    public void warn(Throwable throwable, String format, Object... args) {
        doLog(Level.WARNING, format, throwable, args);
    }

    public void warn(String format, Object... args) {
        doLog(Level.WARNING, format, null, args);
    }

    public void warn(String msg) {
        doLog(Level.WARNING, msg, null);
    }

    public void debug(Throwable throwable, String format, Object... args) {
        if (logger.isDebugEnabled()) {
            doLog(Level.FINE, format, throwable, args);
        }
    }

    public void debug(String format, Object... args) {
        if (logger.isDebugEnabled()) {
            doLog(Level.FINE, format, null, args);
        }
    }

    public void debug(String msg) {
        if (logger.isDebugEnabled()) {
            doLog(Level.FINE, msg, null);
        }
    }

    private void doLog(Level level, String format, Throwable throwable, Object... args) {
        try {
            //DLog Throwable 为第一个参数, 暂兼容slf4j写法
            if (throwable == null
                    && (args != null && args.length > 0 && args[args.length - 1] instanceof Exception)) {
                throwable = (Throwable) args[args.length - 1];
            }
            if (format == null) {
                format = "";
            }
            if (throwable != null) {
                format = format + " Exception:" + throwable.getMessage();
            }

            if (prefix != null) {
                format = prefix + format;
            }

            if (args != null && args.length > 0) {
                format = format(format, args);
            }
            switch (level.getName()) {
                case "FINEST":
                    logger.trace(format, throwable);
                    break;
                case "FINE":
                    logger.debug(format, throwable);
                    break;
                case "INFO":
                    logger.info(format, throwable);
                    break;
                case "WARNING":
                    logger.warn(format, throwable);
                    break;
                case "SEVERE":
                    logger.error(format, throwable);
                    break;
                default:
                    logger.info(format, throwable);
                    break;
            }
        } catch (Exception e) {
            rootLogger.error("doLog error:" + e.getMessage() + " msg:" + format, e);
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public static String format(String format, Object... args) {
        FormattingTuple tuple = MessageFormatter.arrayFormat(format, args);
        return tuple.getMessage();
    }

    public String debugJoin(Object... parts) {
        if (logger.isDebugEnabled()) {
            return join(parts);
        }
        return "DEBUG_JOIN:OFF";
    }

    public String debugSepJoin(char separator, Object... parts) {
        if (logger.isDebugEnabled()) {
            return sepJoin(separator, parts);
        }
        return "DEBUG_JOIN:OFF";
    }

    public static String sepJoin(char separator, Object... parts) {
        return StringUtils.join(parts, separator);
    }

    public static String join(Object... parts) {
        return StringUtils.join(parts);
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static long duration(long startMillis) {
        return System.currentTimeMillis() - startMillis;
    }

    public DLog withPrefix(String prefix) {
        return new DLog(this, prefix);
    }

    public DLog appendPrefix(String prefix) {
        return new DLog(this, this.prefix != null ? this.prefix + prefix : prefix);
    }

    public static String json(Object o) {
        try {
            if (o == null) {
                return "null";
            }
            return gson.toJson(o);
        } catch (Exception e) {
            return o.getClass().getName();
        }
    }

    public String debugJson(Object o) {
        if (logger.isDebugEnabled()) {
            return json(o);
        } else {
            return "{\"DEBUG_OFF\":true}";
        }
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

}
