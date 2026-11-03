package com.sitairis.model;

import com.sitairis.metadata.annotation.EntityMetadata;
import java.time.LocalDate;
import java.util.Objects;

@EntityMetadata(name = "Читатель")
public class Reader {
    private final long id;
    private final String fullName;
    private final String email;
    private final String phone;
    private final LocalDate registrationDate;
    private boolean active;

    public Reader(long id, String fullName, String email, String phone) {
        this(id, fullName, email, phone, LocalDate.now(), true);
    }

    public Reader(long id, String fullName, String email, String phone,
                  LocalDate registrationDate, boolean active) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID читателя должен быть положительным числом.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("ФИО читателя не может быть пустым.");
        }
        this.id = id;
        this.fullName = fullName.trim();
        this.email = email != null ? email.trim() : "";
        this.phone = phone != null ? phone.trim() : "";
        this.registrationDate = registrationDate != null ? registrationDate : LocalDate.now();
        this.active = active;
    }

    public long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public boolean isActive() { return active; }
    public void block() { active = false; }
    public void unblock() { active = true; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Reader reader = (Reader) object;
        return id == reader.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("[ID: %d] %s | Тел: %s | Email: %s | Регистрация: %s | Статус: %s",
                id, fullName, phone, email, registrationDate, active ? "Активен" : "Заблокирован");
    }
}
