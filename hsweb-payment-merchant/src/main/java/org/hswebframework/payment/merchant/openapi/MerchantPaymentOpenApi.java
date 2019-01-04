package org.hswebframework.payment.merchant.openapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.payment.api.enums.*;
import org.hswebframework.payment.api.merchant.*;
import org.hswebframework.payment.api.merchant.config.MerchantChannelConfig;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.merchant.request.MerchantSubstituteRequest;
import org.hswebframework.payment.api.payment.PaymentRequest;
import org.hswebframework.payment.api.payment.PaymentResponse;
import org.hswebframework.payment.api.payment.PaymentService;
import org.hswebframework.payment.api.payment.order.PaymentOrderService;
import org.hswebframework.payment.api.payment.payee.Payee;
import org.hswebframework.payment.api.payment.quick.QuickPaymentConfirmRequest;
import org.hswebframework.payment.api.payment.quick.QuickPaymentConfirmResponse;
import org.hswebframework.payment.api.payment.quick.QuickPaymentRequest;
import org.hswebframework.payment.api.payment.quick.QuickPaymentResponse;
import org.hswebframework.payment.api.payment.substitute.request.SubstituteDetail;
import org.hswebframework.payment.merchant.openapi.request.*;
import org.hswebframework.payment.openapi.annotation.OpenApi;
import org.hswebframework.payment.openapi.annotation.OpenApiParam;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.bean.BeanValidator;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.*;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@RequestMapping("/open-api/payment")
@Validated
@Slf4j
@Api(value = "支付交易OpenApi", tags = "支付交易OpenApi")
public class MerchantPaymentOpenApi {

    @Autowired
    private MerchantConfigManager configManager;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentOrderService paymentOrderService;

    @Autowired
    private WithdrawService withdrawService;

    @Autowired
    private SubstituteService substituteService;

    @Autowired
    private MerchantService merchantService;

    @PostMapping("/query")
    @OpenApi(value = "query-order", name = "统一支付订单查询接口")
    @ApiOperation("查询订单")
    public QueryPaymentResponse queryPaymentOrder(@OpenApiParam QueryPaymentRequest request) {
        if (StringUtils.isEmpty(request.getOrderId())
                && StringUtils.isEmpty(request.getPaymentId())) {
            throw new ValidationException("参数[orderId]和[paymentId]不能同时为空");
        }
        QueryParamEntity entity = QueryParamEntity.of("merchantId", request.getMerchantId())
                .and("orderId", request.getOrderId())
                .and("id", request.getPaymentId());

        return paymentOrderService.query(entity)
                .getData()
                .stream()
                .findAny()
                .map(order -> {
                    QueryPaymentResponse response = FastBeanCopier.copy(order, QueryPaymentResponse::new);
                    response.setPaymentId(order.getId());
                    response.setSuccess(true);
                    response.setStatus(order.getStatus().getValue());
                    response.setStatusText(order.getStatus().getText());
                    return response;
                })
                .orElseThrow(ErrorCode.ORDER_NOT_EXISTS::createException);
    }

    @PostMapping("/gateway")
    @OpenApi(value = "payment-gateway", name = "网关支付接口")
    @ApiOperation("网关支付")
    public Map<String, Object> requestGatewayPayment(@OpenApiParam @Valid GatewayPaymentRequest request,
                                                     Authentication authentication) {
        //判断是否支持此渠道
        assertSupportChannel(request.getMerchantId(), TransType.GATEWAY, request.getChannel());

        PaymentRequest paymentRequest = FastBeanCopier.copy(request, PaymentRequest::new);
        //授权的用户就是商户
        paymentRequest.setMerchantName(authentication.getUser().getName());
        //设置拓展参数
        Optional.ofNullable(request.getExtraParam())
                .map(JSON::parseObject)
                .map(Map.class::cast)
                .ifPresent(paymentRequest::setExtraParam);
        //设置http通知
        if (StringUtils.hasText(request.getNotifyUrl())) {
            Map<String, Object> notifyConfig = Maps.newHashMap();
            notifyConfig.put("notifyUrl", request.getNotifyUrl());
            paymentRequest.setNotifyType(NotifyType.HTTP);
            paymentRequest.setNotifyConfig(notifyConfig);
        } else {
            paymentRequest.setNotifyType(NotifyType.NONE);
        }
        PaymentResponse response = paymentService.gateway().requestGateWayPayment(paymentRequest);
        response.assertSuccess();
        //转换响应结果
        GatewayPaymentResponse paymentResponse = FastBeanCopier.copy(response, GatewayPaymentResponse::new);
        Map<String, Object> data = FastBeanCopier.copy(response, TreeMap::new);
        Map<String, Object> needRemove = FastBeanCopier.copy(paymentResponse, HashMap::new);
        data.remove("requestId");
        needRemove.keySet().forEach(data::remove);
        Map<String, Object> finalResponse = FastBeanCopier.copy(paymentResponse, HashMap::new);
        finalResponse.putAll(data);

        return finalResponse;
    }

    @PostMapping("/quick-request")
    @OpenApi(value = "payment-quick", name = "快捷支付申请接口")
    @ApiOperation("快捷支付申请")
    public QuickPaymentOpenApiResponse requestQuickPayment(@OpenApiParam @Valid QuickPaymentOpenApiRequest request,
                                                           Authentication authentication) {
        //获取该商户使用的快捷支付渠道
        String channel = getQuickPaymentChannel(request.getMerchantId());

        QuickPaymentRequest quickPaymentRequest = FastBeanCopier.copy(request, QuickPaymentRequest::new);
        quickPaymentRequest.setChannel(channel);
        quickPaymentRequest.setNotifyType(NotifyType.NONE);
        //授权的用户就是商户
        quickPaymentRequest.setMerchantName(authentication.getUser().getName());
        QuickPaymentResponse response = paymentService.quick().requestQuickPayment(quickPaymentRequest);
        response.assertSuccess();
        return FastBeanCopier.copy(response, QuickPaymentOpenApiResponse::new);
    }

