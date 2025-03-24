package site.leona.wirebarleytest.common.entity.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    ACCOUNT_NOT_FOUND("ACCOUNT_404", "계좌를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ACCOUNT_ALREADY_EXISTS("ACCOUNT_409", "이미 존재하는 계좌입니다.", HttpStatus.CONFLICT),
    ACCOUNT_ALREADY_CLOSED("ACCOUNT_410", "해지된 계좌입니다.", HttpStatus.GONE),
    ACCOUNT_NOT_ACTIVE("ACCOUNT_403", "활성 계좌가 아닙니다.", HttpStatus.FORBIDDEN),
    INSUFFICIENT_BALANCE("BALANCE_400", "계좌의 잔액이 부족합니다.", HttpStatus.BAD_REQUEST),
    DAILY_WITHDRAW_LIMIT_EXCEEDED("BALANCE_421", "일 출금한도 초과입니다.", HttpStatus.CONFLICT),
    DAILY_TANSFER_LIMIT_EXCEEDED("BALANCE_411", "일 이체한도 초과입니다.", HttpStatus.CONFLICT),
    VALIDATION_ERROR("COMMON_400", "입력값이 잘못되었습니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("SYSTEM_500", "예기치 못한 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
