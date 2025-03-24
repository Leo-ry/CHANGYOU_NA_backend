package site.leona.wirebarleytest.common.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GlobalResponse<T> {
    private boolean isSuccess;
    private String message;
    private String code;
    private long startTime;
    private long endTime;
    private String path;
    private T data;

    public static <T> GlobalResponse<T> success(T data, String path, Instant start) {
        Instant end = Instant.now();

        return GlobalResponse.<T>builder()
                .isSuccess(true)
                .startTime(start.getEpochSecond())
                .endTime(end.getEpochSecond())
                .path(path)
                .message("OK")
                .code("200")
                .data(data)

                .build();
    }

    public static <T> GlobalResponse<T> error(String path, String message, Instant start) {
        Instant end = Instant.now();

        return GlobalResponse.<T>builder()
                .isSuccess(false)
                .startTime(start.getEpochSecond())
                .endTime(end.getEpochSecond())
                .path(path)
                .message(message)
                .code("500")
                .data(null)
                .build();
    }
}
