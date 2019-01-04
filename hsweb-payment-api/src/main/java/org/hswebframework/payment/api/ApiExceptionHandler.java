package org.hswebframework.payment.api;

import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.exception.BusinessException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.hswebframework.web.validate.ValidationException;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Component
@Slf4j
public class ApiExceptionHandler extends StaticMethodMatcherPointcutAdvisor {

    public ApiExceptionHandler() {
        setAdvice((MethodInterceptor) invocation -> {
            ApiRequest request = Arrays.stream(invocation.getArguments())
                    .filter(ApiRequest.class::isInstance)
                    .map(ApiRequest.class::cast)
                    .findFirst()
                    .orElse(null);
            try {
                ApiResponse response = (ApiResponse) invocation.proceed();
                if (response == null) {
                    response = (ApiResponse) invocation.getMethod().getReturnType().newInstance();
                }
                if (request != null && response != null) {
                    response.setRequestId(request.getRequestId());
                }
                return response;
            } catch (Throwable e) {
                return handleError(invocation.getMethod().getReturnType(), request, e);
            }
        });
    }

    @SneakyThrows
    protected Object handleError(Class returnType, ApiRequest request, Throwable e) {
        ApiResponse newResponse = (ApiResponse) returnType.newInstance();
        newResponse.setSuccess(false);
        if (e instanceof BusinessException) {
            newResponse.setCode(((BusinessException) e).getCode());
            newResponse.setMessage(e.getMessage());
            log.error("业务异常", e);
        } else if (e instanceof ValidationException || e instanceof javax.validation.ValidationException) {
            newResponse.setCode(ErrorCode.ILLEGAL_PARAMETERS.getValue());
            newResponse.setMessage(e.getMessage());
            log.error("参数错误", e);
        } else if (e instanceof DuplicateKeyException) {
            log.error("重复的请求", e);
            newResponse.setCode(ErrorCode.DUPLICATE_REQUEST.getValue());
            newResponse.setMessage(ErrorCode.DUPLICATE_REQUEST.getText());
        } else {
            log.error("调用服务失败", e);
            newResponse.setCode(ErrorCode.SERVICE_ERROR.getValue());
            newResponse.setMessage(ErrorCode.SERVICE_ERROR.getText());
        }
        if (request != null) {
            newResponse.setRequestId(request.getRequestId());
        }
        return newResponse;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return ApiResponse.class.isAssignableFrom(method.getReturnType());
    }
}
