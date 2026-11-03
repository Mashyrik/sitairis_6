package com.sitairis.ui;

import com.sitairis.service.LibraryDataExchange;
import com.sitairis.service.LibraryService;
import com.sitairis.service.notifier.AuditLogNotifier;
import com.sitairis.service.notifier.ConsoleNotifier;
import com.sitairis.service.observer.LibraryObserver;
import com.sitairis.service.observer.StatisticsObserver;
import com.sitairis.service.policy.GracePeriodFinePolicy;
import com.sitairis.service.policy.StandardFinePolicy;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.Objects;

public class ConsoleMenu {
    private final LibraryService libraryService;
    private final LibraryDataExchange dataExchange;
    private final Scanner scanner;
    private boolean running;

    public ConsoleMenu(
            LibraryService libraryService, LibraryDataExchange dataExchange, Scanner scanner) {
        this.libraryService = Objects.requireNonNull(libraryService);
        this.dataExchange = Objects.requireNonNull(dataExchange);
        this.scanner = Objects.requireNonNull(scanner);
    }

    public void start() {
        running = true;
        System.out.println("Информационная система библиотеки");
        while (running) {
            printMenu();
            int command = readInt("Выберите действие: ", 0, 14);
            try {
                execute(command);
            } catch (IllegalArgumentException | IllegalStateException exception) {
                System.out.println("Ошибка: " + exception.getMessage());
            }
        }
        System.out.println("Работа программы завершена.");
    }

    private void printMenu() {
        System.out.println("\n1. Показать книги");
        System.out.println("2. Показать читателей");
        System.out.println("3. Показать выдачи");
        System.out.println("4. Добавить книгу");
        System.out.println("5. Зарегистрировать читателя");
        System.out.println("6. Выдать книгу");
        System.out.println("7. Вернуть книгу");
        System.out.println("8. Продлить выдачу");
        System.out.println("9. Проверить просрочки");
        System.out.println("10. Заблокировать/разблокировать читателя");
        System.out.println("11. Выбрать политику штрафов");
        System.out.println("12. Выбрать способ оповещения");
        System.out.println("13. Показать наблюдателей и статистику");
        System.out.println("14. XML и JSON: обработать данные библиотеки");
        System.out.println("0. Выход");
        System.out.println("Текущая политика: " + libraryService.getFinePolicyDescription());
    }

    private void execute(int command) {
        switch (command) {
            case 1 -> printItems(libraryService.getBooks(), "Список книг пуст.");
            case 2 -> printItems(libraryService.getReaders(), "Список читателей пуст.");
            case 3 -> printItems(libraryService.getLoans(), "Выдач пока нет.");
            case 4 -> addBook();
            case 5 -> registerReader();
            case 6 -> issueBook();
            case 7 -> returnBook();
            case 8 -> renewLoan();
            case 9 -> checkOverdue();
            case 10 -> changeReaderStatus();
            case 11 -> changeFinePolicy();
            case 12 -> changeNotifier();
            case 13 -> showObservers();
            case 14 -> dataExchange.demonstrate(System.out);
            case 0 -> running = false;
            default -> throw new IllegalArgumentException("Неизвестная команда.");
        }
    }

    private void addBook() {
        String title = readRequired("Название: ");
        String author = readRequired("Автор: ");
        String isbn = readLine("ISBN (можно оставить пустым): ");
        int year = readInt("Год издания: ", 1450, Year.now().getValue());
        System.out.println("Добавлена: " + libraryService.addBook(title, author, isbn, year));
    }

    private void registerReader() {
        String name = readRequired("ФИО: ");
        String email = readLine("Email: ");
        String phone = readLine("Телефон: ");
        System.out.println("Зарегистрирован: " + libraryService.registerReader(name, email, phone));
    }

    private void issueBook() {
        long readerId = readLong("ID читателя: ", 1);
        long bookId = readLong("ID книги: ", 1);
        int days = readInt("Срок выдачи в днях: ", 1, 365);
        System.out.println("Создана: " + libraryService.issueBook(readerId, bookId, days, LocalDate.now()));
    }

    private void returnBook() {
        long loanId = readLong("ID выдачи: ", 1);
        LocalDate date = readOptionalDate("Дата возврата ГГГГ-ММ-ДД (Enter = сегодня): ");
        System.out.println("Закрыта: " + libraryService.returnBook(loanId, date));
    }

    private void renewLoan() {
        long loanId = readLong("ID выдачи: ", 1);
        int days = readInt("На сколько дней продлить: ", 1, 365);
        System.out.println("Продлена: " + libraryService.renewLoan(loanId, days));
    }

    private void checkOverdue() {
        LocalDate date = readOptionalDate("Дата проверки ГГГГ-ММ-ДД (Enter = сегодня): ");
        int count = libraryService.checkOverdueLoans(date).size();
        System.out.println("Новых просроченных выдач: " + count);
    }

    private void changeReaderStatus() {
        long readerId = readLong("ID читателя: ", 1);
        int state = readInt("1 - разблокировать, 2 - заблокировать: ", 1, 2);
        System.out.println("Обновлён: " + libraryService.setReaderActive(readerId, state == 1));
    }

    private void changeFinePolicy() {
        int choice = readInt("1 - стандартная, 2 - льготная: ", 1, 2);
        libraryService.setFinePolicy(choice == 1 ? new StandardFinePolicy() : new GracePeriodFinePolicy());
    }

    private void changeNotifier() {
        int choice = readInt("1 - консоль, 2 - журнал аудита: ", 1, 2);
        libraryService.setNotifier(choice == 1 ? new ConsoleNotifier() : new AuditLogNotifier());
    }

    private void showObservers() {
        List<LibraryObserver> observers = libraryService.getObservers();
        if (observers.isEmpty()) {
            System.out.println("Наблюдатели не зарегистрированы.");
            return;
        }
        System.out.println("Зарегистрированные наблюдатели:");
        for (LibraryObserver observer : observers) {
            System.out.println("- " + observer.getName());
            if (observer instanceof StatisticsObserver statistics) {
                System.out.println("  " + statistics.getSummary());
            }
        }
    }

    private void printItems(List<?> items, String emptyMessage) {
        if (items.isEmpty()) {
            System.out.println(emptyMessage);
        } else {
            items.forEach(System.out::println);
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String readRequired(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) return value;
            System.out.println("Значение не может быть пустым.");
        }
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            String value = readLine(prompt);
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) return number;
            } catch (NumberFormatException ignored) {
                // Пользователь получает единое понятное сообщение ниже.
            }
            System.out.printf("Введите целое число от %d до %d.%n", min, max);
        }
    }

    private long readLong(String prompt, long min) {
        while (true) {
            String value = readLine(prompt);
            try {
                long number = Long.parseLong(value);
                if (number >= min) return number;
            } catch (NumberFormatException ignored) {
                // Пользователь получает единое понятное сообщение ниже.
            }
            System.out.println("Введите целое число не меньше " + min + ".");
        }
    }

    private LocalDate readOptionalDate(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (value.isEmpty()) return null;
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException ignored) {
                System.out.println("Используйте формат ГГГГ-ММ-ДД, например 2026-11-01.");
            }
        }
    }
}
