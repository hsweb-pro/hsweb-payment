package org.hswebframework.payment.api.enums;

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
@Dict(id = "user-log-type")
public enum UserLogType implements EnumDict<String> {
    LOGIN("登录"),
    UPDATE_PWD("修改密码"),
    WITHDRAW("提现");

    private String text;

    @Override
    public String getValue() {
        return name();
    }
}
