package org.hswebframework.payment.demo.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.payment.demo.PaymentService;
import org.hswebframework.payment.demo.utils.IDGenerator;
import org.hswebframework.payment.demo.utils.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @since 1.0.0
 */
@RestController
@Slf4j
@RequestMapping("/payment")
public class PaymentController {


    @Autowired
    private PaymentService paymentService;


    @PostMapping(value = "/notify", produces = MediaType.TEXT_PLAIN_VALUE)
    public String notify(@RequestParam Map<String, Object> objectMap) {
        System.out.println("验签["+objectMap.get("sign")+"]通知结果:" + SignUtils.verifySign(objectMap, paymentService.getSecretKey()));
        log.debug("接受到支付结果通知:\n{}", JSON.toJSONString(objectMap, SerializerFeature.PrettyFormat));
        return "success";
    }

    @PostMapping(value = "/query", produces = MediaType.TEXT_PLAIN_VALUE)
    public JSONObject notify(@RequestParam(required = false) String paymentId,
                             @RequestParam(required = false) String orderId) {

        return paymentService.queryOrder(orderId, paymentId);
    }


    @PostMapping("/request")
    public JSONObject requestPay(@RequestParam String productName,
                                 @RequestParam String channel,
                                 @RequestParam long amount,
                                 @RequestParam(defaultValue = "{}") String extraParam) {

        return paymentService.requestPay(
                channel,
                IDGenerator.generateId(),
                IDGenerator.generateId(),
                productName,
                amount,
                extraParam);
    }

    @PostMapping("/request-quick")
    public JSONObject requestPay(@RequestParam String accountName,
                                 @RequestParam String accountNumber,
                                 @RequestParam String phoneNumber,
                                 @RequestParam String idNumber,
                                 @RequestParam String productName,
                                 @RequestParam long amount,
                                 @RequestParam(defaultValue = "{}") String extraParam) {
        return paymentService.requestQuickPay(
                accountName,
                accountNumber,
                phoneNumber,
                idNumber,
                IDGenerator.generateId(),
                IDGenerator.generateId(),
                productName,
                amount,
                extraParam);
    }


    @PostMapping("/confirm-quick")
    public JSONObject confirm(@RequestParam String paymentId,
                              @RequestParam String smsCode) {
        return paymentService.confirmQuickPay(paymentId, smsCode);
    }

    @PostMapping("/request-substitute")
    public JSONObject requestSubstitute(
            @RequestParam String payeeType,
            @RequestParam String details,
            @RequestParam String remark) {

        return paymentService.requestSubstitute(
                payeeType, details, remark
        );
    }
}
