package site.leona.wirebarleytest.common.handler;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import site.leona.wirebarleytest.common.model.GlobalResponse;
import site.leona.wirebarleytest.common.model.RequestContext;

import java.time.Instant;

@RestControllerAdvice(basePackages = "site.leona.wirebarleytest.api")
public class DefaultHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // 기본 RestController 반환시 시작시간, 종료시간, 그리고 기본 호출 주소 정도는 찍어줘야 ... 알아보기 편함
        Instant start = RequestContext.getStartTime();
        String path = ((ServletServerHttpRequest) request).getServletRequest().getRequestURI();

        // 혹시라도 내부에서 잘못 루프 돌아서 이미 패키징이 된 경우 그냥 그대로 던져던져!
        if (body instanceof GlobalResponse) {
            return body;
        }

        return GlobalResponse.success(body, path, start);
    }
}
