package site.leona.wirebarleytest.unitTest.transaction;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WithdrawServiceTest {

    @InjectMocks
    private TransitServiceImpl transitService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransitRepository transitRepository;

    @Test
    void 정상_출금처리() {
        Account account = Account.builder()
                .accountId(1L)
                .bankCode("003")
                .accountNumber("12345678901234")
                .ownerName("홍길동")
                .balance(BigDecimal.valueOf(10000))
                .status(AccountStatus.ACTIVE)
                .build();

        TransitDto.withdrawParam param = new TransitDto.withdrawParam();
        param.setBankCode("023");
        param.setAccountNumber("1231827894702");
        param.setAmount(BigDecimal.valueOf(5000));

        when(accountRepository.findByBankCodeAndAccountNumber(param.getBankCode(), param.getAccountNumber()))
                .thenReturn(Optional.of(account));

        TransitDto.transactionInfo result = transitService.withdraw(param);

        Assertions.assertThat(result.getTransactionType()).isEqualTo(TransactionType.WITHDRAW);
        Assertions.assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(5000));
    }

    @Test
    void 존재하지않는_계좌_출금요청_예외처리() {
        TransitDto.withdrawParam param = new TransitDto.withdrawParam();
        param.setBankCode("003");
        param.setAccountNumber("2919289392898");
        param.setAmount(BigDecimal.valueOf(5000));

        when(accountRepository.findByBankCodeAndAccountNumber(param.getBankCode(), param.getAccountNumber()))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> transitService.withdraw(param))
                .isInstanceOf(AccountException.class)
                .hasMessage("계좌를 찾을 수 없습니다.");
    }

    @Test
    void 비활성계좌_출금처리_예외처리() {
        Account account = Account.builder()
                .accountId(1L)
                .bankCode("003")
                .accountNumber("12345678901234")
                .ownerName("홍길동")
                .balance(BigDecimal.valueOf(10000))
                .status(AccountStatus.CLOSED)
                .build();

        TransitDto.withdrawParam param = new TransitDto.withdrawParam();
        param.setBankCode("023");
        param.setAccountNumber("1231827894702");
        param.setAmount(BigDecimal.valueOf(5000));

        when(accountRepository.findByBankCodeAndAccountNumber("023", "1231827894702"))
                .thenReturn(Optional.of(account));

        Assertions.assertThatThrownBy(() -> transitService.withdraw(param))
                .isInstanceOf(AccountException.class)
                .hasMessage("활성 계좌가 아닙니다.");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "-10000"})
    void 잘못된_출금금액_예외처리(BigDecimal amount) {
        TransitDto.withdrawParam param = new TransitDto.withdrawParam();
        param.setBankCode("023");
        param.setAccountNumber("1231827894702");
        param.setAmount(amount);

        Assertions.assertThatThrownBy(() -> transitService.withdraw(param))
                .isInstanceOf(AccountException.class)
                .hasMessage("입력값이 잘못되었습니다.");
    }

    @Test
    void 잔액부족_출금실패_예외처리() {
        Account account = Account.builder()
                .accountId(1L)
                .bankCode("003")
                .accountNumber("12345678901234")
                .ownerName("홍길동")
                .balance(BigDecimal.valueOf(50000))
                .status(AccountStatus.ACTIVE)
                .build();

        TransitDto.withdrawParam param = new TransitDto.withdrawParam();
        param.setBankCode("023");
        param.setAccountNumber("1231827894702");
        param.setAmount(BigDecimal.valueOf(65000));

        when(accountRepository.findByBankCodeAndAccountNumber(param.getBankCode(), param.getAccountNumber()))
                .thenReturn(Optional.of(account));

        Assertions.assertThatThrownBy(() -> transitService.withdraw(param))
                .isInstanceOf(AccountException.class)
                .hasMessage("계좌의 잔액이 부족합니다.");
    }

    @Test
    void 일한도제한_출금실패_예외처리() {
        Account account = Account.builder()
                .bankCode("001")
                .accountNumber("1111222233334444")
                .ownerName("홍길동")
                .balance(BigDecimal.valueOf(2_000_000))
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.findByBankCodeAndAccountNumber("001", "1111222233334444"))
                .thenReturn(Optional.of(account));

        List<Transit> previous = List.of(
                Transit.builder()
                        .account(account)
                        .transactionType(TransactionType.WITHDRAW)
                        .amount(BigDecimal.valueOf(900_000))
                        .fee(BigDecimal.ZERO)
                        .balance(BigDecimal.valueOf(1_100_000))
                        .transitAt(LocalDateTime.now())
                        .build()
        );

        when(transitRepository.findByAccountAndTransactionTypeAndTransitAtBetween(
                eq(account), eq(TransactionType.WITHDRAW),
                any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(previous);

        TransitDto.withdrawParam param = new TransitDto.withdrawParam();
        param.setBankCode("001");
        param.setAccountNumber("1111222233334444");
        param.setAmount(BigDecimal.valueOf(200_000));

        Assertions.assertThatThrownBy(() -> transitService.withdraw(param))
                .isInstanceOf(AccountException.class)
                .hasMessage("일 출금한도 초과입니다.");
    }
}
