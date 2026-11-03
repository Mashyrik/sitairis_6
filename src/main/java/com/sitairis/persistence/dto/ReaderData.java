package com.sitairis.persistence.dto;

import java.time.LocalDate;

public record ReaderData(long id, String fullName, String email, String phone,
                         LocalDate registrationDate, boolean active) {
}
