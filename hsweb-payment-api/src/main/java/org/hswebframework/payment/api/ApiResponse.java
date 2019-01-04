package org.hswebframework.payment.api;

import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.exception.BusinessException;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class ApiResponse implements Serializable {

    private String requestId;

    private boolean success;

    private String code;

    private String message;

    public void setError(ErrorCode error) {
        this.code = error.getValue();
        this.message = error.getText();
        this.success=false;
    }

    public void setError(BusinessException error) {
        this.code = error.getCode();
        this.message = error.getMessage();
        this.success=false;
    }

    public ApiResponse assertSuccess() {
        if (!success) {
            throw new BusinessException(code, message);
        }
        return this;
    }

    @SneakyThrows
    public ApiResponse assertSuccess(Function<ApiResponse, Throwable> function) {
        if (!success) {
            throw function.apply(this);
        }
        return this;
    }

    @SneakyThrows
    public ApiResponse assertSuccess(BiFunction<String, String, Throwable> function) {
        if (!success) {
            throw function.apply(code, message);
        }
        return this;
    }
}
