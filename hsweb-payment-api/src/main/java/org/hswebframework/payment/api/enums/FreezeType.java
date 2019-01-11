package org.hswebframework.payment.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

@Getter
@AllArgsConstructor
@Dict(id = "account-freeze-type")
public enum FreezeType implements EnumDict<String> {


    /***系統使用冻结码，业务冻结请勿使用***/
    FUND_WITHDRAW_FREEZE("FUND_WITHDRAW_FREEZE", "资金提现冻结/解冻"),
    FUND_TRANSFER_FREEZE("FUND_TRANSFER_FREEZE", "资金转账冻结/解冻"),
    FUND_DEPOSIT_BACK_FREEZE("FUND_DEPOSIT_BACK_FREEZE", "资金充退冻结/解冻"),
    TRADE_PAYMENT_FREEZE("TRADE_PAYMENT_FREEZE", "交易付款冻结"),
    /***系統使用冻结码，业务冻结请勿使用***/

    DEDUCT_PAY_FREEZE("DEDUCT_PAY_FREEZE", "代扣交易冻结"),
    WITHDRAW_FREEZE("WITHDRAW_FREEZE", "提现冻结"),
    SPECIFY_FREEZE("SPECIFY_FREEZE", "指定冻结"),
    COMMON_FREEZE("COMMON_FREEZE", "通用冻结");

    private String value;

    private String text;
}
