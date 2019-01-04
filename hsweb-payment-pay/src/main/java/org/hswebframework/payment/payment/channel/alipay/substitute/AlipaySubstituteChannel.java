package org.hswebframework.payment.payment.channel.alipay.substitute;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayFundBatchDetailQueryRequest;
import com.alipay.api.request.AlipayFundBatchTransferRequest;
import com.alipay.api.response.AlipayFundBatchDetailQueryResponse;
import com.alipay.api.response.AlipayFundBatchTransferResponse;
import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.PayeeType;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.ActiveQuerySupportPaymentChannel;
import org.hswebframework.payment.api.payment.ChannelProvider;
import org.hswebframework.payment.api.payment.order.PaymentOrder;
import org.hswebframework.payment.api.payment.payee.Payee;
import org.hswebframework.payment.api.payment.substitute.SubstituteChannel;
import org.hswebframework.payment.api.payment.substitute.SubstituteDetailCompleteEvent;
import org.hswebframework.payment.api.payment.substitute.request.SubstituteRequest;
import org.hswebframework.payment.api.utils.Money;
import org.hswebframework.payment.payment.channel.AbstractPaymentChannel;
import org.hswebframework.payment.payment.channel.alipay.AlipayConfig;
import org.hswebframework.payment.payment.notify.ChannelNotificationResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.bean.FastBeanCopier;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 支付宝批量代付渠道
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
@RestController
public class AlipaySubstituteChannel extends AbstractPaymentChannel<AlipaySubstituteConfig, SubstituteRequest<Payee>, AlipaySubstituteResponse>
        implements SubstituteChannel<Payee, AlipaySubstituteResponse>
        , ActiveQuerySupportPaymentChannel {
    @Override
    public PayeeType getPayeeType() {
        return PayeeType.ALIPAY;
    }

    private AlipayClient createAlipayClient(AlipaySubstituteConfig config) {
        return new DefaultAlipayClient(
                config.getUrl(), config.getAppId(), config.getRsaPrivateKey(),
                "json", "utf-8", config.getPublicKey(), config.getSignType(),
                config.getProxyHost(),
                config.getProxyPort());
    }

    @Override
    public TransType getTransType() {
        return TransType.SUBSTITUTE;
    }

    @PostMapping(value = "/alipay/batch-trans/notify/{paymentId}", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String channelNotify(@PathVariable String paymentId, @RequestParam Map<String, String> params) {
        AlipayConfig config = getChannelConfig(paymentId);

        if (config == null) {
            log.error("收到支付宝异步通知,但是未获取到渠道配置[{}],通知内容:{}", paymentId, params);
            return "fail";
        }
        try {
            boolean verifySuccess = AlipaySignature.rsaCheckV1(params, config.getPublicKey(), "utf-8", config.getSignType());
            if (!verifySuccess) {
                log.warn("验证签名失败:channelId:[{}],data:{}", config.getId(), params);
                return "fail";
            }
            //每条记录以“|”间隔

            //格式为：流水号^收款方账号^收款账号姓名^付款金额^成功标识(S)^成功原因(null)^支付宝内部流水号^完成时间。
            String successDetail = params.get("success_details");
            AtomicLong successAmount = new AtomicLong();
            if (StringUtils.hasText(successDetail)) {
                Arrays.stream(successDetail.split("[|]"))
                        .map(str -> str.split("[\\^]"))
                        .forEach(arr -> {
                            log.debug("支付宝代付成功:{}", String.join(",", arr));
                            String id = arr[0];
                            String acc = arr[1];
                            String name = arr[2];
                            long amount = Money.amout(arr[3]).getCent();
                            String remark = arr[4];
                            successAmount.addAndGet(amount);
                            eventPublisher.publishEvent(SubstituteDetailCompleteEvent.builder()
                                    .amount(amount)
                                    .detailId(id)
                                    .paymentId(paymentId)
                                    .memo("成功:" + remark)
                                    .success(true)
                                    .build());
                        });
            }

            //格式为：流水号^收款方账号^收款账号姓名^付款金额^失败标识(F)^失败原因^支付宝内部流水号^完成时间。
            String failedDetail = params.get("fail_details");
            if (StringUtils.hasText(failedDetail)) {
                Arrays.stream(failedDetail.split("[|]"))
                        .map(str -> str.split("[\\^]"))
                        .forEach(arr -> {
                            log.debug("支付宝代付成功:{}", String.join(",", arr));
                            String id = arr[0];
                            String acc = arr[1];
                            String name = arr[2];
                            String amount = arr[3];
                            String code = arr[4];
                            String remark = arr[6];
                            eventPublisher.publishEvent(SubstituteDetailCompleteEvent.builder()
                                    .amount(Money.amout(amount).getCent())
                                    .detailId(id)
                                    .paymentId(paymentId)
                                    .memo(code + ":" + remark)
                                    .success(false)
                                    .build());
                        });
            }

            afterHandleChannelNotify(ChannelNotificationResult.builder()
                    .paymentId(paymentId)
                    .success(successAmount.get() > 0)
                    .amount(successAmount.get())
                    .resultObject(params)
                    .build());
            return "success";
        } catch (Exception e) {
            log.warn("处理支付宝代付通知失败", e);
        }
        return "fail";
    }

    @Override
    protected AlipaySubstituteResponse doRequestPay(AlipaySubstituteConfig config, SubstituteRequest<Payee> request) {
        AlipaySubstituteResponse response = FastBeanCopier.copy(request, new AlipaySubstituteResponse());

        AlipayClient client = createAlipayClient(config);
        try {
            AtomicLong totalAmount = new AtomicLong();

            List<AccTransDetail> details = request.getDetails().stream()
                    .map(detail -> {
                        AccPayeeInfo payeeInfo = new AccPayeeInfo();
                        payeeInfo.setPayeeAccount(detail.getPayee().getPayee());
                        payeeInfo.setPayeeName(detail.getPayee().getPayeeName());
                        totalAmount.addAndGet(detail.getAmount());
                        AccTransDetail accTransDetail = new AccTransDetail();
                        accTransDetail.setDetailNo(detail.getId());
                        accTransDetail.setPayeeInfo(payeeInfo);
                        accTransDetail.setRemark(detail.getRemark());
                        accTransDetail.setTransAmount(Money.cent(detail.getAmount()).toString());
                        return accTransDetail;
                    }).collect(Collectors.toList());

            AlipayFundBatchTransferModel model = new AlipayFundBatchTransferModel();
            model.setTotalCount(String.valueOf(request.getDetails().size()));
            model.setTotalTransAmount(Money.cent(totalAmount.get()).toString());
            model.setAccDetailList(details);
            model.setBatchNo(request.getPaymentId());
            model.setPayerAccountType(config.getAccountType());
            model.setPayerAccount(config.getAccountName());

            model.setBizCode("BATCH_TRANS_ACC");
            model.setBizScene("LOCAL");

            AlipayFundBatchTransferRequest transferRequest = new AlipayFundBatchTransferRequest();
            transferRequest.setNotifyUrl(getNotifyLocation(config) + "alipay/batch-trans/notify/" + request.getPaymentId());
            transferRequest.setBizModel(model);
            AlipayFundBatchTransferResponse channelResponse = client.execute(transferRequest);
            String batchNo = channelResponse.getBatchNo();
            String batchTransId = channelResponse.getBatchTransId();
            response.setBatchNo(batchNo);
            response.setBatchTransId(batchTransId);
            response.setSuccess(channelResponse.isSuccess());
            response.setMessage(channelResponse.getSubMsg());
            return response;
        } catch (AlipayApiException e) {
            log.error("发起支付宝代付失败:", e);
            response.setError(ErrorCode.CHANNEL_RETURN_ERROR.createException(e.getErrMsg()));
        } catch (Exception e) {
            log.error("发起支付宝代付失败:", e);
            response.setError(ErrorCode.SERVICE_ERROR);
        }
        return response;
    }

    @Override
    public String getChannel() {
        return "alipay-substitute";
    }

    @Override
    public String getChannelName() {
        return "支付宝批量付款";
    }

    @Override
    @SneakyThrows
    public void doActiveQueryOrderResult(PaymentOrder order) {
        AlipaySubstituteConfig config = getConfigurator().getPaymentConfigById(order.getChannelId());
        AlipayClient client = createAlipayClient(config);

        OriginalRequestResponse requestResponse = getOriginalRequestResponse(order.getId());

        AlipaySubstituteResponse substituteResponse = requestResponse.getResponse();


        AlipayFundBatchDetailQueryModel model = new AlipayFundBatchDetailQueryModel();

        model.setBatchNo(substituteResponse.getBatchNo());
        model.setBizCode("BATCH_TRANS_ACC");
        model.setBizScene("LOCAL");
        AlipayFundBatchDetailQueryRequest request = new AlipayFundBatchDetailQueryRequest();
        request.setBizModel(model);
        AlipayFundBatchDetailQueryResponse response = client.execute(request);

        if (response.isSuccess()) {
            List<AccDetailModel> models = response.getAccDetailList();
            if (!CollectionUtils.isEmpty(models)) {
                //判断整个批次是否全部完成
                for (AccDetailModel detailModel : models) {
                    boolean processing = "INIT".equalsIgnoreCase(detailModel.getStatus())
                            || "APPLIED".equalsIgnoreCase(detailModel.getStatus())
                            || "DEALED".equalsIgnoreCase(detailModel.getStatus());

                    //有明细还在处理中则退出
                    if (processing) {
                        return;
                    }
                }
                long successAmount = 0;
                for (AccDetailModel detailModel : models) {
                    boolean success = "SUCCESS".equalsIgnoreCase(detailModel.getStatus());
                    long amount = Money.amout(detailModel.getTransAmount()).getCent();
                    if (success) {
                        successAmount += amount;
                    }
                    eventPublisher.publishEvent(SubstituteDetailCompleteEvent.builder()
                            .amount(amount)
                            .detailId(detailModel.getDetailNo())
                            .paymentId(order.getId())
                            .memo(detailModel.getErrorMsg())
                            .success(success)
                            .build());
                }
                afterHandleChannelNotify(ChannelNotificationResult.builder()
                        .amount(successAmount)
                        .success(successAmount > 0)
                        .paymentId(order.getId())
                        .build());
            }
        }
    }

    @Override
    public String getChannelProvider() {
        return ChannelProvider.officialAlipay;
    }

    @Override
    public String getChannelProviderName() {
        return "官方支付宝";
    }
}
