package site.leona.wirebarleytest.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import site.leona.wirebarleytest.entity.Account;
import site.leona.wirebarleytest.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Consumer;

public class TransitDto {
    @Getter
    @Setter
    public static class transactionInfo {
        private Long id;
        private BigDecimal balance;
        private TransactionType transactionType;

        public transactionInfo(Long id, BigDecimal balance, TransactionType transactionType) {
            this.id = id;
            this.balance = balance;
            this.transactionType = transactionType;
        }
    }

    @Getter
    @Setter
    public static class depositParam {
        @NotBlank(message = "은행코드는 필수값입니다.")
        private String bankCode;
        @NotBlank(message = "계좌번호는 필수값입니다.")
        private String accountNumber;

        @NotNull
        @DecimalMin(value = "1.0", message = "입금 금액은 1원 이상이어야합니다.")
        private BigDecimal amount;

        public Consumer<Account> toDeposit() {
            return account -> account.deposit(amount, BigDecimal.ZERO);
        }
    }

    @Getter
    @Setter
    public static class withdrawParam {
        @NotBlank(message = "은행코드는 필수값입니다.")
        private String bankCode;
        @NotBlank(message = "계좌번호는 필수값입니다.")
        private String accountNumber;

        @NotNull
        @DecimalMin(value = "1.0", message = "입금 금액은 1원 이상이어야합니다.")
        private BigDecimal amount;

        public Consumer<Account> toWithdraw() {
            return account -> account.withdraw(amount, BigDecimal.ZERO);
        }
    }

    @Getter
    @Setter
    public static class transferParam {
        private String fromBankCode;
        private String toBankCode;

        private String fromAccountNumber;
        private String toAccountNumber;

        private BigDecimal amount;

        public Consumer<Account> toWithdraw(BigDecimal fee) {
            return account -> account.withdraw(amount, fee);
        }

        public Consumer<Account> toDeposit() {
            return account -> account.deposit(amount, BigDecimal.ZERO);
        }

        public Consumer<Account> toTransfer(boolean isReceiver, BigDecimal fee, String relatedBankCode, String relatedAccountNumber) {
            return account -> account.transfer(isReceiver, amount, fee, relatedBankCode, relatedAccountNumber);
        }
    }

    @Getter
    @Setter
    public static class transitInfo {
        private Long accountId;
        private BigDecimal balance;

        private Page<transit> transits;

        public transitInfo(Long accountId, BigDecimal balance, Page<transit> transits) {
            this.accountId = accountId;
            this.balance = balance;
            this.transits = transits;
        }
    }

    @Getter
    @Setter
    public static class transit {
        private Long transitId;
        private BigDecimal amount;
        private BigDecimal fee;
        private BigDecimal balance;
        private TransactionType transactionType;
        private String relatedBankCode;
        private String relatedAccountNumber;
        private LocalDateTime transitAt;

        public transit(Long transitId, BigDecimal amount, BigDecimal fee, BigDecimal balance, TransactionType transactionType, String relatedBankCode, String relatedAccountNumber, LocalDateTime transitAt) {
            this.transitId = transitId;
            this.amount = amount;
            this.fee = fee;
            this.balance = balance;
            this.transactionType = transactionType;
            this.relatedBankCode = relatedBankCode;
            this.relatedAccountNumber = relatedAccountNumber;
            this.transitAt = transitAt;
        }
    }
}
