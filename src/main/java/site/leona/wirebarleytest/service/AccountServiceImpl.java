package site.leona.wirebarleytest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.leona.wirebarleytest.common.entity.enums.ErrorCode;
import site.leona.wirebarleytest.common.exception.AccountException;
import site.leona.wirebarleytest.entity.Account;
import site.leona.wirebarleytest.enums.AccountStatus;
import site.leona.wirebarleytest.model.AccountDto;
import site.leona.wirebarleytest.repository.AccountRepository;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public AccountDto.accountInfo saveAccount(AccountDto.saveAccountParam param) {
        // 기존 같은 계좌가 있는지 판단
        Optional<Account> oldAccount = accountRepository.findByBankCodeAndAccountNumber(param.getBankCode(), param.getAccountNumber());

        if (oldAccount.isPresent()) {
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
        }

        Account account = accountRepository.save(param.toAccount().get());
        return new AccountDto.accountInfo(account.getAccountId(), account.getOwnerName(), account.getStatus(), account.getCreatedAt(), true);
    }

    @Override
    public AccountDto.accountInfo deleteAccount(Long id) {
        // 요청한 계좌가 있는지 판단
        Optional<Account> account = accountRepository.findById(id);

        if (account.isEmpty()) {
            throw new AccountException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        if (account.get().getStatus() == AccountStatus.CLOSED) {
            // 이미 비활성 계좌인 경우 삭제처리하지않고 예외 처리
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_CLOSED);
        }

        account.get().doModifyStatus(AccountStatus.CLOSED);
        accountRepository.save(account.get());

        return new AccountDto.accountInfo(account.get().getAccountId(), account.get().getOwnerName(), account.get().getStatus(), account.get().getCreatedAt(), false);
    }
}
