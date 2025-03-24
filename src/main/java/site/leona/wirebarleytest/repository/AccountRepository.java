package site.leona.wirebarleytest.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import site.leona.wirebarleytest.entity.Account;

import java.nio.channels.FileChannel;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByBankCodeAndAccountNumber(String bankCode, String accountNumber);

}
