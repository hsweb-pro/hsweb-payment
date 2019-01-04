package org.hswebframework.payment.account.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.EnumDict;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum  TradeLogType implements EnumDict<String> {

    DEDUCT("DEDUCT","单笔代收"),

    BATCH_DEDUCT("BATCH_DEDUCT","批量代收"),

    QUICK_PAY("QUICK_PAY","快捷支付"),

    BANK_PAY( "BANK_PAY","网关支付"),

    BATCH_PREPARE("BATCH_PREPARE","批量代付"),

    SCAN_PAY("SCAN_PAY","扫码支付"),

    DEPOSIT("DEPOSIT","充值"),

    CHARGE("CHARGE","收费"),

    SETTLE("SETTLE","结算");


    private String value;

    private String text;

}
