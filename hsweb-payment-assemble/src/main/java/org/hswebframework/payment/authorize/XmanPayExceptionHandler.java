package org.hswebframework.payment.authorize;

import org.hswebframework.payment.api.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestControllerAdvice
@Slf4j
public class XmanPayExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseMessage handleException(BusinessException e) {
        log.error("业务异常", e);
        return ResponseMessage.error(500, e.getMessage())
                .code(e.getCode());
    }
}
