package com.example.book.transaction;


public class BookTransactionMapper {

    public static BorrowedBookResponse toBorrowedBookResponse(BookTransaction bookTransaction) {
        return BorrowedBookResponse.builder()
                .id(bookTransaction.getBook().getId())
                .title(bookTransaction.getBook().getTitle())
                .authorName(bookTransaction.getBook().getAuthorName())
                .isbn(bookTransaction.getBook().getIsbn())
                .rate(bookTransaction.getBook().getRate())
                .status(bookTransaction.getStatus())
                .build();
    }
}
