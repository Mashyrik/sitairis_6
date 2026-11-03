package com.sitairis.system;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sitairis.metadata.execution.DirectLibraryActionExecutor;
import com.sitairis.repository.memory.InMemoryBookRepository;
import com.sitairis.repository.memory.InMemoryLoanRepository;
import com.sitairis.repository.memory.InMemoryReaderRepository;
import com.sitairis.service.LibraryDataExchangeService;
import com.sitairis.service.LibraryService;
import com.sitairis.service.notifier.Notifier;
import com.sitairis.service.policy.StandardFinePolicy;
import com.sitairis.ui.ConsoleMenu;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

class ConsoleMenuSystemTest {
    @Test
    void userCanIssueAndReturnBookThroughMenu() {
        LibraryService service = new LibraryService(
                new InMemoryBookRepository(), new InMemoryReaderRepository(),
                new InMemoryLoanRepository(), new StandardFinePolicy(),
                new SilentNotifier(), new DirectLibraryActionExecutor());
        service.addBook("Системный тест", "Автор", "SYS-1", 2024);
        service.registerReader("Системный читатель", "system@example.com", "+375292222222");

        String commands = String.join(System.lineSeparator(),
                "6", "1", "1", "7",
                "3",
                "7", "1", "",
                "3",
                "0") + System.lineSeparator();
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setIn(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

            new ConsoleMenu(service, new LibraryDataExchangeService(),
                    new Scanner(System.in)).start();
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        String console = output.toString(StandardCharsets.UTF_8);
        assertTrue(console.contains("Создана: [Выдача #1]"));
        assertTrue(console.contains("Статус: Активна"));
        assertTrue(console.contains("Закрыта: [Выдача #1]"));
        assertTrue(console.contains("Статус: Закрыта"));
        assertTrue(console.contains("Работа программы завершена."));
    }

    @Test
    void xmlAndJsonFeaturesAreAvailableFromMainMenu() {
        LibraryService service = new LibraryService(
                new InMemoryBookRepository(), new InMemoryReaderRepository(),
                new InMemoryLoanRepository(), new StandardFinePolicy(),
                new SilentNotifier(), new DirectLibraryActionExecutor());
        String commands = String.join(System.lineSeparator(), "14", "0")
                + System.lineSeparator();
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setIn(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            new ConsoleMenu(service, new LibraryDataExchangeService(),
                    new Scanner(System.in)).start();
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        String console = output.toString(StandardCharsets.UTF_8);
        assertTrue(console.contains("=== XSD-валидация ==="));
        assertTrue(console.contains("JSON round-trip: OK"));
    }

    private static class SilentNotifier implements Notifier {
        @Override public void send(String recipient, String message) { }
        @Override public String getName() { return "Тестовый уведомитель"; }
    }
}
