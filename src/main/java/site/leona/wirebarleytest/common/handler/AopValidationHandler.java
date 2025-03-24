package site.leona.wirebarleytest.common.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import site.leona.wirebarleytest.common.model.GlobalResponse;
import site.leona.wirebarleytest.common.model.RequestContext;

import java.time.Instant;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(basePackages = "site.leona.wirebarleytest.api")
public class AopValidationHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse<Object>> methodArgValid(MethodArgumentNotValidException e, HttpServletRequest request) {
        Instant start = RequestContext.getStartTime();
        String errorMsg = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(","));

        return ResponseEntity.badRequest().body(GlobalResponse.error(request.getRequestURI(), errorMsg, start));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<GlobalResponse<Object>> HandlerMethodValid(HandlerMethodValidationException e, HttpServletRequest request) {
        Instant start = RequestContext.getStartTime();
        String errorMsg = e.getParameterValidationResults().stream()
                .flatMap(res
                        -> res.getResolvableErrors()
                        .stream()
                        .map(err
                                -> res.getMethodParameter().getParameterName() + ": " + err.getDefaultMessage()))
                .collect(Collectors.joining(","));
        return ResponseEntity.badRequest().body(GlobalResponse.error(request.getRequestURI(), errorMsg, start));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GlobalResponse<Object>> constraintViolation(ConstraintViolationException e, HttpServletRequest request) {
        Instant start = RequestContext.getStartTime();
        String errorMsg = e.getConstraintViolations()
                .stream()
                .map(err -> err.getPropertyPath() + ": " + err.getMessage())
                .collect(Collectors.joining(","));

        return ResponseEntity.badRequest().body(GlobalResponse.error(request.getRequestURI(), errorMsg, start));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<GlobalResponse<Object>> missingParameter(MissingServletRequestParameterException e, HttpServletRequest request) {
        Instant start = RequestContext.getStartTime();
        String errorMsg = e.getParameterName() + " 가 누락되었습니다.";

        return ResponseEntity.badRequest().body(GlobalResponse.error(request.getRequestURI(), errorMsg, start));
    }
}
