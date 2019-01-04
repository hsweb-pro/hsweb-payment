package org.hswebframework.payment.payment.controller;

import org.hswebframework.payment.api.crypto.CipherManager;
import org.hswebframework.payment.api.crypto.Signature;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.payment.PaymentRequest;
import org.hswebframework.payment.payment.entity.PaymentOrderEntity;
import org.hswebframework.payment.payment.service.LocalPaymentOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Controller
public class PaymentReturnController {

    @Autowired
    private LocalPaymentOrderService orderService;

    @Autowired
    private CipherManager cipherManager;

    @RequestMapping("/payment/return/{orderId}")
    public RedirectView doReturn(@PathVariable String orderId, RedirectAttributes attributes) {
        PaymentOrderEntity entity = orderService.selectByPk(orderId);
        PaymentRequest request = entity.getOriginalRequest(PaymentRequest.class);
        RedirectView redirectView = new RedirectView(request.getReturnUrl());
        Map<String, String> map = new TreeMap<>();

        map.put("orderId", entity.getOrderId());
        map.put("amount", String.valueOf(entity.getAmount()));
        map.put("settleAmount", String.valueOf(entity.getRealAmount()));
        map.put("paymentId", entity.getId());
        map.put("status", entity.getStatus().getValue());
        map.put("statusText", entity.getStatus().getText());

        String sign = cipherManager
                .getSignature(Signature.Type.MD5, entity.getMerchantId())
                .sign(map);
        map.put("sign", sign);

        attributes.addAllAttributes(map);

        return redirectView;
    }
}
