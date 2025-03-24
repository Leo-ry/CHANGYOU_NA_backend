package site.leona.wirebarleytest.common.model;

import java.time.Instant;

public class RequestContext {
    private static final ThreadLocal<Instant> REQUEST_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    public static void setStartTime(Instant instant) {
        REQUEST_CONTEXT_THREAD_LOCAL.set(instant);
    }

    public static Instant getStartTime() {
        return REQUEST_CONTEXT_THREAD_LOCAL.get();
    }

    public static void clear() {
        REQUEST_CONTEXT_THREAD_LOCAL.remove();
    }
}
