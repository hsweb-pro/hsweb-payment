package org.hswebframework.payment.api.account;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class Account implements Serializable {

    private String id;

    private String accountNo;

    private String name;

    private String merchantId;

    private long balance;

    private String currency;

    private long freezeBalance;
}
