package site.leona.wirebarleytest.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import site.leona.wirebarleytest.common.model.RequestContext;

import java.time.Instant;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Instant instant = Instant.now();
        RequestContext.setStartTime(instant);

        request.setAttribute("startTime", instant.getEpochSecond());
        log.info("[Request Start] Method : {}  ||  URI : {}  ||  Call Time : {}", request.getMethod(), request.getRequestURI(), instant.getEpochSecond());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        RequestContext.clear();
        log.info("[Response Start] Method : {}  ||  URI : {}", request.getMethod(), request.getRequestURI());
    }
}
