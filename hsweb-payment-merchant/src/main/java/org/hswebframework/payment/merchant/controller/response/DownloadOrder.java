package org.hswebframework.payment.merchant.controller.response;


import lombok.*;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DownloadOrder {

    private String id;

    private String transType;

    private String channel;

    private String channelName;

    private String channelId;

    private String orderId;

    private String productId;

    private String merchantId;

    private String merchantName;

    private String productName;

    private String amount;

    private String currency;

    private String createTime;

    private String updateTime;

    private String completeTime;

    private String requestJson;

    private String responseJson;

    private String notified;

    private String notifyTime;

    private String status;

    private String channelProvider;

    private String channelProviderName;

    private String comment;
}
