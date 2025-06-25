package com.example.demo.controller;

import com.example.demo.dto.response.BookPageResponse;
import com.example.demo.dto.response.BookResponse;
import com.example.demo.dto.response.ShelfCurrentLoansResponse;
import com.example.demo.service.BookService;
import com.example.demo.service.CheckoutService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final CheckoutService checkoutService;


    //Page<Book>（＝DBのページ付きデータ）
    // → List<BookResponse>（＝必要な情報だけのレスポンス形式）
    // → BookPageResponse に詰めて返す
    @GetMapping
    public ResponseEntity<BookPageResponse> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
     ) {
        BookPageResponse books = bookService.findAllBooks(page, size);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/title")
    public BookPageResponse searchByTitle(
            @RequestParam String title,
            @RequestParam int page,
            @RequestParam int size
    ) {
        return bookService.searchBooksByTitle(title,page,size);
    }

    @GetMapping("/search/category")
    public BookPageResponse searchByCategory(
            @RequestParam String category,
            @RequestParam int page,
            @RequestParam int size
    ) {
        return bookService.searchBookByCategory(category, page, size);
    }

    @GetMapping("/checkout")
     public BookResponse getBookById(@RequestParam Long bookId) {
        return bookService.findBookById(bookId);
    }

    @GetMapping("/secure/currentloans/count")
    public ResponseEntity<Integer> getCurrentLoansCount(@RequestHeader("Authorization") String token) {

        try {
            String idToken = token.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            System.out.println(email);
            int count =  checkoutService.countLoansCountByUserEmail(email);
            return ResponseEntity.ok(count);

        } catch (Exception e) {
            e.printStackTrace(); // ← ここでエラーの詳細をログ出力
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
    @GetMapping("/secure/ischeckout/byuser")
    public ResponseEntity<Boolean> isBookCheckeoutByUser(@RequestHeader("Authorization") String token,
                                        @RequestParam Long bookId) {
        try {
            String idToken = token.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String UserEmail = decodedToken.getEmail();

            boolean isCheckout = checkoutService.isCheckedOutByUser(UserEmail, bookId);
            return ResponseEntity.ok(isCheckout);

        } catch (Exception e) {
            e.printStackTrace(); // ← ここでエラーの詳細をログ出力
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
    @PutMapping("/secure/checkout")
    public ResponseEntity<?> checkoutBookById(@RequestHeader("Authorization")String token,
                                 @RequestParam Long bookId)
    {
        try {
            String idToken = token.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String UserEmail = decodedToken.getEmail();

            checkoutService.checkoutBookByUserAndEmail(UserEmail, bookId);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/secure/currentloans")
    public List<ShelfCurrentLoansResponse> currentLoansList(@RequestHeader(value = "Authorization") String token){
        try {
            String idToken = token.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            return checkoutService.currentLoans(email);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/secure/return")
    public ResponseEntity<?> returnBook(@RequestHeader(value = "Authorization") String token, @RequestParam Long bookId) {

        try {
            String idToken = token.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            checkoutService.returnBook(email, bookId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/secure/renew")
    public ResponseEntity<?> renewLoan(@RequestHeader(value = "Authorization") String token,@RequestParam Long bookId) {
        try{
            String idToken = token.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            checkoutService.renewLoan(email,bookId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

}
