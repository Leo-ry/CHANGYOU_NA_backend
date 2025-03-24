package site.leona.wirebarleytest.service;

import site.leona.wirebarleytest.model.AccountDto;

public interface AccountService {

    AccountDto.accountInfo saveAccount(AccountDto.saveAccountParam param);
    AccountDto.accountInfo deleteAccount(Long id);
}
