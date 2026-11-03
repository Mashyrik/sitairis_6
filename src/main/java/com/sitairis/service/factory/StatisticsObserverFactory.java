package com.sitairis.service.factory;

import com.sitairis.service.observer.LibraryObserver;
import com.sitairis.service.observer.StatisticsObserver;

public class StatisticsObserverFactory extends ObserverFactory {
    @Override
    public LibraryObserver createObserver() {
        return new StatisticsObserver();
    }
}
