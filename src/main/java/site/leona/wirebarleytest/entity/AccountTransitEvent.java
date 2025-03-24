package site.leona.wirebarleytest.entity;

import site.leona.wirebarleytest.enums.TransactionType;

import java.math.BigDecimal;

public record AccountTransitEvent(Account account, BigDecimal amount, TransactionType transactionType, BigDecimal fee, String relatedBankCode, String relatedAccountNumber) {
}
