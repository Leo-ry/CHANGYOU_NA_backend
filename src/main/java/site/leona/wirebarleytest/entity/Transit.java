package site.leona.wirebarleytest.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.leona.wirebarleytest.common.entity.BaseEntity;
import site.leona.wirebarleytest.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Supplier;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "transit")
public class Transit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long transitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountId")
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "transactionType")
    private TransactionType transactionType;

    // 거래금액
    @Column(name = "amount", nullable = false, precision = 16, scale = 0)
    private BigDecimal amount;

    // 수수료 -> 계산은 서비스 레이어에서
    @Column(name = "fee", nullable = false, precision = 16, scale = 0)
    private BigDecimal fee;

    // 거래후 잔액
    @Column(name = "balance", nullable = false, precision = 16, scale = 0)
    private BigDecimal balance;

    @Column(name = "relatedBankCode", length = 4)
    private String relatedBankCode;

    @Column(name = "relatedAccountNumber", length = 20)
    private String relatedAccountNumber;

    @Column(name = "transitAt")
    private LocalDateTime transitAt;

    //기본 생성자
    @Builder
    private Transit(Long transitId, Account account, TransactionType transactionType, BigDecimal amount, BigDecimal fee, BigDecimal balance, String relatedBankCode, String relatedAccountNumber, LocalDateTime transitAt) {
        this.transitId = transitId;
        this.account = account;
        this.transactionType = transactionType;
        this.amount = amount;
        this.fee = fee;
        this.balance = balance;
        this.relatedBankCode = relatedBankCode;
        this.relatedAccountNumber = relatedAccountNumber;
        this.transitAt = transitAt;
    }

    public Transit(Account account, TransactionType transactionType, BigDecimal amount, BigDecimal fee, BigDecimal balance, String relatedBankCode, String relatedAccountNumber, LocalDateTime transitAt) {
        this.account = account;
        this.transactionType = transactionType;
        this.amount = amount;
        this.fee = fee;
        this.balance = balance;
        this.relatedBankCode = relatedBankCode;
        this.relatedAccountNumber = relatedAccountNumber;
        this.transitAt = transitAt;
    }

    public static Supplier<Transit> doCreate(Account account, TransactionType transactionType, BigDecimal amount, BigDecimal fee, BigDecimal balance, String relatedBankCode, String relatedAccountNumber, LocalDateTime transitAt) {
        return () -> new Transit(account, transactionType, amount, fee, balance, relatedBankCode, relatedAccountNumber, transitAt);
    }
}
