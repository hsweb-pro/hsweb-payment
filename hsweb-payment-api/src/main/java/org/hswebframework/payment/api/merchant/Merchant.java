package org.hswebframework.payment.api.merchant;

import org.hswebframework.payment.api.enums.BankCode;
import org.hswebframework.payment.api.enums.MerchantStatus;
import org.hswebframework.payment.api.enums.MerchantType;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 商户信息
 *
 * @author zhouhao
 * @see MerchantConfigManager
 * @since 1.0.0
 */
@Getter
@Setter
public class Merchant {
    private String id;

    private String name;

    private String phone;

    private String agentId;

    private Date createTime;

    private String userId;

    private String username;

    private String accountNo;

    private MerchantStatus status;

    private BankCode bankId;

    /**
     * 商户产品名称
     */
    private String productName;


    /**
     * 法人姓名
     */
    private String legalPersonName;

    /**
     * 法人身份证号
     */
    private String legalPersonIdCard;

    /**
     * 公司地址
     */
    private String companyAddress;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 法人身份证正面
     */
    private String idCardFront;

    /**
     * 法人身份证反面
     */
    private String idCardBack;

    /**
     * 营业执照
     */
    private String businessLicense;

    /**
     * QQ
     */
    private String qq;

    /**
     * 微信
     */
    private String weChat;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 商户类型
     */
    @NotNull
    private MerchantType type;


}
