package com.chatapp;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** One message, tagged with the sender's username and unique user key. */
public record ChatMessage(long timestamp, String senderKey, String senderUsername, String text) {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    /** Display form: [2026-10-19 19:08] alice#1A2B3C4D: Hello */
    public String format() {
        return "[" + FORMAT.format(Instant.ofEpochMilli(timestamp)) + "] "
                + senderUsername + "#" + senderKey + ": " + text;
    }
}
