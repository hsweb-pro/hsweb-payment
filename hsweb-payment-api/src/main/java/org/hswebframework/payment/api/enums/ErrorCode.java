package org.hswebframework.payment.api.enums;

import com.alibaba.fastjson.annotation.JSONType;
import org.hswebframework.payment.api.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
@Dict(id = "error-code")
@JSONType(deserializer = EnumDict.EnumDictJSONDeserializer.class)
public enum ErrorCode implements EnumDict<String> {

    /*========================公共错误信息====================================*/
    ILLEGAL_PARAMETERS("参数不合法"),
    DUPLICATE_REQUEST("重复的请求"),
    VERIFY_CODE_ERROR("验证码错误"),

    /*========================OpenApi相关====================================*/
    SIGN_ERROR("签名错误"),
    UNSUPPORTED_SIGN_TYPE("不支持的签名类型"),
    ACCESS_DENIED("无此接口的访问权限"),
    BUSINESS_FAILED("业务异常"),
    SERVICE_ERROR("服务异常"),
    SERVICE_BUSY("服务繁忙"),
    /*========================支付相关====================================*/
    CHANNEL_UNSUPPORTED("不支持的支付渠道"),
    CHANNEL_RETURN_ERROR("渠道返回错误"),
    CHANNEL_CONFIG_ERROR("渠道配置错误"),
    DUPLICATE_PAYMENT("重复的支付请求"),
    BIND_CARD_NOT_EXISTS("绑卡信息不存在"),
    ORDER_NOT_EXISTS("订单不存在"),
    BIND_CARD_ALREADY_BIND("已经完成绑卡"),
    CHANEL_OUT_OF_LIMIT("渠道已超过限额"),
    /*========================商户相关====================================*/
    MERCHANT_CONFIG_ERROR("商户配置错误"),
    MERCHANT_FREEZE("商户已被冻结"),
    MERCHANT_NOT_ACTIVE("商户未激活"),
    MERCHANT_NOT_EXISTS("商户未注册"),
    USERNAME_ALREADY_EXISTS("用户名已被占用"),
    WITHDRAW_ALREADY_EXISTS("已存在未处理的提现申请"),
    /*========================资金账户相关====================================*/
    ACCOUNT_NOT_FOUND("资金账户不存在"),
    INSUFFICIENT_BALANCE("余额不足"),
    FREEZE_ERROR("冻结/解冻失败"),
    DUPLICATE_FREEZE("重复解冻"),

    ;
    private String text;

    @Override
    public String getValue() {
        return name().toLowerCase();
    }

    public void throwError() {
        throw createException();
    }

    public BusinessException createException() {
        return new BusinessException(getValue(), getText());
    }

    public BusinessException createException(String message) {
        return new BusinessException(getValue(), message);
    }

    public BusinessException createException(String message, Throwable cause) {
        return new BusinessException(getValue(), message, cause);
    }

    public BusinessException createException(String code, String message) {
        return new BusinessException(code, message);
    }

    public BusinessException createException(String code, String message, Throwable cause) {
        return new BusinessException(code, message, cause);
    }

    public BusinessException createException(Throwable cause) {
        return new BusinessException(getValue(), getText(), cause);
    }

    public void throwError(Throwable cause) {
        throw createException(cause);
    }

    @Override
    public boolean isWriteJSONObjectEnabled() {
        return false;
    }
}
