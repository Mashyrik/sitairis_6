package com.sitairis.persistence.dto;

public record BookData(long id, String title, String author, String isbn,
                       int publicationYear, String status) {
}
