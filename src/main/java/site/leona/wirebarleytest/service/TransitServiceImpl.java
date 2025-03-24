package site.leona.wirebarleytest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.leona.wirebarleytest.common.entity.enums.ErrorCode;
import site.leona.wirebarleytest.common.exception.AccountException;
import site.leona.wirebarleytest.entity.Account;
import site.leona.wirebarleytest.entity.Transit;
import site.leona.wirebarleytest.enums.AccountStatus;
import site.leona.wirebarleytest.enums.TransactionType;
import site.leona.wirebarleytest.model.TransitDto;
import site.leona.wirebarleytest.repository.AccountRepository;
import site.leona.wirebarleytest.repository.TransitRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class TransitServiceImpl implements TransitService {

    // 출금 일제한 한도 일백만원
    private static final BigDecimal DAILY_WITHDRAW_LIMIT = BigDecimal.valueOf(1_000_000);
    private static final BigDecimal DAILY_TRANSFER_LIMIT = BigDecimal.valueOf(3_000_000);

    private final AccountRepository accountRepository;
    private final TransitRepository transitRepository;

    @Override
    public TransitDto.transactionInfo deposit(TransitDto.depositParam param) {
        // RestController 에서만 호출하는게 아닐 수도 있으니 방어로직, DB 콜전에 호출하는게 좋음
        if (param.getAmount() == null || param.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountException(ErrorCode.VALIDATION_ERROR);
        }

        // 입금하고자 하는 계좌 검색
        Account targetAccount = accountRepository.findByBankCodeAndAccountNumber(param.getBankCode(), param.getAccountNumber())
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (targetAccount.getStatus() == AccountStatus.ACTIVE) {
            param.toDeposit().accept(targetAccount);
            accountRepository.save(targetAccount);

            return new TransitDto.transactionInfo(targetAccount.getAccountId(), targetAccount.getBalance(), TransactionType.DEPOSIT);
        } else {
            throw new AccountException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
    }

    @Override
    public TransitDto.transactionInfo withdraw(TransitDto.withdrawParam param) {
        // RestController 에서만 호출하는게 아닐 수도 있으니 방어로직, DB 콜전에 호출하는게 좋음
        if (param.getAmount() == null || param.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountException(ErrorCode.VALIDATION_ERROR);
        }

        // 출금하고자 하는 계좌 검색
        Account targetAccount = accountRepository.findByBankCodeAndAccountNumber(param.getBankCode(), param.getAccountNumber())
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        if(targetAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }

        // 계좌의 잔액이 출금하고자하는 금액보다 작은 경우 예외처리
        if(targetAccount.getBalance().compareTo(param.getAmount()) < 0) {
            throw new AccountException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // 해당 출금 계좌의 일한도 제한 확인해서 예외처리
        BigDecimal todayTotal = getTodayWithdrawTotal(targetAccount);

        if (todayTotal.add(param.getAmount()).compareTo(DAILY_WITHDRAW_LIMIT) > 0) {
            throw new AccountException(ErrorCode.DAILY_WITHDRAW_LIMIT_EXCEEDED);
        }

        param.toWithdraw().accept(targetAccount);
        accountRepository.save(targetAccount);
        return new TransitDto.transactionInfo(targetAccount.getAccountId(), targetAccount.getBalance(), TransactionType.WITHDRAW);
    }

    @Override
    public TransitDto.transactionInfo transfer(TransitDto.transferParam param) {
        if (param.getAmount() == null || param.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountException(ErrorCode.VALIDATION_ERROR);
        }

        // 출금 계좌 체크
        Account sender = accountRepository.findByBankCodeAndAccountNumber(param.getFromBankCode(), param.getFromAccountNumber())
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        // 입금 계좌 체크
        Account receiver = accountRepository.findByBankCodeAndAccountNumber(param.getToBankCode(), param.getToAccountNumber())
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (sender.getStatus() != AccountStatus.ACTIVE || receiver.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }

        // 수수료 1%계산 -> 별도로 수수료테이블 관리에 대해서 이야기하지않음 그냥 박아버림
        BigDecimal fee = param.getAmount().multiply(BigDecimal.valueOf(0.01)).setScale(0, RoundingMode.DOWN);

        // 출금계좌의 금액보다 이체금액(원금 + 수수료)이 큰 경우 예외처리
        if (sender.getBalance().compareTo(param.getAmount().add(fee)) < 0) {
            throw new AccountException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // 이체한도 제한 체크
        BigDecimal todayTotal = getTodayTransferTotal(sender);

        if (todayTotal.add(param.getAmount()).compareTo(DAILY_TRANSFER_LIMIT) > 0) {
            throw new AccountException(ErrorCode.DAILY_TANSFER_LIMIT_EXCEEDED);
        }

        param.toTransfer(false, fee, receiver.getBankCode(), receiver.getAccountNumber()).accept(sender);
        param.toTransfer(true, BigDecimal.ZERO, sender.getBankCode(), sender.getAccountNumber()).accept(receiver);

        // 한번에 저장
        accountRepository.save(sender);
        accountRepository.save(receiver);

        return new TransitDto.transactionInfo(receiver.getAccountId(), receiver.getBalance(), TransactionType.TRANSFER);
    }

    @Override
    public TransitDto.transitInfo transitHistory(String bankCode, String accountNumber, Pageable pageable) {
        Account account = accountRepository.findByBankCodeAndAccountNumber(bankCode, accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        Page<TransitDto.transit> transits = transitRepository.findByAccountOrderByTransitAtDesc(account, pageable)
                .map(t -> new TransitDto.transit(t.getTransitId(), t.getAmount(), t.getFee()
                        ,t.getBalance(), t.getTransactionType(), t.getRelatedBankCode()
                        , t.getRelatedAccountNumber(), t.getTransitAt()));

        return new TransitDto.transitInfo(account.getAccountId(), account.getBalance(), transits);
    }

    private BigDecimal getTodayWithdrawTotal(Account account) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<Transit> withdraws = transitRepository.findByAccountAndTransactionTypeAndTransitAtBetween(
                account, TransactionType.WITHDRAW, start, end
        );

        return withdraws.stream().map(Transit::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getTodayTransferTotal(Account account) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<Transit> transfer = transitRepository.findByAccountAndTransactionTypeAndTransitAtBetween(
                account, TransactionType.TRANSFER_OUT, start, end
        );

        return transfer.stream().map(Transit::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

