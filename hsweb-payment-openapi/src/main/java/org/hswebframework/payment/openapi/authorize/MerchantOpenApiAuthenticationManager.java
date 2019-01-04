package org.hswebframework.payment.openapi.authorize;

import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.MerchantConfigKey;
import org.hswebframework.payment.api.enums.MerchantStatus;
import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.merchant.MerchantService;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.merchant.config.MerchantServiceConfig;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.basic.web.AuthorizedToken;
import org.hswebframework.web.authorization.basic.web.ParsedToken;
import org.hswebframework.web.authorization.basic.web.UserTokenParser;
import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import org.hswebframework.web.authorization.simple.SimplePermission;
import org.hswebframework.web.authorization.simple.SimpleUser;
import org.hswebframework.web.authorization.token.ThirdPartAuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0.0
 */
//@Component
public class MerchantOpenApiAuthenticationManager implements ThirdPartAuthenticationManager, UserTokenParser {
    @Override
    public String getTokenType() {
        return "merchant-open-api-token";
    }

    @Autowired
    private MerchantConfigManager merchantConfigManager;

    @Autowired
    private MerchantService merchantService;

    @Override
    public Authentication getByUserId(String userId) {
        Merchant merchant = merchantService.getMerchantById(userId);
        if (merchant == null) {
            throw ErrorCode.MERCHANT_NOT_EXISTS.createException();
        }
        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            throw ErrorCode.MERCHANT_FREEZE.createException();
        }
        List<MerchantServiceConfig> configs = merchantConfigManager
                .<MerchantServiceConfig>getConfigList(userId, MerchantConfigKey.SUPPORTED_SERVICE)
                .orElseGet(Collections::emptyList);

        SimpleAuthentication authentication = new SimpleAuthentication();
        authentication.setUser(SimpleUser
                .builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .type("merchant-open-api")
                .build());

        authentication.setPermissions(configs.stream()
                .map(autz -> SimplePermission.builder()
                        .actions(autz.getActions())
                        .id(autz.getServiceId())
                        .build()).collect(Collectors.toList()));
        return authentication;
    }

    protected boolean isOpenApiRequest(javax.servlet.http.HttpServletRequest request) {
        Object isOpenApiRequest = request.getAttribute("_IS_OPEN_API_REQUEST");

        return (Boolean.TRUE.equals(isOpenApiRequest));
    }

    @Override
    public ParsedToken parseToken(javax.servlet.http.HttpServletRequest request) {
        if (isOpenApiRequest(request)) {
            String merchantId = request.getParameter("merchantId");
            return new AuthorizedToken() {
                @Override
                public String getUserId() {
                    return merchantId;
                }

                @Override
                public String getToken() {
                    return merchantId;
                }

                @Override
                public String getType() {
                    return getTokenType();
                }
            };
        }
        return null;
    }

}
