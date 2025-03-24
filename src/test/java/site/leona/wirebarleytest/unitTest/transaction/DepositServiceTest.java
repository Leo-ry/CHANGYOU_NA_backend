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
import site.leona.wirebarleytest.enums.AccountStatus;
import site.leona.wirebarleytest.enums.TransactionType;
import site.leona.wirebarleytest.model.TransitDto;
import site.leona.wirebarleytest.repository.AccountRepository;
import site.leona.wirebarleytest.service.TransitService;
import site.leona.wirebarleytest.service.TransitServiceImpl;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DepositServiceTest {

    @InjectMocks
    private TransitServiceImpl transitService;

    @Mock
    private AccountRepository accountRepository;

    @Test
    void 정상_입금처리() {
        Account account = Account.builder()
                .accountId(1L)
                .bankCode("003")
                .accountNumber("12345678901234")
                .ownerName("홍길동")
                .balance(BigDecimal.valueOf(10000))
                .status(AccountStatus.ACTIVE)
                .build();

        TransitDto.depositParam param = new TransitDto.depositParam();
        param.setBankCode("023");
        param.setAccountNumber("1231827894702");
        param.setAmount(BigDecimal.valueOf(5000));

        when(accountRepository.findByBankCodeAndAccountNumber("023", "1231827894702"))
                .thenReturn(Optional.of(account));

        TransitDto.transactionInfo result = transitService.deposit(param);

        Assertions.assertThat(result.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        Assertions.assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(15000));
    }

    @Test
    void 존재하지않는_계좌_출금요청_예외처리() {
        TransitDto.depositParam param = new TransitDto.depositParam();
        param.setBankCode("003");
        param.setAccountNumber("2919289392898");
        param.setAmount(BigDecimal.valueOf(5000));

        when(accountRepository.findByBankCodeAndAccountNumber(param.getBankCode(), param.getAccountNumber()))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> transitService.deposit(param))
                .isInstanceOf(AccountException.class)
                .hasMessage("계좌를 찾을 수 없습니다.");
    }

    @Test
    void 비활성계좌_입금처리_예외처리() {
        Account account = Account.builder()
                .accountId(1L)
                .bankCode("003")
                .accountNumber("12345678901234")
                .ownerName("홍길동")
                .balance(BigDecimal.valueOf(10000))
                .status(AccountStatus.CLOSED)
                .build();

        TransitDto.depositParam param = new TransitDto.depositParam();
        param.setBankCode("023");
        param.setAccountNumber("1231827894702");
        param.setAmount(BigDecimal.valueOf(5000));

        when(accountRepository.findByBankCodeAndAccountNumber("023", "1231827894702"))
                .thenReturn(Optional.of(account));

        Assertions.assertThatThrownBy(() -> transitService.deposit(param))
                .isInstanceOf(AccountException.class)
                .hasMessage("활성 계좌가 아닙니다.");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "-10000"})
    void 잘못된_입금금액_예외처리(BigDecimal amount) {
        TransitDto.depositParam param = new TransitDto.depositParam();
        param.setBankCode("023");
        param.setAccountNumber("1231827894702");
        param.setAmount(amount);

        Assertions.assertThatThrownBy(() -> transitService.deposit(param))
                .isInstanceOf(AccountException.class)
                .hasMessage("입력값이 잘못되었습니다.");
    }
}
