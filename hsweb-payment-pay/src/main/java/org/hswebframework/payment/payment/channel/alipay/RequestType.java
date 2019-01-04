package org.hswebframework.payment.payment.channel.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayRequest;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.payment.PaymentRequest;
import org.hswebframework.payment.api.payment.PaymentResponse;
import org.hswebframework.payment.api.utils.Money;
import org.springframework.util.StringUtils;

public enum RequestType {
        CODE {
            @Override
            AlipayRequest createRequest(PaymentRequest request, String notifyUrl) {
                AlipayTradePrecreateRequest alipayRequest = new AlipayTradePrecreateRequest();
                AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
                model.setBody(request.getProductName());
                model.setSubject(request.getProductName());
                model.setOutTradeNo(request.getPaymentId());
                model.setTotalAmount(Money.cent(request.getAmount()).getAmount().toString());
                model.setTimeoutExpress("10m");
                alipayRequest.setBizModel(model);
                alipayRequest.setNotifyUrl(notifyUrl);
                alipayRequest.setReturnUrl(request.getReturnUrl());
                return alipayRequest;
            }

            @Override
            PaymentResponse execute(AlipayClient client, AlipayRequest aliRequest) throws AlipayApiException {
                com.alipay.api.AlipayResponse alipayResponse = client.execute(aliRequest);
                AlipayQrCodeResponse response = new AlipayQrCodeResponse();
                String code = ((AlipayTradePrecreateResponse) alipayResponse).getQrCode();
                if (StringUtils.isEmpty(code)) {
                    throw new AlipayApiException("申请支付失败:" + alipayResponse.getSubMsg());
                }
                response.setQrCode(code);
                return response;
            }
        },
        WAP {
            @Override
            AlipayRequest createRequest(PaymentRequest request, String notifyUrl) {
                AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
                AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
                model.setOutTradeNo(request.getPaymentId());
                model.setSubject(request.getProductName());
                model.setTotalAmount(Money.cent(request.getAmount()).getAmount().toString());
                model.setBody(request.getProductName());
                model.setProductCode("QUICK_WAP_PAY");
                model.setTimeoutExpress("10m");
                alipayRequest.setBizModel(model);
                alipayRequest.setNotifyUrl(notifyUrl);
                alipayRequest.setReturnUrl(request.getReturnUrl());
                return alipayRequest;
            }
        },
        WEB {
            @Override
            AlipayRequest createRequest(PaymentRequest request, String notifyUrl) {
                AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
                AlipayTradePagePayModel model = new AlipayTradePagePayModel();
                model.setOutTradeNo(request.getPaymentId());
                model.setSubject(request.getProductName());
                model.setTotalAmount(Money.cent(request.getAmount()).getAmount().toString());
                model.setBody(request.getProductName());
                model.setProductCode("FAST_INSTANT_TRADE_PAY");
                model.setTimeoutExpress("10m");
                alipayRequest.setBizModel(model);
                alipayRequest.setNotifyUrl(notifyUrl);
                alipayRequest.setReturnUrl(request.getReturnUrl());
                return alipayRequest;
            }
        };

        static RequestType of(String type) {
            try {
                return RequestType.valueOf(type.toUpperCase());
            } catch (Exception e) {
                throw ErrorCode.ILLEGAL_PARAMETERS.createException("参数[extraParam.type]错误");
            }
        }

        PaymentResponse execute(AlipayClient client, AlipayRequest aliRequest) throws AlipayApiException {
            com.alipay.api.AlipayResponse alipayResponse = client.pageExecute(aliRequest, "GET");
            AlipayWebsiteResponse response = new AlipayWebsiteResponse();
            response.setPayUrl(alipayResponse.getBody());
            return response;
        }

        abstract AlipayRequest createRequest(PaymentRequest request, String notifyUrl);
    }
