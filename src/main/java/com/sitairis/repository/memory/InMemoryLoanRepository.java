package com.sitairis.repository.memory;

import com.sitairis.model.Loan;
import com.sitairis.repository.LoanRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryLoanRepository implements LoanRepository {
    private final Map<Long, Loan> loans = new LinkedHashMap<>();

    @Override
    public void save(Loan loan) {
        if (loan == null) throw new IllegalArgumentException("Выдача не может быть null.");
        if (loans.containsKey(loan.getId())) {
            throw new IllegalArgumentException("Выдача с ID " + loan.getId() + " уже существует.");
        }
        loans.put(loan.getId(), loan);
    }

    @Override public Optional<Loan> findById(long id) { return Optional.ofNullable(loans.get(id)); }
    @Override public List<Loan> findAll() { return new ArrayList<>(loans.values()); }
    @Override public List<Loan> findActiveLoans() {
        return loans.values().stream().filter(Loan::isActive).toList();
    }
    @Override public long nextId() {
        return loans.keySet().stream().mapToLong(Long::longValue).max().orElse(0) + 1;
    }
}
