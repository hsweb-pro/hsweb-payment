package org.hswebframework.payment.merchant.service.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorValidateCache implements Serializable {

    private long createTime;

    public boolean isExpire(long expire) {
        return System.currentTimeMillis() - createTime >= expire;
    }
}
