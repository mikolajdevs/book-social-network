package com.example.book.transaction;

import com.example.book.common.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction")
public class BookTransactionController {

    private final BookTransactionService service;

    @GetMapping()
    public ResponseEntity<PageResponse<BorrowedBookResponse>> findAllBBookTransactions(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "page", defaultValue = "10", required = false) int size,
            @RequestParam(name = "status", required = false) BorrowStatus status,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.findAllBookTransactions(page, size, status, connectedUser));
    }

    @PostMapping()
    public ResponseEntity<Long> borrowBook(
            @RequestBody Long bookId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.borrowBook(bookId, connectedUser));
    }

    @PatchMapping("/{transactionId}/return")
    public ResponseEntity<Long> returnBorrowedBook(
            @PathVariable Long transactionId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.returnBorrowedBook(transactionId, connectedUser));
    }

    @PatchMapping("/{transactionId}/approve")
    public ResponseEntity<Long> approveReturnBorrowedBook(
            @PathVariable Long transactionId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.approveReturnBorrowedBook(transactionId, connectedUser));
    }
}
