package com.sitairis.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sitairis.model.Loan;
import com.sitairis.model.LoanStatus;
import com.sitairis.metadata.execution.DirectLibraryActionExecutor;
import com.sitairis.repository.memory.InMemoryBookRepository;
import com.sitairis.repository.memory.InMemoryLoanRepository;
import com.sitairis.repository.memory.InMemoryReaderRepository;
import com.sitairis.service.LibraryService;
import com.sitairis.service.notifier.Notifier;
import com.sitairis.service.policy.StandardFinePolicy;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LibraryServiceIntegrationTest {
    @Test
    void realServiceAndRepositoriesCompleteLoanLifecycle() {
        LibraryService service = new LibraryService(
                new InMemoryBookRepository(),
                new InMemoryReaderRepository(),
                new InMemoryLoanRepository(),
                new StandardFinePolicy(),
                new SilentNotifier(),
                new DirectLibraryActionExecutor());
        service.addBook("Интеграционное тестирование", "Автор", "INT-1", 2024);
        service.registerReader("Анна Петрова", "anna@example.com", "+375291111111");

        Loan loan = service.issueBook(1, 1, 7, LocalDate.of(2026, 1, 1));

        assertEquals(1, service.getLoans().size());
        assertFalse(service.getBooks().get(0).isAvailable());

        service.returnBook(loan.getId(), LocalDate.of(2026, 1, 10));

        assertEquals(LoanStatus.RETURNED, service.getLoans().get(0).getStatus());
        assertEquals("1.00", service.getLoans().get(0).getFineAmount().toPlainString());
        assertTrue(service.getBooks().get(0).isAvailable());
    }

    private static class SilentNotifier implements Notifier {
        @Override public void send(String recipient, String message) { }
        @Override public String getName() { return "Тестовый уведомитель"; }
    }
}
