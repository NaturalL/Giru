package com.biasee.giru.event.client;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;


public class EventClientUtil {
    public final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EventService.class);
    private static ObjectMapper objectMapper = new ObjectMapper();


    public static void sleep(long millis) {
        try {
            if (millis <= 0) return;

            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String toJson(Object object) {
        try {
            return objectMapper.writer().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }
}
