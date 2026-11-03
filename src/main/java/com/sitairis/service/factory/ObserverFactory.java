package com.sitairis.service.factory;

import com.sitairis.service.LibraryService;
import com.sitairis.service.observer.LibraryObserver;

public abstract class ObserverFactory {
    public abstract LibraryObserver createObserver();

    public LibraryObserver createAndRegister(LibraryService service) {
        if (service == null) throw new IllegalArgumentException("Сервис библиотеки обязателен.");
        LibraryObserver observer = createObserver();
        service.attachObserver(observer);
        return observer;
    }

    public static ObserverFactory getFactory(ObserverType type) {
        if (type == null) throw new IllegalArgumentException("Тип наблюдателя обязателен.");
        return switch (type) {
            case READER_NOTIFICATION -> new ReaderNotificationObserverFactory();
            case AUDIT_LOG -> new AuditLogObserverFactory();
            case STATISTICS -> new StatisticsObserverFactory();
        };
    }
}
