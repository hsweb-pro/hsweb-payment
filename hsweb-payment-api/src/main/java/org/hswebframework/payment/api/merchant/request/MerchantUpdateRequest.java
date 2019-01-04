package org.hswebframework.payment.api.merchant.request;

import org.hswebframework.payment.api.ApiRequest;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class MerchantUpdateRequest extends ApiRequest {

    @NotBlank(message = "商户ID不能为空")
    private String merchantId;

    private String name;

    private String phone;

    private String username;

    private String password;

    //上级代理Id
    private String agentId;

    private String email;

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

}
