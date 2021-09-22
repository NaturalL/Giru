package com.biasee.giru.event.core.service.dto;

import java.util.*;

import lombok.Data;


@Data
public class AppHistory {
    List<String> days;
    Map<String, List<Integer>> counts;
    Map<String, String> names;
}
