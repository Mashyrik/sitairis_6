package com.sitairis.service.factory;

import com.sitairis.service.observer.AuditLogObserver;
import com.sitairis.service.observer.LibraryObserver;

public class AuditLogObserverFactory extends ObserverFactory {
    @Override
    public LibraryObserver createObserver() {
        return new AuditLogObserver();
    }
}
