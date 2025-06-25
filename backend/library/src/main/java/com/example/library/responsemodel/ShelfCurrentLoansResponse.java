package com.example.library.responsemodel;

import com.example.library.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShelfCurrentLoansResponse {


    private Book book;
    private int daysLeft;


}
