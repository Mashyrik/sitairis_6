package com.sitairis.service.factory;

import com.sitairis.service.observer.LibraryObserver;
import com.sitairis.service.observer.ReaderNotificationObserver;

public class ReaderNotificationObserverFactory extends ObserverFactory {
    @Override
    public LibraryObserver createObserver() {
        return new ReaderNotificationObserver();
    }
}
