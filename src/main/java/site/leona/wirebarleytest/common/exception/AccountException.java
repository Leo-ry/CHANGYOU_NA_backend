package site.leona.wirebarleytest.common.exception;

import site.leona.wirebarleytest.common.entity.enums.ErrorCode;

public class AccountException extends BaseException {

    public AccountException(ErrorCode errorCode) {
        super(errorCode);
    }
}
