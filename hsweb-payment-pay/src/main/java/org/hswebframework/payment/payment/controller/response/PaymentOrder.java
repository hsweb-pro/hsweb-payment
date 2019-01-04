package org.hswebframework.payment.payment.controller.response;

import org.hswebframework.payment.api.enums.PaymentStatus;
import org.hswebframework.payment.api.enums.TransType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentOrder {

    private TransType transType;

    private String channel;

    private String channelName;

    private String channelId;

    private String orderId;

    private String productId;

    private String merchantId;

    private String merchantName;

    private String productName;

    private Long amount;

    private String currency;

    private Date createTime;

    private Date updateTime;

    private Date completeTime;

    private Boolean notified;

    private Date notifyTime;

    private PaymentStatus status;

    private String channelProvider;

    private String channelProviderName;

    private String comment;


    @ApiModelProperty("服务费")
    private Long serviceAmount;

    @ApiModelProperty("费率备注")
    private String serviceComment;

}
