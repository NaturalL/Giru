package com.biasee.giru.event.client.appender;

import static com.biasee.giru.event.client.EventClientUtil.LOGGER;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.biasee.giru.event.client.EventService;
import com.biasee.giru.event.client.events.ErrorEvent;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

public class LogbackErrorAppender extends UnsynchronizedAppenderBase<ILoggingEvent> implements
        ErrorAppender {

    private EventService eventService;

    public LogbackErrorAppender(EventService eventService) {
        this.name = "giru-error-appender";
        this.eventService = eventService;
    }

    @Override
    protected void append(ILoggingEvent event) {
        try {
            report(event);
        } catch (Throwable e) {
            if (eventService.isEnableDebug()) {
                LOGGER.warn("logback append error", e);
            }
        }
    }

    private void report(ILoggingEvent logEvent) {
        if (isNotLoggable(logEvent)) {
            return;
        }
        String msg = logEvent.getFormattedMessage();
        Level level = logEvent.getLevel();
        long timeStamp = logEvent.getTimeStamp();
        String loggerName = logEvent.getLoggerName();

        ErrorEvent errorEvent = new ErrorEvent("error-log", msg);
        errorEvent.setTime(timeStamp);
        errorEvent.label("level", level.levelStr);
        errorEvent.label("logger", loggerName);
        extractException(logEvent, errorEvent);
        eventService.report(errorEvent);
    }

    private boolean isNotLoggable(ILoggingEvent logEvent) {
        return !eventService.isEnableErrorLogEvent()
                || eventService.isBufferFull()
                || !logEvent.getLevel().isGreaterOrEqual(Level.ERROR)
                || LOGGER.getName().equals(logEvent.getLoggerName());
    }

    protected void extractException(ILoggingEvent logEvent, ErrorEvent errorEvent) {
        IThrowableProxy throwableProxy = logEvent.getThrowableProxy();
        if (throwableProxy == null) {
            return;
        }
        Set<IThrowableProxy> circularityDetector = new HashSet<>();
        int count = eventService.getMaxStacktrace();
        boolean first = true;
        boolean root = false;
        StringBuilder builder = new StringBuilder();

        while (throwableProxy != null) {
            IThrowableProxy cause = throwableProxy.getCause();
            if (cause == null || !circularityDetector.add(cause)) {
                root = true;
            }
            if (first) {
                errorEvent.putExceptionClass(throwableProxy.getClassName());
            }
            builder.append(first ? "" : "Caused by: ").append(throwableProxy.getClassName()).append(": ")
                    .append(StringUtils.abbreviate(throwableProxy.getMessage(), 2000)).append("\n");

            StackTraceElement[] stackTraceElements = toStackTraceElements(throwableProxy);
            if (stackTraceElements != null) {
                count -= appendStackTrace(errorEvent, builder, stackTraceElements, Math.max(count, 1));
            }
            if (first) {
                first = false;
            }
            if (root) {
                break;
            }
            throwableProxy = cause;

        }
        errorEvent.putStackTrace(builder.toString());
    }

    protected StackTraceElement[] toStackTraceElements(IThrowableProxy throwableProxy) {
        StackTraceElementProxy[] stackTraceElementProxies = throwableProxy
                .getStackTraceElementProxyArray();
        StackTraceElement[] stackTraceElements = new StackTraceElement[stackTraceElementProxies.length];

        for (int i = 0, stackTraceElementsLength = stackTraceElementProxies.length;
             i < stackTraceElementsLength; i++) {
            stackTraceElements[i] = stackTraceElementProxies[i].getStackTraceElement();
        }
        return stackTraceElements;
    }

    public static void attach(EventService eventService) {
        LogbackErrorAppender errorAppender = new LogbackErrorAppender(eventService);
        errorAppender.start();

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        for (Logger logger : loggerContext.getLoggerList()) {

            if ((!logger.isAdditive() && !logger.isAttached(errorAppender)) || "ROOT"
                    .equals(logger.getName())) {
                logger.addAppender(errorAppender);
                LOGGER.info("添加 appender {} 到 logback logger {}", errorAppender.getName(),
                        logger.getName());
            }
        }
    }
}