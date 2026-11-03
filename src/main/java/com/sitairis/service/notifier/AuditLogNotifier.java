package com.sitairis.service.notifier;

public class AuditLogNotifier implements Notifier {
    @Override
    public void send(String recipient, String message) {
        System.out.printf("  [AUDIT-LOG] Кому: %s | Событие: %s%n", recipient, message);
    }

    @Override
    public String getName() { return "Журнал аудита"; }
}
