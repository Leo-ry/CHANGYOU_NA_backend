package site.leona.wirebarleytest.enums;

import lombok.Getter;

@Getter
public enum AccountStatus {

    ACTIVE(100, "계좌활성", ""),
    CLOSED(900, "계좌해지", ""),
    SUSPENDED(200, "계좌비활성", ""),
    BANNED(400, "접근차단", ""),
    ETC(000, "기타", ""),
    ;

    private final int code;
    private final String name;
    private final String description;

    AccountStatus(int code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
}