    @PostMapping("/quick-confirm")
    @OpenApi(value = "payment-quick", name = "快捷支付确认接口")
    @ApiOperation("快捷支付确认")
    public QuickPaymentOpenApiConfirmResponse confirmQuickPayment(@OpenApiParam @Valid QuickPaymentOpenApiConfirmRequest request) {
        QuickPaymentConfirmRequest confirmRequest = FastBeanCopier.copy(request, QuickPaymentConfirmRequest::new);
        QuickPaymentConfirmResponse confirmResponse = paymentService.quick().confirmQuickPayment(confirmRequest);
        confirmResponse.assertSuccess();
        return FastBeanCopier.copy(confirmResponse, QuickPaymentOpenApiConfirmResponse::new);
    }


    @PostMapping("/transfer")
    @OpenApi(value = "batch-transfer", name = "批量代付接口")
    @SuppressWarnings("all")
    @ApiOperation("批量代付")
    public SubstituteOpenApiResponse substitute(@OpenApiParam @Valid SubstituteOpenApiRequest request) {
        Merchant merchant = merchantService.getMerchantById(request.getMerchantId());

        MerchantSubstituteRequest merchantSubstituteRequest = FastBeanCopier.copy(request, new MerchantSubstituteRequest());

        merchantSubstituteRequest.setMerchantId(merchant.getId());
        merchantSubstituteRequest.setMerchantName(merchant.getName());
        merchantSubstituteRequest.setNotifyUrl(request.getNotifyUrl());
        merchantSubstituteRequest.setPayeeType(PayeeType.of(request.getPayeeType())
                .orElseThrow(() -> new ValidationException("不支持的收款人类型:" + request.getPayeeType())));
        try {
            List<JSONObject> list = JSONObject.parseArray(request.getDetails(), JSONObject.class);
            if (CollectionUtils.isEmpty(list)) {
                throw new ValidationException("代付明细不能为空");
            }
            List<SubstituteDetail<? extends Payee>> details = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                JSONObject detailJson = list.get(i);
                SubstituteOpenApiDetail openApiDetail = detailJson.toJavaObject(SubstituteOpenApiDetail.class);
                Payee payee = detailJson.toJavaObject(merchantSubstituteRequest.getPayeeType().getPayeeType());
                try {
                    BeanValidator.tryValidate(openApiDetail);
                    BeanValidator.tryValidate(payee);
                } catch (org.hswebframework.web.validate.ValidationException e) {
                    throw new ValidationException("明细第" + (i + 1) + "条格式错误:" + e.getMessage());
                }
                SubstituteDetail detail = new SubstituteDetail<>();
                detail.setTransNo(openApiDetail.getTransNo());
                detail.setAmount(openApiDetail.getAmount());
                detail.setPayee(payee);
                detail.setRemark(openApiDetail.getRemark());
                details.add(detail);
            }
            merchantSubstituteRequest.setDetail(details);
        } catch (JSONException e) {
            log.warn("代付请求参数错误", e.getMessage());
            throw new ValidationException("代付明细格式错误");
        }

        MerchantSubstituteResponse response = substituteService.requestSubstitute(merchantSubstituteRequest);
        response.assertSuccess();
        SubstituteOpenApiResponse openApiResponse = new SubstituteOpenApiResponse();
        openApiResponse.setSuccess(response.isSuccess());
        openApiResponse.setTransferId(response.getTransId());
        return openApiResponse;
    }

//    @PostMapping("/apply-withdraw")
//    @OpenApi(name = "申请提现")
//    public WithdrawOpenApiResponse applyWithdraw(@OpenApiParam WithdrawOpenApiRequest request) {
//        ApplyWithdrawRequest withdrawRequest = FastBeanCopier.copy(request, ApplyWithdrawRequest::new);
//        withdrawRequest.setWithdrawType(WithdrawType.MANUAL);
//        withdrawRequest.setApplyTime(new Date());
//        ApplyWithdrawResponse applyWithdrawResponse = withdrawService.applyWithdraw(withdrawRequest);
//        return FastBeanCopier.copy(applyWithdrawResponse, WithdrawOpenApiResponse::new);
//    }

    private String getQuickPaymentChannel(String merchantId) {
        return configManager.<MerchantChannelConfig>getConfigList(merchantId, MerchantConfigKey.SUPPORTED_CHANNEL)
                .flatMap(list -> list.stream()
                        .filter(config -> TransType.QUICK == config.getTransType())
                        .findAny())
                .map(MerchantChannelConfig::getChannel)
                .orElseThrow(ErrorCode.CHANNEL_UNSUPPORTED::createException);
    }

    private void assertSupportChannel(String merchantId, TransType transType, String channel) {
        configManager.<MerchantChannelConfig>getConfigList(merchantId, MerchantConfigKey.SUPPORTED_CHANNEL)
                .flatMap(list -> list.stream()
                        .filter(config -> transType == config.getTransType() && config.getChannel().equals(channel))
                        .findAny())
                .orElseThrow(ErrorCode.CHANNEL_UNSUPPORTED::createException);
    }
}
