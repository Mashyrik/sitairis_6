package com.sitairis.service.notifier;

public class ConsoleNotifier implements Notifier {
    @Override
    public void send(String recipient, String message) {
        System.out.println("  [Оповещение -> " + recipient + "]: " + message);
    }

    @Override
    public String getName() { return "Консольный уведомитель"; }
}
