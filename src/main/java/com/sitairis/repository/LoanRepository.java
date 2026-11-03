package com.sitairis.repository;

import com.sitairis.model.Loan;
import java.util.List;
import java.util.Optional;

public interface LoanRepository {
    void save(Loan loan);
    Optional<Loan> findById(long id);
    List<Loan> findAll();
    List<Loan> findActiveLoans();
    long nextId();
}
