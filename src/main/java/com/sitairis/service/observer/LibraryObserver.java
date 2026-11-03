package com.sitairis.service.observer;

public interface LibraryObserver {
    void onEvent(LibraryEvent event);
    String getName();
}
