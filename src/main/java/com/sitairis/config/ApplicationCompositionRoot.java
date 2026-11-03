package com.sitairis.config;

import com.sitairis.metadata.LibraryMetadataProcessor;
import com.sitairis.metadata.execution.ReflectionLibraryActionExecutor;
import com.sitairis.model.Book;
import com.sitairis.model.Loan;
import com.sitairis.model.Reader;
import com.sitairis.repository.memory.InMemoryBookRepository;
import com.sitairis.repository.memory.InMemoryLoanRepository;
import com.sitairis.repository.memory.InMemoryReaderRepository;
import com.sitairis.service.InitialLibraryDataSeeder;
import com.sitairis.service.LibraryDataExchangeService;
import com.sitairis.service.LibraryService;
import com.sitairis.service.factory.ObserverFactory;
import com.sitairis.service.factory.ObserverType;
import com.sitairis.service.notifier.ConsoleNotifier;
import com.sitairis.service.policy.StandardFinePolicy;
import com.sitairis.ui.ConsoleMenu;
import java.util.Scanner;

/** The only place where the application object graph is assembled. */
public final class ApplicationCompositionRoot {
    public ConsoleMenu compose() {
        var books = new InMemoryBookRepository();
        var readers = new InMemoryReaderRepository();
        var loans = new InMemoryLoanRepository();
        var metadata = new LibraryMetadataProcessor().scan(
                Reader.class, Book.class, Loan.class);

        var libraryService = new LibraryService(
                books,
                readers,
                loans,
                new StandardFinePolicy(),
                new ConsoleNotifier(),
                new ReflectionLibraryActionExecutor(metadata));

        ObserverFactory.getFactory(ObserverType.READER_NOTIFICATION)
                .createAndRegister(libraryService);
        ObserverFactory.getFactory(ObserverType.AUDIT_LOG)
                .createAndRegister(libraryService);
        ObserverFactory.getFactory(ObserverType.STATISTICS)
                .createAndRegister(libraryService);

        new InitialLibraryDataSeeder(books, readers).seed();
        return new ConsoleMenu(
                libraryService, new LibraryDataExchangeService(), new Scanner(System.in));
    }
}
