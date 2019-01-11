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
@Dict(id = "bank-code")
public enum BankCode implements EnumDict<String> {
    ICBC("102", "工商银行"),

    ABC("103", "农业银行"),

    BOC("104", "中国银行"),

    CCB("105", "建设银行"),

    COMM("301", "交通银行"),

    CMB("308", "招商银行"),

    CITIC("302", "中信银行"),

    CEB("303", "光大银行"),

    CIB("309", "兴业银行"),

    CMBC("305", "民生银行"),

    HXB("304", "华夏银行"),

    SPDB("310", "浦发银行"),

    PSBC("100", "邮政储蓄银行"),

    PINGANBK("307", "平安银行"),

    BKSH("401", "上海银行"),

    CGB("306", "广发银行"),

    NBCB("408", "宁波银行"),
    BOBJ("403", "北京银行"),
    HZCB("423", "杭州银行"),
    TJCB("434", "天津银行"),
    CZB("316", "浙商银行"),
    GZRCB("1405", "广州农村商业银行"),
    BOGZ("1569", "贵州银行"),
    HSB("440", "徽商银行"),
    QLB("409", "齐鲁银行"),
    QDB("450", "青岛银行"),
    YTCB("404", "烟台银行"),

    NJCB("424", "南京银行"),
    JXCB("448", "江西银行"),
    LYCB("418", "洛阳银行"),
    GLCB("491", "桂林银行"),
    HCRCB("1562", "珲春农商行"),
    YHRCB("1565", "颖淮农商行"),
    YBRCB("1580", "延边农商行"),
    ;

    private String id;

    private String text;

    @Override
    public String getValue() {
        return name();
    }
}
