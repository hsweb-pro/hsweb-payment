package org.hswebframework.payment.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

@Getter
@AllArgsConstructor
@Dict(id = "freeze-direction-type")
public enum FreezeDirection implements EnumDict<String> {


    FREEZE_DIRECTION("FREEZE", "冻结"),

    UNFREEZE_DIRECTION("UNFREEZE", "解冻");

    private String value;

    private String text;
}
