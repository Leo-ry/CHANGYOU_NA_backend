package site.leona.wirebarleytest.unitTest.account;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import site.leona.wirebarleytest.common.exception.AccountException;
import site.leona.wirebarleytest.entity.Account;
import site.leona.wirebarleytest.entity.Transit;
import site.leona.wirebarleytest.enums.AccountStatus;
import site.leona.wirebarleytest.enums.TransactionType;
import site.leona.wirebarleytest.model.AccountDto;
import site.leona.wirebarleytest.model.TransitDto;
import site.leona.wirebarleytest.repository.AccountRepository;
import site.leona.wirebarleytest.repository.TransitRepository;
import site.leona.wirebarleytest.service.AccountServiceImpl;
import site.leona.wirebarleytest.service.TransitServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class AccountServiceTest {

    @InjectMocks
    private AccountServiceImpl accountService;

    @InjectMocks
    private TransitServiceImpl transitService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransitRepository transitRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Account createAccount(Long id) {
        return Account.builder()
                .accountId(id)
                .bankCode("023")
                .accountNumber("129839012809480")
                .ownerName("홍길동")
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void 계좌_정상_생성처리() {
        AccountDto.saveAccountParam param = new AccountDto.saveAccountParam();
        param.setBankCode("023");
        param.setAccountNumber("129839012809480");
        param.setOwnerName("홍길동");

        when(accountRepository.findByBankCodeAndAccountNumber(param.getBankCode(), param.getAccountNumber()))
                .thenReturn(Optional.empty());

        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
           Account account = inv.getArgument(0);
           account = Account.builder()
                   .accountId(1L)
                   .bankCode(account.getBankCode())
                   .accountNumber(account.getAccountNumber())
                   .ownerName(account.getOwnerName())
                   .balance(BigDecimal.ZERO)
                   .status(AccountStatus.ACTIVE)
                   .build();

           return account;
        });

        AccountDto.accountInfo result = accountService.saveAccount(param);

        Assertions.assertThat(result.getId()).isEqualTo(1L);
        Assertions.assertThat(result.getOwnerName()).isEqualTo("홍길동");
        Assertions.assertThat(result.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        Assertions.assertThat(result.isNew()).isTrue();
    }

    @Test
    void 중복계좌_생성처리_예와처리() {
        AccountDto.saveAccountParam param = new AccountDto.saveAccountParam();
        param.setBankCode("023");
        param.setAccountNumber("129839012809480");
        param.setOwnerName("홍길동");

        Account exsitedAccount = Account.builder()
                .accountId(1L)
                .bankCode("023")
                .accountNumber("129839012809480")
                .ownerName("홍길동")
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.findByBankCodeAndAccountNumber(param.getBankCode(), param.getAccountNumber()))
                .thenReturn(Optional.of(exsitedAccount));

        Assertions.assertThatThrownBy(() -> accountService.saveAccount(param))
                .isInstanceOf(AccountException.class)
                .hasMessage("이미 존재하는 계좌입니다.");
    }

    @Test
    void 계좌_정상_삭제처리() {
        Account account = createAccount(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        accountService.deleteAccount(1L);

        Assertions.assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    void 존재하지않는_계좌_삭제처리_예외발생() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> accountService.deleteAccount(999L))
                .isInstanceOf(AccountException.class)
                .hasMessage("계좌를 찾을 수 없습니다.");
    }

    @Test
    void 이미_해지처리된_계좌_삭제처리_예외발생() {
        Account account = createAccount(1L);
        account.doModifyStatus(AccountStatus.CLOSED);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Assertions.assertThatThrownBy(() -> accountService.deleteAccount(1L))
                .isInstanceOf(AccountException.class)
                .hasMessage("해지된 계좌입니다.");
    }

    @Test
    void 특정계좌_거래내역_조회_정상처리() {
        Account account = Account.builder()
                .accountId(1L)
                .bankCode("003")
                .accountNumber("12345678901234")
                .ownerName("홍길동")
                .balance(BigDecimal.valueOf(10000))
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.findByBankCodeAndAccountNumber("003", "12345678901234"))
                .thenReturn(Optional.of(account));

        Transit tx1 = Transit.builder()
                .account(account)
                .transactionType(TransactionType.DEPOSIT)
                .amount(BigDecimal.valueOf(300_000))
                .fee(BigDecimal.ZERO)
                .balance(BigDecimal.valueOf(300_000))
                .transitAt(LocalDateTime.now().minusMinutes(5))
                .build();

        Transit tx2 = Transit.builder()
                .account(account)
                .transactionType(TransactionType.WITHDRAW)
                .amount(BigDecimal.valueOf(100_000))
                .fee(BigDecimal.ZERO)
                .balance(BigDecimal.valueOf(200_000))
                .transitAt(LocalDateTime.now())
                .build();

        List<Transit> mockTransits = List.of(tx2, tx1);

        Page<Transit> mockPage = new PageImpl<>(mockTransits);

        when(transitRepository.findByAccountOrderByTransitAtDesc(eq(account), any(Pageable.class)))
                .thenReturn(mockPage);

        Pageable pageable = PageRequest.of(0, 10);

        TransitDto.transitInfo result = transitService.transitHistory("003", "12345678901234", pageable);

        Assertions.assertThat(result.getTransits().getContent()).hasSize(2);

        TransitDto.transit first = result.getTransits().getContent().get(0);
        Assertions.assertThat(first.getTransactionType()).isEqualTo(TransactionType.WITHDRAW);
        Assertions.assertThat(first.getBalance()).isEqualTo(BigDecimal.valueOf(200_000));

        TransitDto.transit second = result.getTransits().getContent().get(1);
        Assertions.assertThat(second.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        Assertions.assertThat(second.getBalance()).isEqualTo(BigDecimal.valueOf(300_000));
    }
}
