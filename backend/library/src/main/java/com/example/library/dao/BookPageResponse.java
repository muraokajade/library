package com.example.library.dao;

import com.example.library.entity.Book;
import org.springframework.data.domain.Page;

import java.util.List;

public record BookPageResponse(
        List<Book> content,
        int totalPages,
        Long totalElements,
        int currentPage,
        int size,
        String category

) {
    public static BookPageResponse from(Page<Book> page, String category) {
        return new BookPageResponse(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                category
        );
    }
}
