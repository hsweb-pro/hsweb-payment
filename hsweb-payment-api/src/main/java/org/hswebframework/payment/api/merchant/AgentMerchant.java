package org.hswebframework.payment.api.merchant;

import org.hswebframework.payment.api.enums.MerchantStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class AgentMerchant implements Serializable {

    private String id;

    //商户名
    private String name;

    //虚拟资金账户
    private String accountNo;

    //上级代理ID
    private String parentId;

    //管理用户ID
    private String userId;

    private MerchantStatus status;

    private Date createTime;

    private String phone;

    private String qq;

    /**
     * 微信
     */
    private String weChat;

    /**
     * 邮箱
     */
    private String email;
}
