package com.globalbooks.shipping.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class TrackingNumberGenerator {

    @Value("${shipping.tracking.prefix:GB}")
    private String prefix;

    private final Random random = new Random();

    public String generate() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = String.format("%04d", random.nextInt(10000));
        return prefix + timestamp + randomSuffix;
    }
}