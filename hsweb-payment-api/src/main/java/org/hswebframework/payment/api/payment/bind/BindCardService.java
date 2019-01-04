package org.hswebframework.payment.api.payment.bind;

/**
 * 绑卡服务
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface BindCardService {
    BindCardResponse requestBindCard(BindCardRequest request);

    BindCardConfirmResponse confirmBindCard(BindCardConfirmRequest request);
}
