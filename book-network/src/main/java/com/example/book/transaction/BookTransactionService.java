package com.example.book.transaction;

import com.example.book.book.Book;
import com.example.book.book.BookMapper;
import com.example.book.book.BookService;
import com.example.book.common.PageResponse;
import com.example.book.exception.OperationNotPermittedException;
import com.example.book.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class BookTransactionService {

    private final BookTransactionRepository bookTransactionRepository;
    private final BookService bookService;

    public PageResponse<BorrowedBookResponse> findAllBookTransactions(
            int page, int size, @Nullable BorrowStatus status, Authentication connectedUser
    ) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransaction> allBorrowedBooks = bookTransactionRepository
                .findAllByUserIdAndStatus(pageable, user.getId(), status);
        List<BorrowedBookResponse> bookResponses = allBorrowedBooks.stream()
                .map(BookTransactionMapper::toBorrowedBookResponse).toList();
        return new PageResponse<>(
                bookResponses,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public Long borrowBook(Long bookId, Authentication connectedUser) {
        Book book = bookService.findBookById(bookId);
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or not shareable");
        }
        User user = (User) connectedUser.getPrincipal();
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("Cannot borrow own book");
        }
        final boolean isAlreadyBorrowed = bookTransactionRepository.isAlreadyBorrowedByUser(bookId, user.getId());
        if (isAlreadyBorrowed) {
            throw new OperationNotPermittedException("The requested book is already borrowed");
        }
        BookTransaction bookTransaction = BookTransaction.builder()
                .user(user)
                .book(book)
                .status(BorrowStatus.BORROWED)
                .build();
        return bookTransactionRepository.save(bookTransaction).getId();
    }

    public Long returnBorrowedBook(Long transactionId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        BookTransaction bookTransaction = findBookTransactionById(transactionId);
        if (Objects.equals(user.getId(), bookTransaction.getBook().getOwner().getId())) {
            throw new OperationNotPermittedException("Cannot borrow or return own book");
        }
        if (!bookTransaction.getStatus().equals(BorrowStatus.BORROWED)) {
            throw new OperationNotPermittedException("Book is not borrowed");
        }
        bookTransaction.setStatus(BorrowStatus.RETURNED);
        return bookTransactionRepository.save(bookTransaction).getId();
    }

    public Long approveReturnBorrowedBook(Long transactionId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        BookTransaction bookTransaction = findBookTransactionById(transactionId);
        if (!Objects.equals(user.getId(), bookTransaction.getBook().getOwner().getId())) {
            throw new OperationNotPermittedException("Cannot approve others books");
        }
        if (!bookTransaction.getStatus().equals(BorrowStatus.RETURNED)) {
            throw new OperationNotPermittedException("Book is not returned");
        }
        bookTransaction.setStatus(BorrowStatus.RETURN_APPROVED);
        return bookTransactionRepository.save(bookTransaction).getId();
    }

    BookTransaction findBookTransactionById(Long transactionId) {
        return bookTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("No transaction found with ID: " + transactionId));
    }
}
