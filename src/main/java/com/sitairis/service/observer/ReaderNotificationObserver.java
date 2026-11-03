package com.sitairis.service.observer;

public class ReaderNotificationObserver implements LibraryObserver {
    private String lastNotification;

    @Override
    public void onEvent(LibraryEvent event) {
        String readerName = event.getLoan().getReader().getFullName();
        lastNotification = "Читателю " + readerName + ": " + event.getMessage();
        System.out.println("  [Observer: оповещение] " + lastNotification);
    }

    @Override
    public String getName() { return "Оповещение читателя"; }

    public String getLastNotification() { return lastNotification; }
}
