package org.hswebframework.payment.merchant.controller;

import org.hswebframework.payment.api.account.AccountTransLog;
import org.hswebframework.payment.api.account.AccountTransService;
import org.hswebframework.payment.api.account.reqeust.QueryMerchantTransLogRequest;
import org.hswebframework.payment.api.account.response.AccountTransLogResponse;
import org.hswebframework.payment.api.enums.TransRateType;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.annotation.CurrentMerchant;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Lind
 * @since 1.0
 */
@RestController
@Authorize(permission = "payment")
@RequestMapping("/merchant/order")
public class MerchantOrderController {

}
