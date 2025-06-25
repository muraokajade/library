package com.example.library.repository;

import com.example.library.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {


    Page<Review> findByBookId(Long bookId, Pageable pageable);

    Review findByUserEmailAndBookId(String userEmail, Long bookId);
}
