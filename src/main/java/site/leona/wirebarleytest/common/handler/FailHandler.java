package site.leona.wirebarleytest.common.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.leona.wirebarleytest.common.exception.BaseException;
import site.leona.wirebarleytest.common.model.GlobalResponse;
import site.leona.wirebarleytest.common.model.RequestContext;

import java.time.Instant;

@RestControllerAdvice(basePackages = "site.leona.wirebarleytest.api")
public class FailHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<GlobalResponse<Object>> businessException(BaseException e, HttpServletRequest request) {
        Instant start = RequestContext.getStartTime();
        return ResponseEntity.status(e.getStatus()).body(GlobalResponse.error(request.getRequestURI(), e.getMessage(), start));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse<Object>> systemException(Exception e, HttpServletRequest request) {
        Instant start = RequestContext.getStartTime();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GlobalResponse.error(request.getRequestURI(), "예기치 못한 오류가 발생됨.", start));
    }

}
