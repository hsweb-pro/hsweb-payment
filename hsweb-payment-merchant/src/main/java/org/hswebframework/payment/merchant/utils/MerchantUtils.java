package org.hswebframework.payment.merchant.utils;

import org.hswebframework.payment.api.enums.MerchantConfigKey;
import org.hswebframework.payment.api.enums.TransRateType;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.merchant.config.MerchantChannelConfig;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.merchant.config.MerchantRateConfig;
import org.hswebframework.payment.merchant.entity.MerchantEntity;
import org.hswebframework.web.authorization.Authentication;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MerchantUtils {


    public static Optional<String> getMerchantIdByAuthentication(Authentication authentication,
                                                                 Function<String, MerchantEntity> getMerchantByUserId){


        return Optional.ofNullable( authentication.<String>getAttribute("merchantId")
                .orElseGet(()-> Optional
                        .ofNullable(getMerchantByUserId.apply(authentication.getUser().getId()))
                        .map(MerchantEntity::getId)
                        .orElse(null)));
    }



    /**
     * 根据商户ID获取费率配置
     *
     * @param merchantId
     * @return
     */
    public static List<MerchantRateConfig> getRateConfigById(MerchantConfigManager configManager,String merchantId) {
        //查询已开通的渠道
        Map<String, MerchantChannelConfig> channelConfigMap = configManager
                .<MerchantChannelConfig>getConfigList(merchantId, MerchantConfigKey.SUPPORTED_CHANNEL)
                .map(list -> {
                    Map<String, MerchantChannelConfig> configMap = new HashMap<>();
                    for (MerchantChannelConfig config : list) {
                        configMap.put(config.getChannel() + "-" + config.getTransType().getValue(), config);
                        configMap.put("null-" + config.getTransType().getValue(), config);
                    }
                    return configMap;
                })
                .orElse(Collections.emptyMap());

        //费率配置
        List<MerchantRateConfig> allConfigs = configManager
                .<MerchantRateConfig>getConfigList(merchantId, MerchantConfigKey.RATE_CONFIG)
                .map(list -> list.stream()
                        .filter(config -> channelConfigMap.containsKey(config.getChannel() + "-" + config.getTransType()))
                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);


//        List<MerchantRateConfigResponse> responses = allConfigs
//                .stream()
//                .map(e -> {
//                    MerchantRateConfigResponse response = new MerchantRateConfigResponse();
//                    TransRateType rateType = e.getRateType();
//                    TransType transType = e.getTransType();
//                    response.setChannel(e.getChannel());
//                    response.setChannelName(e.getChannelName());
//                    response.setMemo(e.getMemo());
//                    response.setRate(e.getRate());
//                    response.setRateType(rateType != null ? rateType.getText() : "");
//                    response.setTransType(transType != null ? transType.getText() : "");
//                    return response;
//                }).collect(Collectors.toList());
        return allConfigs;
    }


}
