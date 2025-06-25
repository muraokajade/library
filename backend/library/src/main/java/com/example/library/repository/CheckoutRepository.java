package com.example.library.repository;

import com.example.library.entity.Checkout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CheckoutRepository extends JpaRepository<Checkout, Long> {


    Checkout findByUserEmailAndBookId(String userEmail, Long bookId);

    int countByUserEmail(String email);

    List<Checkout> findByUserEmailAndReturnDateAfter(String userEmail, LocalDate date);

    List<Checkout> findAllByUserEmailAndBookId(String userEmail, Long bookId);

}
