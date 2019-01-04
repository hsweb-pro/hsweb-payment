package org.hswebframework.payment.api.enums;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.collect.Maps;
import org.hswebframework.payment.api.merchant.config.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.payment.api.crypto.Signature;
import org.hswebframework.payment.api.merchant.config.*;
import org.hswebframework.web.commons.bean.BeanValidator;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

/**
 * 商户配置属性枚举
 *
 * @author zhouhao
 * @see MerchantConfigManager
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
@Dict(id = "merchant-config-key")
@JSONType(deserializer = EnumDict.EnumDictJSONDeserializer.class)
public enum MerchantConfigKey implements EnumDict<String> {

    /**
     * @see Signature
     */
    SECRET_KEY("商户密钥", String.class, true, false),

    SETTLE_CONFIG("结算配置", MerchantSettleConfig.class, false, true),

    SUPPORTED_SERVICE("已开通服务", MerchantServiceConfig.class, true, false),

    SUPPORTED_CHANNEL("已开通渠道", MerchantChannelConfig.class, true, false),

    RATE_CONFIG("费率配置", MerchantRateConfig.class, true, false),

    PAYEE_CONFIG("收款人配置", MerchantPayeeConfig.class, true, true);

    private String text;

    private Class<?> type;

    /**
     * 是否必填
     */
    private boolean required;

    /**
     * 商户可编辑
     */
    private boolean merchantWritable;

    public void tryValidate(String config) {
        if (getType() == String.class || config == null) {
            return;
        }
        if (config.startsWith("[")) {
            JSON.parseArray(config, getType()).forEach(this::doValidate);
        } else {
            this.doValidate(JSON.parseObject(config));
        }
    }

    private void doValidate(Object conf) {
        BeanValidator.tryValidate(conf);
    }

    @Override
    public String getValue() {
        return name();
    }

    @Override
    public Object getWriteJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", getValue());
        jsonObject.put("text", getText());
        // TODO: 18-11-15 解析类型
        jsonObject.put("meta", Maps.newHashMap());
        jsonObject.put("required", isRequired());
        jsonObject.put("merchantWritable", isMerchantWritable());
        return jsonObject;
    }
}
