package site.leona.wirebarleytest.unitTest.transaction;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.leona.wirebarleytest.common.exception.AccountException;
import site.leona.wirebarleytest.entity.Account;
import site.leona.wirebarleytest.entity.Transit;
import site.leona.wirebarleytest.enums.AccountStatus;
import site.leona.wirebarleytest.enums.TransactionType;
import site.leona.wirebarleytest.model.TransitDto;
import site.leona.wirebarleytest.repository.AccountRepository;
import site.leona.wirebarleytest.repository.TransitRepository;
import site.leona.wirebarleytest.service.TransitServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

//@SpringBootTest
//@Transactional
@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @InjectMocks
    private TransitServiceImpl transitService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransitRepository transitRepository;

    @Test
    void 이체_이벤트수신_거래내역_정상처리() {
        Account sender = Account.builder()
                .bankCode("001")
                .accountNumber("1111222233334444")
                .ownerName("홍길동")
                .balance(BigDecimal.valueOf(1_000_000))
                .status(AccountStatus.ACTIVE)
                .build();

        Account receiver = Account.builder()
                .bankCode("002")
                .accountNumber("2222333344445555")
                .ownerName("성춘향")
                .balance(BigDecimal.valueOf(100_000))
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.findByBankCodeAndAccountNumber("001", "1111222233334444"))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findByBankCodeAndAccountNumber("002", "2222333344445555"))
                .thenReturn(Optional.of(receiver));

        List<Transit> previousTransfers = List.of(
                Transit.builder()
                        .account(sender)
                        .transactionType(TransactionType.TRANSFER_OUT)
                        .amount(BigDecimal.valueOf(900_000))
                        .fee(BigDecimal.valueOf(0))
                        .balance(BigDecimal.valueOf(100_000))
                        .transitAt(LocalDateTime.now())
                        .build()
        );

        when(transitRepository.findByAccountAndTransactionTypeAndTransitAtBetween(
                eq(sender), eq(TransactionType.TRANSFER_OUT), any(), any()
        )).thenReturn(previousTransfers);

        TransitDto.transferParam param = new TransitDto.transferParam();
        param.setFromBankCode("001");
        param.setFromAccountNumber("1111222233334444");
        param.setToBankCode("002");
        param.setToAccountNumber("2222333344445555");
        param.setAmount(BigDecimal.valueOf(300_000));

        TransitDto.transactionInfo result = transitService.transfer(param);

        assertThat(result.getId()).isEqualTo(receiver.getAccountId());
        assertThat(result.getTransactionType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(result.getBalance()).isEqualTo(receiver.getBalance());
    }

    @Test
    void 이체_일한도제한_예외처리() {
        Account sender = Account.builder()
                .bankCode("001")
                .accountNumber("1111222233334444")
                .ownerName("홍길동")
                .balance(BigDecimal.valueOf(5_000_000))
                .status(AccountStatus.ACTIVE)
                .build();

        Account receiver = Account.builder()
                .bankCode("002")
                .accountNumber("2222333344445555")
                .ownerName("성춘향")
                .balance(BigDecimal.valueOf(100_000))
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.findByBankCodeAndAccountNumber("001", "1111222233334444"))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findByBankCodeAndAccountNumber("002", "2222333344445555"))
                .thenReturn(Optional.of(receiver));

        List<Transit> previousTransfers = List.of(
                Transit.builder()
                        .account(sender)
                        .transactionType(TransactionType.TRANSFER_OUT)
                        .amount(BigDecimal.valueOf(2_800_000))
                        .fee(BigDecimal.valueOf(0))
                        .balance(BigDecimal.valueOf(2_200_000))
                        .transitAt(LocalDateTime.now())
                        .build()
        );

        when(transitRepository.findByAccountAndTransactionTypeAndTransitAtBetween(
                eq(sender), eq(TransactionType.TRANSFER_OUT), any(), any()
        )).thenReturn(previousTransfers);

        TransitDto.transferParam param = new TransitDto.transferParam();
        param.setFromBankCode("001");
        param.setFromAccountNumber("1111222233334444");
        param.setToBankCode("002");
        param.setToAccountNumber("2222333344445555");
        param.setAmount(BigDecimal.valueOf(300_000));

        Assertions.assertThatThrownBy(() -> transitService.transfer(param))
                .isInstanceOf(AccountException.class)
                .hasMessage("일 이체한도 초과입니다.");
    }
}
