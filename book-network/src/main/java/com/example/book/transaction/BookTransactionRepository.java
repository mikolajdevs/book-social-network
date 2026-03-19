package com.example.book.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookTransactionRepository extends JpaRepository<BookTransaction, Long> {

    @Query("""
            SELECT t FROM BookTransaction t
            WHERE t.user.id = :userId AND (:status IS NULL OR t.status = :status)
            """)
    Page<BookTransaction> findAllByUserIdAndStatus(Pageable pageable, Long userId, BorrowStatus status);

    @Query("""
            SELECT (COUNT(*) > 0) AS isBorrowed FROM BookTransaction t
            WHERE t.user.id = :userId
            AND t.book.id = :bookId
            AND t.status != 'RETURN_APPROVED'
            """)
    boolean isAlreadyBorrowedByUser(Long bookId, Long userId);
}
