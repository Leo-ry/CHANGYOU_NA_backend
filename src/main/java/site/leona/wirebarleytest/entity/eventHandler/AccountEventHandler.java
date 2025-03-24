package site.leona.wirebarleytest.entity.eventHandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import site.leona.wirebarleytest.entity.Account;
import site.leona.wirebarleytest.entity.AccountTransitEvent;
import site.leona.wirebarleytest.entity.Transit;
import site.leona.wirebarleytest.enums.TransactionType;
import site.leona.wirebarleytest.repository.TransitRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AccountEventHandler {

    private final TransitRepository transitRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(AccountTransitEvent accountTransitEvent) {
        Account account = accountTransitEvent.account();
        BigDecimal amount = accountTransitEvent.amount();
        BigDecimal fee = accountTransitEvent.fee();
        TransactionType transactionType = accountTransitEvent.transactionType();
        String relatedBankCode = accountTransitEvent.relatedBankCode();
        String relatedAccountNumber = accountTransitEvent.relatedAccountNumber();

        Transit transit = Transit.doCreate(account, transactionType, amount, fee, account.getBalance(), relatedBankCode, relatedAccountNumber, LocalDateTime.now()).get();
        transitRepository.save(transit);

        System.out.println("Transit created: " + transit);
    }
}
