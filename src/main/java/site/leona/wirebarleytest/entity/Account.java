package site.leona.wirebarleytest.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import site.leona.wirebarleytest.common.entity.BaseEntity;
import site.leona.wirebarleytest.enums.AccountStatus;
import site.leona.wirebarleytest.enums.TransactionType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 계좌 테이블
 * 계좌 관리를 위한 최소한의 필요 내용만을 담음
 */

@Getter
@Entity
@Table(name = "account")
@RequiredArgsConstructor
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long accountId;

    @Column(name = "bankCode", nullable = false, length = 4)
    private String bankCode;

    @Column(name = "accountNumber", nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "ownerName", nullable = false, length = 100)
    private String ownerName;

    @Column(name = "balance", nullable = false, precision = 16, scale = 0)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transit> transits = new ArrayList<>();

    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    @DomainEvents
    public List<Object> getDomainEvents() {
        return domainEvents;
    }

    @AfterDomainEventPublication
    public void clearEvent() {
        domainEvents.clear();
    }

    @Builder
    // 기본 생성자
    private Account(Long accountId, String bankCode, String accountNumber, String ownerName, BigDecimal balance, AccountStatus status) {
        this.accountId = accountId;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
        this.status = status;
    }

    private Account(String bankCode, String accountNumber, String ownerName) {
        super();
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = BigDecimal.ZERO;
        // 최초 계설시 무조건 계좌 활성상태로 처리할것 -> 이후 별도의 차단로직을 걸더라도 반드시 활성이후 변경필요
        this.status = AccountStatus.ACTIVE;
    }

    // Repository 레벨에서 해당 함수를 사용하여 Entity 를 생성할 수 있도록 처리
    public static Supplier<Account> doCreate(String bankCode, String accountNumber, String ownerName) {
        return () -> new Account(bankCode, accountNumber, ownerName);
    }

    // 해당 계좌의 상태값을 변경하기 위한 함수
    public void doModifyStatus(AccountStatus status) {
        this.status = status;
    }

    // 해당 계좌의 나머지 정보를 변경하기 위한 함수 -> 반드시 상태값과 별도의 기능으로 처리해야함 (계좌 정보의 일관성있는 처리를 위함)
//    public void doModifyAccountInfo(String bankCode, String accountNumber, String ownerName) {
//        this.bankCode = bankCode;
//        this.accountNumber = accountNumber;
//        this.ownerName = ownerName;
//    }

    public void deposit(BigDecimal amount, BigDecimal fee) {
        this.balance = this.balance.add(amount);
        domainEvents.add(new AccountTransitEvent(this, amount, TransactionType.DEPOSIT, fee, null, null));
    }

    public void withdraw(BigDecimal amount, BigDecimal fee) {
        this.balance = this.balance.subtract(amount).subtract(fee);
        domainEvents.add(new AccountTransitEvent(this, amount, TransactionType.WITHDRAW, fee, null, null));
    }

    public void transfer(boolean isReceiver, BigDecimal amount, BigDecimal fee, String relatedBankCode, String relatedAccountNumber) {
        this.balance = isReceiver ? this.balance.add(amount) : this.balance.subtract(amount).subtract(fee);
        domainEvents.add(new AccountTransitEvent(this, amount, isReceiver ? TransactionType.TRANSFER_IN : TransactionType.TRANSFER_OUT, fee, relatedBankCode, relatedAccountNumber));
    }
}
