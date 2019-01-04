package org.hswebframework.payment.merchant.entity;

import org.hswebframework.payment.api.enums.MerchantStatus;
import org.hswebframework.payment.api.enums.MerchantType;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * 商户信息
 *
 * @author zhouhao
 * @see org.hswebframework.payment.api.merchant.config.MerchantConfigManager
 * @since 1.0.0
 */
@Getter
@Setter
@Table(name = "mer_merchant")
public class MerchantEntity extends SimpleGenericEntity<String> {

    /**
     * 商户名称
     */
    @Column(name = "name")
    private String name;

    /**
     * 联系电话
     */
    @Column(name = "phone")
    private String phone;

    /**
     * 代理商户ID
     */
    @Column(name = "agent_id")
    private String agentId;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 商户管理用户ID
     */
    @Column(name = "user_id")
    private String userId;

    @Column(name = "username")
    private String username;
    /**
     * 资金账户号
     */
    @Column(name = "account_no")
    private String accountNo;

    /**
     * 状态
     */
    @Column(name = "status")
    private MerchantStatus status;

    //=========================备案信息

    /**
     * 商户产品名称
     */
    @Column(name = "product_name")
    private String productName;


    /**
     * 法人姓名
     */
    @Column(name = "legal_person_name")
    private String legalPersonName;

    /**
     * 法人身份证号
     */
    @Column(name = "legal_person_id_card")
    private String legalPersonIdCard;

    /**
     * 公司地址
     */
    @Column(name = "company_address")
    private String companyAddress;

    /**
     * 公司名称
     */
    @Column(name = "company_name")
    private String companyName;
    /**
     * 法人身份证正面
     */
    @Column(name = "id_card_front")
    private String idCardFront;

    /**
     * 法人身份证反面
     */
    @Column(name = "id_card_back")
    private String idCardBack;

    /**
     * 营业执照
     */
    @Column(name = "business_license")
    private String businessLicense;

    /**
     * QQ
     */
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

    /**
     * 商户类型
     */
    @Column(name = "type")
    private MerchantType type;
}
