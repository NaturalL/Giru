package com.biasee.giru.event.client.appender;

import static com.biasee.giru.event.client.EventClientUtil.LOGGER;

import com.biasee.giru.event.client.EventService;
import com.biasee.giru.event.client.events.ErrorEvent;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;

public class Log4jErrorAppender extends AppenderSkeleton implements ErrorAppender {

    private EventService eventService;

    public Log4jErrorAppender(EventService eventService) {
        this.name = "giru-error-appender";
        this.eventService = eventService;
    }


    private boolean isNotLoggable(LoggingEvent event) {
        return !eventService.isEnableErrorLogEvent()
                || eventService.isBufferFull()
                || event.getLevel().toInt() < Level.ERROR.toInt()
                || LOGGER.getName().equals(event.getLoggerName());
    }

    protected void append(LoggingEvent event) {
        try {
            report(event);
        } catch (Throwable e) {
            if (eventService.isEnableDebug()) {
                LOGGER.warn("log4j append error", e);
            }
        }
    }

    protected void report(LoggingEvent event) {
        if (isNotLoggable(event)) {
            return;
        }
        Object message = event.getMessage();

        String msg = String.valueOf(message);
        Level level = event.getLevel();
        long timeStamp = event.getTimeStamp();
        String loggerName = event.getLoggerName();

        Throwable throwable = null;
        if (message instanceof Throwable) {
            throwable = (Throwable) message;
        } else if (event.getThrowableInformation() != null) {
            throwable = event.getThrowableInformation().getThrowable();
        }

        ErrorEvent errorEvent = new ErrorEvent("error-log", msg);
        errorEvent.setTime(timeStamp);
        errorEvent.label("level", level.toString());
        errorEvent.label("logger", loggerName);
        extractException(throwable, errorEvent);

        eventService.report(errorEvent);
    }


    protected void extractException(Throwable throwable, ErrorEvent errorEvent) {
        if (throwable == null) {
            return;
        }
        Set<Throwable> circularityDetector = new HashSet<>();
        int count = eventService.getMaxStacktrace();
        boolean first = true;
        boolean root = false;
        StringBuilder builder = new StringBuilder();

        while (throwable != null) {
            Throwable cause = throwable.getCause();
            if (cause == null || !circularityDetector.add(cause)) {
                root = true;
            }
            if (first) {
                errorEvent.putExceptionClass(throwable.getClass().getName());
            }
            builder.append(first ? "" : "Caused by: ").append(throwable.getClass().getName()).append(": ")
                    .append(StringUtils.abbreviate(throwable.getMessage(), 2000)).append("\n");

            StackTraceElement[] stackTraceElements = throwable.getStackTrace();
            if (stackTraceElements != null) {
                count -= appendStackTrace(errorEvent, builder, stackTraceElements, Math.max(count, 1));
            }
            if (first) {
                first = false;
            }
            if (root) {
                break;
            }
            throwable = cause;
        }
        errorEvent.putStackTrace(builder.toString());
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    public static void attach(EventService eventService) {
        Log4jErrorAppender errorAppender = new Log4jErrorAppender(eventService);
        if (!LogManager.getRootLogger().isAttached(errorAppender)) {
            LogManager.getRootLogger().addAppender(errorAppender);
        }

        Enumeration loggers = LogManager.getCurrentLoggers();
        while (loggers.hasMoreElements()) {
            Category logger = (Category) loggers.nextElement();
            if (!logger.getAdditivity() && !logger.isAttached(errorAppender)) {
                logger.addAppender(errorAppender);
                LOGGER.info("添加 appender {} 到 log4j logger {}", errorAppender.getName(),
                        logger.getName());
            }
        }
    }

}