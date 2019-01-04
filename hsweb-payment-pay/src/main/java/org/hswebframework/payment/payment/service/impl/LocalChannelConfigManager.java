package org.hswebframework.payment.payment.service.impl;

import org.hswebframework.payment.api.payment.ChannelConfig;
import org.hswebframework.payment.api.payment.ChannelConfigManager;
import org.hswebframework.payment.payment.service.LocalChannelConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
public class LocalChannelConfigManager implements ChannelConfigManager {

    @Autowired
    private LocalChannelConfigService configService;

    @Override
    public <T extends ChannelConfig> Optional<T> getChannelConfigById(String channelConfigId, Class<T> tClass) {
        if (StringUtils.isEmpty(channelConfigId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(configService
                .selectByPk(channelConfigId)
                .toChannelConfig(tClass));
    }
}
