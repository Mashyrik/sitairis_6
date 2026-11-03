package com.sitairis.persistence.dto;

import java.util.List;

public record LibraryData(List<ReaderData> readers, List<BookData> books,
                          List<LoanData> loans) {
    public LibraryData {
        readers = List.copyOf(readers);
        books = List.copyOf(books);
        loans = List.copyOf(loans);
    }
}
