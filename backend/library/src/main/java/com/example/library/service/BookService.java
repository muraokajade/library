package com.example.library.service;

import com.example.library.entity.Book;
import com.example.library.entity.Checkout;
import com.example.library.repository.BookRepository;
import com.example.library.repository.CheckoutRepository;
import com.example.library.responsemodel.ShelfCurrentLoansResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Check;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.swing.text.html.Option;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CheckoutRepository checkoutRepository;

    public List<Book> findAllBooks() {
        return bookRepository.findAll();
    }

    //TODO orElseThrow(...) を使った時点で 「Optionalではない」世界に移行
    public Book findBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }


    public Boolean checkoutBookByUser(String userEmail, Long bookId) {
        // ユーザーがその本を既に借りているか確認
        Checkout checkout = checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);

        return  checkout != null;
    }

    //1. 書籍の存在確認 2. 重複貸出確認 3. 在庫数確認と減算 4. 書籍保存 5. チェックアウト記録作成・保存
    public Book checkoutBook(String userEmail, Long bookId) {
        //TODO 書籍の存在確認　BookRepository で確認するのが正しい
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "書籍が見つかりません"));

        //TODO CheckoutRepository はあくまで「誰がどの本を借りたか」という貸出記録を扱う
        // 2. 重複貸出チェック
        if(checkoutRepository.findByUserEmailAndBookId(userEmail, bookId) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "すでにこの本を借りています");
        }
        //TODO 3. 在庫数確認
        if(book.getCopiesAvailable() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "在庫がありません");
        }
        //TODO 4. 在庫減算
        book.setCopiesAvailable(book.getCopiesAvailable() -1);
        bookRepository.save(book);

        Checkout checkout = new Checkout(userEmail, LocalDate.now(),  // ← LocalDate型
                LocalDate.now().plusDays(7), bookId);
        checkoutRepository.save(checkout);

        return book;
    }


    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }


    public Page<Book> searchBook(String keyword, Pageable pageable) {
        String sanitized = keyword.trim().toLowerCase();
        return bookRepository.searchByTitle(sanitized, pageable);
    }

    public Page<Book> searchByCategory(String category, Pageable pageable) {
        String sanitize = category.trim().toLowerCase();
        return bookRepository.findByCategory(category, pageable);
    }

    public int currentLoansCount(String email) {
        return checkoutRepository.countByUserEmail(email);
    }

    //TODO 指定された userEmail のユーザーが、現在貸出中（未返却かつ返却期限が今日以降）の本の一覧を取得し、画面表示用のDTO（ShelfCurrentLoansResponse）リストとして返すこと。
    public List<ShelfCurrentLoansResponse> currentLoans(String userEmail) throws Exception {

        List<ShelfCurrentLoansResponse> shelfCurrentLoansResponses = new ArrayList<>();

        //TODO ここで新たなメソッド必要か？何だ？
        List<Checkout> checkoutList = checkoutRepository.findByUserEmailAndReturnDateAfter(userEmail, LocalDate.now());
        List<Long> bookIdList = new ArrayList<>();

        System.out.println("📘 チェックアウト件数: " + checkoutList.size());

        //TODO 各貸出データ（Checkout）から bookId を抽出 findAllById() でまとめて本の情報を取得するため
        for(Checkout c: checkoutList) {
            System.out.println("📘" + c);
            bookIdList.add(c.getBookId());
        }

        List<Book> bookList = bookRepository.findAllById(bookIdList);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");



        for(Book book: bookList) {
            Optional<Checkout> checkout = checkoutList
                    .stream()
                    .filter(x -> x.getBookId() == book.getId())
                    .findFirst();
            //型変換
            if (checkout.isPresent()) {
                // ✅ getReturnDate() が LocalDate 型の場合
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate returnDate = checkout.get().getReturnDate();

                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), returnDate);
                shelfCurrentLoansResponses.add(new ShelfCurrentLoansResponse(book, (int) daysLeft));
            }

        }
        return shelfCurrentLoansResponses;
    }

    public void returnBook(String userEmail, Long bookId) throws Exception {
        // 1. 対象の本を取得（存在しない場合は例外）
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new Exception("本が存在しません。"));

        // 2. 対象ユーザーがその本をチェックアウトしているすべての記録を取得
        List<Checkout> checkouts = checkoutRepository.findAllByUserEmailAndBookId(userEmail, bookId);

        // 3. チェックアウト記録がなければ例外をスロー
        if (checkouts.isEmpty()) {
            throw new Exception("チェックアウト記録が見つかりません。");
        }

        // 4. 利用可能冊数を加算（重複分全て加算）
        book.setCopiesAvailable(book.getCopiesAvailable() + checkouts.size());

        // 5. 本の情報を保存（在庫更新）
        bookRepository.save(book);

        // 6. 該当のチェックアウト記録を全て削除（重複を含めるため List）
        for (Checkout checkout : checkouts) {
            checkoutRepository.deleteById(checkout.getId());
        }
    }


//    public void returnBook(String userEmail, Long bookId) throws Exception {
//        Book book = bookRepository.findById(bookId)
//                .orElseThrow(() -> new Exception("本が存在しません。"));
//
//        Checkout validateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);
//
//        if(validateCheckout == null) {
//            throw new Exception("本がェックアウトされてません。");
//        }
//
//        book.setCopiesAvailable(book.getCopiesAvailable() + 1);
//
//        bookRepository.save(book);
//        checkoutRepository.deleteById(validateCheckout.getId());
//
//    }
}
