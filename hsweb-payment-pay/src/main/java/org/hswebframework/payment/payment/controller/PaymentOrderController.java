package org.hswebframework.payment.payment.controller;

import org.hswebframework.payment.api.account.AccountTransService;
import org.hswebframework.payment.payment.entity.PaymentOrderEntity;
import org.hswebframework.payment.payment.service.LocalPaymentOrderService;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@Authorize(permission = "payment")
@RequestMapping("/payment/order")
public class PaymentOrderController implements QueryController<PaymentOrderEntity, String, QueryParamEntity> {

    @Autowired
    private LocalPaymentOrderService paymentService;

    @Override
    public LocalPaymentOrderService getService() {
        return paymentService;
    }


    @Override
    public ResponseMessage<PagerResult<PaymentOrderEntity>> list(QueryParamEntity param) {
        return QueryController.super.list(param).exclude(PaymentOrderEntity.class, "requestJson", "responseJson");
    }
}
