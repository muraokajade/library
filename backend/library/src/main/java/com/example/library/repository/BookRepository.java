package com.example.library.repository;

import com.example.library.entity.Book;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface BookRepository extends JpaRepository<Book, Long> {

    //TODO Book全権取得
    @NonNull
    List<Book> findAll();

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    Page<Book> findByCategory(String category, Pageable pageable);

    // 何も追加しなくてOK。findAll(Pageable pageable) は継承されてる

    //Optional<Book> findById(Long bookId);
}
