package org.hswebframework.payment.payment.entity;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * 商户渠道绑定表
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Table(name = "pay_mer_channel_bind")
public class MerchantBindPaymentChannelEntity extends SimpleGenericEntity<String> {

    @Column(name = "config_id")
    private String configId;

    @Column(name = "config_name")
    private String configName;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "merchant_name")
    private String merchantName;

}
