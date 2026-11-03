package com.sitairis.service.notifier;

public interface Notifier {
    void send(String recipient, String message);
    String getName();
}
