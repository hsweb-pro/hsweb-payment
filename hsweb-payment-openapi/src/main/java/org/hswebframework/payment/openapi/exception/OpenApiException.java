package org.hswebframework.payment.openapi.exception;

import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.exception.BusinessException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenApiException extends RuntimeException {

    private String code;

    public static OpenApiException of(BusinessException error) {
        return new OpenApiException(error.getCode(), error.getMessage(), error.getCause());
    }

    public static OpenApiException of(ErrorCode errorCode) {
        return new OpenApiException(errorCode.getValue(), errorCode.getText());
    }

    public static OpenApiException of(ErrorCode errorCode, Throwable cause) {
        return new OpenApiException(errorCode.getValue(), errorCode.getText(), cause);
    }


    public OpenApiException(String code, String message) {
        super(message);
        this.code = code;
    }

    public OpenApiException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
