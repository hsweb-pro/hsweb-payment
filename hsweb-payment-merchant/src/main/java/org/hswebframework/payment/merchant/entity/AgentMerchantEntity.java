package org.hswebframework.payment.merchant.entity;

import org.hswebframework.payment.api.enums.MerchantStatus;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * 代理
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Table(name = "mer_agent")
public class AgentMerchantEntity extends SimpleGenericEntity<String> {

    //商户名
    @Column(name = "name")
    private String name;

    //虚拟资金账户
    @Column(name = "account_no", updatable = false)
    private String accountNo;

    //上级代理ID
    @Column(name = "parent_id")
    private String parentId;

    //管理用户ID
    @Column(name = "user_id", updatable = false)
    private String userId;

    //管理账户用户名
    @Column(name = "username")
    private String username;

    @Column(name = "status")
    private MerchantStatus status;

    @Column(name = "create_time", updatable = false)
    private Date createTime;

    @Column(name = "phone")
    private String phone;

    @Column(name = "qq")
    private String qq;

    /**
     * 微信
     */
    @Column(name = "we_chat")
    private String weChat;

    /**
     * 邮箱
     */
    @Column(name = "email")
    private String email;

}
