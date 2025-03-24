package site.leona.wirebarleytest.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import site.leona.wirebarleytest.entity.Account;
import site.leona.wirebarleytest.model.TransitDto;

public interface TransitService {
    TransitDto.transactionInfo deposit(TransitDto.depositParam param);
    TransitDto.transactionInfo withdraw(TransitDto.withdrawParam param);
    TransitDto.transactionInfo transfer(TransitDto.transferParam param);
    TransitDto.transitInfo transitHistory(String bankCode, String accountNumber, Pageable pageable);
}
