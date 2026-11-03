package com.sitairis.service.observer;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuditLogObserver implements LibraryObserver {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final List<String> logs = new ArrayList<>();

    @Override
    public void onEvent(LibraryEvent event) {
        String logEntry = "[" + event.getTimestamp().format(FORMATTER) + "] "
                + event.getType() + " -> " + event.getMessage();
        logs.add(logEntry);
        System.out.println("  [Observer: аудит] " + logEntry);
    }

    @Override
    public String getName() { return "Журнал аудита"; }

    public List<String> getLogs() { return Collections.unmodifiableList(logs); }
}
