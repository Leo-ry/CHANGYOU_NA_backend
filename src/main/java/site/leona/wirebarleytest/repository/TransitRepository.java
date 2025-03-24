package site.leona.wirebarleytest.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import site.leona.wirebarleytest.entity.Account;
import site.leona.wirebarleytest.entity.Transit;
import site.leona.wirebarleytest.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransitRepository extends JpaRepository<Transit, Long> {
    List<Transit> findByAccountAndTransactionTypeAndTransitAtBetween(Account account, TransactionType transactionType, LocalDateTime start, LocalDateTime end);

    Page<Transit> findByAccountOrderByTransitAtDesc(Account account, Pageable pageable);
}
