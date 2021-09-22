package com.biasee.giru.event.client;

import static com.biasee.giru.event.client.EventClientUtil.LOGGER;

import com.biasee.giru.event.client.api.EventClient;
import com.biasee.giru.event.client.appender.Log4jErrorAppender;
import com.biasee.giru.event.client.appender.LogbackErrorAppender;
import com.biasee.giru.event.client.events.BootEvent;
import com.biasee.giru.event.client.events.Event;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;

import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;


@Service
public class EventService {

    @Value("${giru.event.error-log.enabled:true}")
    private volatile boolean enableErrorLogEvent;
    @Value("${giru.event.report.enabled:true}")
    private volatile boolean enableReport;
    @Value("${giru.event.report.debug.enabled:false}")
    private boolean enableDebug;

    @Value("${giru.event.error-log.stacktraceDepth:50}")
    private int maxStacktrace;

    @Value("${giru.event.error-log.bizPackages:}")
    private String[] bizPackages;


    @Value("${spring.application.name:unknown}")
    private String appName;

    private static String ip;

    static {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.error("UnknownHostException", e);
        }
    }

    @Autowired
    private EventClient eventClient;

    private static final int MAX_BUFFER = 100;
    private BlockingQueue<Event> buffer = new ArrayBlockingQueue<>(MAX_BUFFER);
    private AtomicInteger dropped = new AtomicInteger(0);

    private Thread reportThread;


    @PostConstruct
    public void init() {
        attachErrorAppender();

        reportThread = new Thread(() -> {
            while (true) {
                try {
                    Event event;
                    try {
                        event = buffer.take();
                    } catch (InterruptedException e) {
                        break;
                    }

                    doReport(event);

                } catch (Throwable t) {
                    logIfDebug("report error", t);
                    EventClientUtil.sleep(5000);
                }
            }
        });
        reportThread.setName("GiruEventReporter");
        reportThread.setDaemon(false);
        reportThread.start();
        LOGGER.info("启动");
    }

    private void doReport(Event event) {
        String json = EventClientUtil.toJson(event);
        while (enableReport) {
            try {
                eventClient.report(event.getType(), json);
                return;
            } catch (Throwable t) {
                logIfDebug("doReport error", t);
                EventClientUtil.sleep(5000);
            }
        }
    }

    public void logIfDebug(String msg, Throwable t) {
        if (enableDebug) {
            LOGGER.warn(msg, t);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void afterStartup() {
        BootEvent bootEvent = new BootEvent("start");
        report(bootEvent);
        LOGGER.info("发送 BootEvent");
    }

    public void attachErrorAppender() {

        final StaticLoggerBinder binder = StaticLoggerBinder.getSingleton();
        String loggerFactoryClassStr = binder.getLoggerFactoryClassStr();
        LOGGER.warn("ErrorLogAppender attach to " + loggerFactoryClassStr);

        if (loggerFactoryClassStr.startsWith("ch.qos.logback")) {
            LogbackErrorAppender.attach(this);
        } else if (loggerFactoryClassStr.startsWith("org.slf4j.impl.Log4jLoggerFactory")) {
            Log4jErrorAppender.attach(this);
        } else {
            LOGGER.warn("目前仅支持logback,log4j");
        }
    }

    public boolean isBufferFull() {
        return buffer.size() == MAX_BUFFER;
    }

    public boolean report(Event event) {
        if (Thread.currentThread().equals(reportThread)) {
            return false;
        }

        event.setAppId(appName);
        event.setIp(ip);

        boolean offered = buffer.offer(event);
        if (!offered) {
            dropped.incrementAndGet();
        }
        return offered;
    }

    public boolean isEnableDebug() {
        return enableDebug;
    }

    public boolean isEnableErrorLogEvent() {
        return enableErrorLogEvent;
    }

    public int getMaxStacktrace() {
        return maxStacktrace;
    }

    public String[] getBizPackages() {
        return bizPackages;
    }
}
