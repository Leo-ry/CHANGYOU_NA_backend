package site.leona.wirebarleytest.common.exception;

import org.springframework.http.HttpStatus;
import site.leona.wirebarleytest.common.entity.enums.ErrorCode;

public abstract class BaseException extends RuntimeException {
    private final ErrorCode errorCode;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return HttpStatus.valueOf(errorCode.getCode());
    }

    public String getCode() {
        return errorCode.getCode();
    }

    @Override
    public String getMessage() {
        return errorCode.getMessage();
    }
}
