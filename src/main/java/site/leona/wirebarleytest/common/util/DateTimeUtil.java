package site.leona.wirebarleytest.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

public class DateTimeUtil {

    public Long timeToEpochTimeConvert(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            localDateTime = LocalDateTime.now();
        }

        return localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    public LocalDateTime epochTimeToLocalDateTimeConvert(Long epochTime) {
        if(epochTime == null) {
            return LocalDateTime.now();
        }

        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochTime), ZoneId.systemDefault());
    }


}
