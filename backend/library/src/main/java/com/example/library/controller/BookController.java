package com.example.library.controller;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.example.library.dao.BookPageResponse;
import com.example.library.entity.Book;
import com.example.library.responsemodel.ShelfCurrentLoansResponse;
import com.example.library.service.BookService;
import com.example.library.utils.ExtractJWT;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@CrossOrigin("http://localhost:3000")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping("/all")
    public List<Book> getAllBooks() {
        return bookService.findAllBooks();
    }

    //チェックアウト画面表示
    @GetMapping("/checkout")
    public ResponseEntity<Book> getBookById(@RequestParam Long bookId) {
        Book book = bookService.findBookById(bookId);
        return ResponseEntity.ok(book);
    }

    @GetMapping
    public BookPageResponse getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size

    ) {
        Pageable pageable = PageRequest.of(page,size);
        //TODO Page はジェネリクスを持つため、型を書かないとならない
        Page<Book> pageData = bookService.findAll(pageable);
        return BookPageResponse.from(pageData, "ALL");

    }


    @GetMapping("/search")
    public Page<Book> searchBook(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    )
    {
        System.out.println("keyword: " + keyword);
        Pageable pageable = PageRequest.of(page,size);
        Page<Book> keywordBook = bookService.searchBook(keyword,pageable);
        return  keywordBook;
    }

    @GetMapping("/category")
    public BookPageResponse searchByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page,size);
        Page<Book>  pageData;

        if("all".equalsIgnoreCase(category)) {
            pageData = bookService.findAll(pageable);
        } else {
            pageData = bookService.searchByCategory(category, pageable);
        }
        return BookPageResponse.from(pageData, category);
    }

    //ユーザーのEmailに基づいてチェックアウトされた書籍の本をカウント
    @GetMapping("/secure/currentloans/count")
    public ResponseEntity<Integer> countLoansCount(@RequestHeader(value = "Authorization") String token) {

        try{
            String idToken = token.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            int loanCount = bookService.currentLoansCount(email);
            return ResponseEntity.ok(loanCount);

        } catch (Exception e) {
            System.out.println("Firebase トークン検証エラー: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }



    }

    @GetMapping("/secure/ischeckout/byuser")
    public Boolean isCheckoutByUser(@RequestHeader(value = "Authorization") String token,
                                    @RequestParam("bookId") Long bookId) {
        try {
            String idToken = token.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String userEmail = decodedToken.getEmail();

            if (userEmail == null) {
                return false;
            }

            return bookService.checkoutBookByUser(userEmail, bookId);

        } catch (Exception e) {
            System.out.println("Firebase トークン検証エラー: " + e.getMessage());
            return false;
        }

    }

    /* 書籍のチェックアウト処理 */
    @PutMapping("/secure/checkout")
    public Book checkoutBook(@RequestHeader(value = "Authorization") String token,
                             @RequestParam Long bookId) {
        try {
            String idToken = token.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String userEmail = decodedToken.getEmail();

            return bookService.checkoutBook(userEmail, bookId);

        } catch (ResponseStatusException e) {
            // すでにステータス付き例外として投げられたなら再スロー
            throw e;
        } catch (Exception e) {
            // それ以外の例外は500エラーとして処理
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error during checkout");
        }
    }
    @GetMapping("/secure/currentloans")
    public List<ShelfCurrentLoansResponse> shelfCurrentLoansResponses(@RequestHeader(value = "Authorization") String token) {

        try{
            String idToken = token.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String userEmail = decodedToken.getEmail();
            return bookService.currentLoans(userEmail);
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token verification failed", e);
        }
    }

    @PutMapping("/secure/return")
    public void returnBook(@RequestHeader(value = "Authorization") String token,
                           @RequestParam Long bookId) {

        try {
            String idToken = token.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String userEmail = decodedToken.getEmail();
            bookService.returnBook(userEmail,bookId);

        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "token failed", e);
        }

    }




}

