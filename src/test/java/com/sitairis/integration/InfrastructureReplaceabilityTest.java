package com.sitairis.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sitairis.metadata.execution.DirectLibraryActionExecutor;
import com.sitairis.model.Book;
import com.sitairis.model.Loan;
import com.sitairis.repository.BookRepository;
import com.sitairis.repository.memory.InMemoryBookRepository;
import com.sitairis.repository.memory.InMemoryLoanRepository;
import com.sitairis.repository.memory.InMemoryReaderRepository;
import com.sitairis.service.LibraryService;
import com.sitairis.service.notifier.Notifier;
import com.sitairis.service.policy.StandardFinePolicy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class InfrastructureReplaceabilityTest {
    @Test
    void bookRepositoryCanBeReplacedWithoutChangingIssueScenario() {
        for (BookRepository books : List.of(
                new InMemoryBookRepository(), new ListBookRepository())) {
            LibraryService service = new LibraryService(
                    books,
                    new InMemoryReaderRepository(),
                    new InMemoryLoanRepository(),
                    new StandardFinePolicy(),
                    new SilentNotifier(),
                    new DirectLibraryActionExecutor());
            service.addBook("Заменяемая инфраструктура", "Автор", "REPLACE-1", 2026);
            service.registerReader("Тестовый читатель", "test@example.com", "+375290000000");

            Loan loan = service.issueBook(1, 1, 14, LocalDate.of(2026, 11, 3));

            assertEquals("Заменяемая инфраструктура", loan.getBook().getTitle());
            assertEquals(LocalDate.of(2026, 11, 17), loan.getDueDate());
            assertEquals(1, service.getLoans().size());
        }
    }

    /** A second infrastructure adapter used without changing LibraryService. */
    private static final class ListBookRepository implements BookRepository {
        private final List<Book> books = new ArrayList<>();

        @Override public void save(Book book) { books.add(book); }
        @Override public Optional<Book> findById(long id) {
            return books.stream().filter(book -> book.getId() == id).findFirst();
        }
        @Override public List<Book> findAll() { return List.copyOf(books); }
        @Override public long nextId() {
            return books.stream().mapToLong(Book::getId).max().orElse(0) + 1;
        }
    }

    private static final class SilentNotifier implements Notifier {
        @Override public void send(String recipient, String message) { }
        @Override public String getName() { return "Тестовый уведомитель"; }
    }
}
