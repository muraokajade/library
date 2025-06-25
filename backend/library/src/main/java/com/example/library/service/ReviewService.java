package com.example.library.service;

import com.example.library.entity.Review;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ReviewRepository;
import com.example.library.requestmodels.ReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    public Page<Review> findReviewsByBookId(Long bookId, Pageable pageable) {
        return reviewRepository.findByBookId(bookId, pageable);

    }

    public void postReview(String userEmail, ReviewRequest reviewRequest) throws Exception {
        Review validateReview = reviewRepository.findByUserEmailAndBookId(userEmail, reviewRequest.getBookId());
        if(validateReview != null) {
            throw new Exception("Review already created");
        }

        //
        Review review = new Review();
        review.setBookId(reviewRequest.getBookId());
        review.setRating(reviewRequest.getRating());
        review.setUserEmail(userEmail);
        review.setReviewDescription(reviewRequest.getReviewDescription().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        review.setDate(Date.valueOf(LocalDate.now()));
        reviewRepository.save(review);
    }

    public boolean userReviewListed(String userEmail, Long bookId) {
        Review validateReview = reviewRepository.findByUserEmailAndBookId(userEmail, bookId);

        return validateReview != null;


    }


}
