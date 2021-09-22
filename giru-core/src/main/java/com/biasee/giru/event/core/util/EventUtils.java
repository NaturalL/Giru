package com.biasee.giru.event.core.util;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.biasee.giru.event.client.events.ErrorEvent;
import com.biasee.giru.event.core.tools.metrics.RealTimeMeter;
import com.biasee.giru.event.core.tools.metrics.RealTimeMetricRegistry;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EventUtils {

    public static final MetricRegistry metrics = new MetricRegistry();
    public static final RealTimeMetricRegistry realTimeMetrics = new RealTimeMetricRegistry();

    public static final Logger statsLogger = getLogger("monitor", EventUtils.class);
    public static final ScheduledExecutorService scheduledExecutor = Executors
            .newSingleThreadScheduledExecutor();

    public static final RealTimeMeter eventMeter = EventUtils.realTimeMetrics.meter(1, 60);

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static String[] lowPriorityException = new String[]{"InvocationTargetException", "NestedServletException"};

    private static final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    static {
        Slf4jReporter.forRegistry(EventUtils.metrics)
                .outputTo(statsLogger)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build().start(10, TimeUnit.SECONDS);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        Type typeOfT = TypeToken.getParameterized(List.class, clazz).getType();
        return gson.fromJson(json, typeOfT);
    }

    public static String toJson(Object o) {
        return gson.toJson(o);
    }

    public static String formatDate(long timestamp) {
        LocalDateTime date = LocalDateTime
                .ofInstant(Instant.ofEpochMilli(timestamp), TimeZone.getDefault().toZoneId());
        return dateFormatter.format(date);
    }

    public static String formatDateTime(long timestamp) {
        LocalDateTime date = LocalDateTime
                .ofInstant(Instant.ofEpochMilli(timestamp), TimeZone.getDefault().toZoneId());
        return dateTimeFormatter.format(date);
    }

    //按模块分日志文件, 模块名和日志名相同
    public static Logger getLogger(String fileName, Class clazz) {
        return LoggerFactory.getLogger(fileName + "." + clazz);
    }

    public static Logger getLogger(Class clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String getException(ErrorEvent error) {
        String exceptionRaw = getExceptionRaw(error);
        return StringUtils.isBlank(exceptionRaw) ? "其他" : exceptionRaw;
    }

    public static String getExceptionRaw(ErrorEvent error) {
        if (StringUtils.isNotBlank(error.takeExceptionClass())) {
            return error.takeExceptionClass().substring(error.takeExceptionClass().lastIndexOf(".") + 1);
        }
        String msg = error.getError();
        if (error.takeStackTrace() != null) {
            String[] result = error.takeStackTrace().split("\n", 2);
            if (result != null && result.length > 0) {
                msg = result[0]
                        + " " + StringUtils.substringBetween(error.takeStackTrace(), "Caused by", "\n")
                        + " " + msg;
            }
        }

        List<String> exceptionList = new ArrayList<>(2);
        if (StringUtils.isNotBlank(msg)) {
            String[] split = msg.split("[ \\.:,，\\|]");
            for (String s : split) {
                if (s.endsWith("Exception") && !"Exception".equals(s)) {
                    if (!StringUtils.startsWithAny(s, lowPriorityException)) {
                        return s;
                    } else {
                        exceptionList.add(s);
                    }
                }
                if (s.endsWith("Error") && !"Error".equals(s)) {
                    exceptionList.add(s);
                }
            }
        }
        if (!exceptionList.isEmpty()) {
            return exceptionList.get(0);
        }
        if (msg.contains("java.lang.Exception")) {
            return "Exception";
        }
        return "";
    }
    public static String[] getFeatureElements(ErrorEvent errorEvent) {
        if (StringUtils.isBlank(errorEvent.takeStackTrace())) {
            String exception = EventUtils.getException(errorEvent);
            return new String[]{errorEvent.getAppId(), exception,
                            errorEvent.label("logger")};
        } else {
            return new String[]{errorEvent.getAppId(), errorEvent.takeFirstStack(), errorEvent.takeRootStack()};
        }
    }

    public static String getExceptionFeature(ErrorEvent errorEvent) {
        String feature = StringUtils.join(getFeatureElements(errorEvent), "|");
        return String.valueOf(Math.abs(feature.hashCode()));
    }

    public static String getCodeLine(String stackTrace) {
        return StringUtils.substringBetween(stackTrace,"(", ")");
    }

    public static String highlight(String lines, String regex) {
        if (lines != null) {
            //escape 防止注入
            String escapeHtml4 = StringEscapeUtils.escapeHtml4(lines);
            if (StringUtils.isNotBlank(regex)) {
                return escapeHtml4.replaceAll("(" + regex + ")",
                        "<span style=\"color:red;font-weight:bold;\">$1</span>");
            }
        }
        return "";
    }

    public static boolean isEmpty(String[] arr) {
        return arr == null || arr.length == 0;
    }
}
