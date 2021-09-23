package com.biasee.giru.event.core.util;


import org.junit.jupiter.api.Test;


public class EventUtilsTest {

    @Test
    public void testHighlight() {
        String lines ="<abc>\ncom.biasee.test\ncom.other.dao.TestDAO";
        System.out.println(EventUtils.highlight(lines, "com\\.biasee.*"));

    }

}