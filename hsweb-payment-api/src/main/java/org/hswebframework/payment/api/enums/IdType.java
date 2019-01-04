package org.hswebframework.payment.api.enums;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
@JSONType(deserializer = EnumDict.EnumDictJSONDeserializer.class)
@Dict(id = "id-type")
public enum IdType implements EnumDict<String> {

    /**
     * 身份证
     */
    ID("0", "身份证"),
    /**
     * 军官证
     */
    ARMYID("3", "军官证"),
    /**
     * 护照
     */
    PASSPORT("2", "护照"),
    /**
     * 香港居民往来内地通行证
     */
    HK_HRP("5", "香港居民往来内地通行证"),
    /**
     * 澳门居民往来内地通行证
     */
    AOMEN_HRP("5", "澳门居民往来内地通行证"),
    /**
     * 台湾居民来往大陆通行证
     */
    TW_HRPT("6", "台湾居民来往大陆通行证"),
    /**
     * 武装警察身份证件
     */
    OC("9", "武装警察身份证件"),
    /**
     * 军人身份证件
     */
    SC("4", "军人身份证件"),
    /**
     * 户口簿
     */
    RB("1", "户口簿"),
    FOREIGN_RP("8", "外国人居留证"),
    /**
     * 其它证件
     */
    OTHER("X", "其它证件");
    private String code;

    private String text;


    @Override
    public String getValue() {
        return name();
    }
}
