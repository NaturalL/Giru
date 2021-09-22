package com.biasee.giru.event.client.appender;

import com.biasee.giru.event.client.events.ErrorEvent;
import org.apache.commons.lang3.StringUtils;


public interface ErrorAppender {


    default int appendStackTrace(ErrorEvent errorEvent, StringBuilder builder,
                                 StackTraceElement[] stackTraceElements, final int maxDepth) {
        for (int i = 0; i < stackTraceElements.length; i++) {
            StackTraceElement stackTraceElement = stackTraceElements[i];
            String stackTrace = stackTraceElement.toString();

            if (i < maxDepth) {
                builder.append("\t").append(stackTrace).append("\n");
            }
            if (i == 0) {
                errorEvent.putRootStack(stackTrace);
                if (errorEvent.takeFirstStack() == null) {
                    errorEvent.putFirstStack(stackTrace);
                }
            }
        }
        if (stackTraceElements.length > maxDepth) {
            builder.append("\t").append("... ").append(stackTraceElements.length - maxDepth)
                    .append(" more").append("\n");
            return maxDepth;
        }
        return stackTraceElements.length;
    }


}
