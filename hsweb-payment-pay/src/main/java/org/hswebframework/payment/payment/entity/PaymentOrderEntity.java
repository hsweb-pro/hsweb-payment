package org.hswebframework.payment.payment.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.hswebframework.payment.api.enums.PaymentStatus;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.PaymentRequest;
import org.hswebframework.payment.api.payment.PaymentResponse;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;
import java.util.Optional;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Table
public class PaymentOrderEntity extends SimpleGenericEntity<String> {

    @Column(name = "trans_type")
    private TransType transType;

    @Column(name = "channel")
    private String channel;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "real_amount")
    private Long realAmount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "complete_time")
    private Date completeTime;

    @Column(name = "req_json")
    private String requestJson;

    @Column(name = "res_json")
    private String responseJson;

    @Column(name = "channel_result")
    private String channelResult;

    @Column(name = "is_notified")
    private Boolean notified;

    @Column(name = "notify_time")
    private Date notifyTime;

    @Column(name = "status")
    private PaymentStatus status;

    @Column(name = "channel_provider")
    private String channelProvider;

    @Column(name = "channel_provider_name")
    private String channelProviderName;

    @Column(name = "comment")
    private String comment;

    public <T extends PaymentRequest> T getOriginalRequest(Class<T> type) {
        return Optional.ofNullable(requestJson)
                .map(json -> JSON.parseObject(json, type))
                .orElse(null);
    }

    public <T extends PaymentResponse> T getOriginalResponse(Class<T> type) {
        return Optional.ofNullable(responseJson)
                .map(json -> JSON.parseObject(json, type))
                .orElse(null);
    }

    public String getRequestJsonString() {
        return Optional.ofNullable(requestJson)
                .map(JSON::parseObject)
                .map(obj -> JSON.toJSONString(obj, SerializerFeature.PrettyFormat))
                .orElse(null);
    }

    public String getResponseJsonString() {
        return Optional.ofNullable(responseJson)
                .map(JSON::parseObject)
                .map(obj -> JSON.toJSONString(obj, SerializerFeature.PrettyFormat))
                .orElse(null);
    }

}
