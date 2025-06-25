package com.example.library.controller;

import com.example.library.entity.Review;
import com.example.library.requestmodels.ReviewRequest;
import com.example.library.service.ReviewService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@CrossOrigin("http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/search/{bookId}")
    public ResponseEntity<Page<Review>> getReviewList(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page,size);
        Page<Review> reviews = reviewService.findReviewsByBookId(bookId,pageable);
        return ResponseEntity.ok(reviews);
    }

    //再取得API
    @GetMapping("/{bookId}")
    public ResponseEntity<Page<Review>> getReviewsByBookId(@PathVariable Long bookId,
                                                           @RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "5")int size) {
        //TODO returnの型どうしようか、

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewService.findReviewsByBookId(bookId,pageable);

        return ResponseEntity.ok(reviews);
    }


    @GetMapping("/secure/user/book")
    public Boolean reviewBookByUser(@RequestHeader(value = "Authorization") String token,
                                    @RequestParam Long bookId) throws Exception {
        try {
            String idToken = token.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            return reviewService.userReviewListed(email,bookId);
        } catch(Exception e) {
            throw new Exception("認証失敗");
        }

    }


    @PostMapping("/secure")
    public void postReview(@RequestHeader(value = "Authorization") String token,
                           @RequestBody ReviewRequest reviewRequest)throws Exception {

        try {
            String idToken = token.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            reviewService.postReview(email, reviewRequest);
        } catch (Exception e) {
            System.out.println("Firebase トークン検証エラー: " + e.getMessage());
        }
    }

}
