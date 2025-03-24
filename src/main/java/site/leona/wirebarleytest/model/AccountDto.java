package site.leona.wirebarleytest.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import site.leona.wirebarleytest.entity.Account;
import site.leona.wirebarleytest.enums.AccountStatus;

import java.time.LocalDateTime;
import java.util.function.Supplier;

public class AccountDto {

    @Getter
    @Setter
    public static class accountInfo {
        private Long id;
        private String ownerName;
        private AccountStatus status;
        private LocalDateTime createdAt;
        private boolean isNew;

        public accountInfo(Long id, String ownerName, AccountStatus status, LocalDateTime createdAt, boolean isNew) {
            this.id = id;
            this.ownerName = ownerName;
            this.status = status;
            this.createdAt = createdAt;
            this.isNew = isNew;
        }
    }

    @Getter
    @Setter
    public static class saveAccountParam {
        @NotBlank(message = "은행코드는 필수값입니다.")
        private String bankCode;

        @NotBlank(message = "계좌번호는 필수값입니다.")
        private String accountNumber;

        @NotBlank(message = "계좌주명은 필수값입니다.")
        private String ownerName;

        public Supplier<Account> toAccount() {
            return Account.doCreate(bankCode, accountNumber, ownerName);
        }
    }
}
