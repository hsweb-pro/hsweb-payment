package org.hswebframework.payment.merchant.entity;

import lombok.*;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Table
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantConfigEntity extends SimpleGenericEntity<String> {

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "key")
    private String key;

    @Column(name = "value")
    private String value;

    @Column(name = "is_mer_writable")
    private Boolean merchantWritable;
}
