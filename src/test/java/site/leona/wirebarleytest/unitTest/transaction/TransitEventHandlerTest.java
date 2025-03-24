package site.leona.wirebarleytest.unitTest.transaction;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.leona.wirebarleytest.entity.Account;
import site.leona.wirebarleytest.entity.AccountTransitEvent;
import site.leona.wirebarleytest.entity.Transit;
import site.leona.wirebarleytest.entity.eventHandler.AccountEventHandler;
import site.leona.wirebarleytest.enums.AccountStatus;
import site.leona.wirebarleytest.enums.TransactionType;
import site.leona.wirebarleytest.repository.TransitRepository;

import java.math.BigDecimal;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TransitEventHandlerTest {

    @InjectMocks
    private AccountEventHandler accountEventHandler;

    @Mock
    private TransitRepository transitRepository;

    @Test
    void 입금_이벤트_수신_거래내역_정상저장() {
        Account account = Account.builder()
                .accountId(1L)
                .bankCode("003")
                .accountNumber("12345678901234")
                .ownerName("홍길동")
                .balance(BigDecimal.valueOf(150000))
                .status(AccountStatus.ACTIVE)
                .build();

        BigDecimal depositAmount = BigDecimal.valueOf(50000);
        AccountTransitEvent event = new AccountTransitEvent(account, depositAmount, TransactionType.DEPOSIT, BigDecimal.ZERO, account.getBankCode(), account.getAccountNumber());

        accountEventHandler.handle(event);

        ArgumentCaptor<Transit> txCaptor = ArgumentCaptor.forClass(Transit.class);
        verify(transitRepository, times(1)).save(txCaptor.capture());

        Transit savedTx = txCaptor.getValue();

        Assertions.assertThat(savedTx.getAccount()).isEqualTo(account);
        Assertions.assertThat(savedTx.getAmount()).isEqualTo(depositAmount);
        Assertions.assertThat(savedTx.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        Assertions.assertThat(savedTx.getFee()).isEqualTo(BigDecimal.ZERO);
        Assertions.assertThat(savedTx.getBalance()).isEqualTo(account.getBalance());
        Assertions.assertThat(savedTx.getTransitAt()).isNotNull();
    }

    @Test
    void 출금_이벤트_수신_거래내역_정상저장() {
        Account account = Account.builder()
                .accountId(1L)
                .bankCode("003")
                .accountNumber("12345678901234")
                .ownerName("홍길동")
                .balance(BigDecimal.valueOf(150000))
                .status(AccountStatus.ACTIVE)
                .build();

        BigDecimal withdrawAmount = BigDecimal.valueOf(50000);
        AccountTransitEvent event = new AccountTransitEvent(account, withdrawAmount, TransactionType.WITHDRAW, BigDecimal.ZERO, account.getBankCode(), account.getAccountNumber());

        accountEventHandler.handle(event);

        ArgumentCaptor<Transit> txCaptor = ArgumentCaptor.forClass(Transit.class);
        verify(transitRepository, times(1)).save(txCaptor.capture());

        Transit savedTx = txCaptor.getValue();

        Assertions.assertThat(savedTx.getAccount()).isEqualTo(account);
        Assertions.assertThat(savedTx.getAmount()).isEqualTo(withdrawAmount);
        Assertions.assertThat(savedTx.getTransactionType()).isEqualTo(TransactionType.WITHDRAW);
        Assertions.assertThat(savedTx.getFee()).isEqualTo(BigDecimal.ZERO);
        Assertions.assertThat(savedTx.getBalance()).isEqualTo(account.getBalance());
        Assertions.assertThat(savedTx.getTransitAt()).isNotNull();
    }
}
