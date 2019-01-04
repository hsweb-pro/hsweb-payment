package org.hswebframework.payment.merchant.controller;

import org.hswebframework.payment.api.merchant.AgentMerchant;
import org.hswebframework.payment.api.merchant.AgentMerchantService;
import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.merchant.MerchantService;
import org.hswebframework.payment.api.annotation.CurrentMerchant;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;
import java.util.function.Function;

@Component
public class MerchantArgsResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private AgentMerchantService agentMerchantService;

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(CurrentMerchant.class) != null;
    }

    Function<Authentication, String> merchantIdFunction = autz ->
            autz.<String>getAttribute("merchantId")
                    .orElseGet(() -> {
                        Merchant merchant = merchantService.getMerchantByUserId(autz.getUser().getId());
                        if (merchant == null) {
                            return null;
                        }
                        return merchant.getId();
                    });

    Function<Authentication, Merchant> merchantFunction = autz ->
            autz.<String>getAttribute("merchantId")
                    .map(merchantService::getMerchantById)
                    .orElseGet(() -> {
                        return merchantService.getMerchantByUserId(autz.getUser().getId());
                    });


    Function<Authentication, String> agentIdFunction = autz ->
            autz.<String>getAttribute("agentId")
                    .orElseGet(() -> {
                        AgentMerchant agentMerchant = agentMerchantService.getAgentById(autz.getUser().getId());
                        if (agentMerchant == null) {
                            return null;
                        }
                        return agentMerchant.getId();
                    });

    Function<Authentication, AgentMerchant> agentFunction = autz ->
            autz.<String>getAttribute("agentId")
                    .map(agentMerchantService::getAgentById)
                    .orElseGet(() -> {
                        return agentMerchantService.getAgentByUserId(autz.getUser().getId());
                    });


    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        CurrentMerchant merchant = methodParameter.getParameterAnnotation(CurrentMerchant.class);
        Function<Authentication, ?> function = null;
        if (methodParameter.getParameterType() == String.class) {
            if (merchant.agentOrMerchant()) {
                Authentication authentication = Authentication.current().orElseThrow(UnAuthorizedException::new);
                return Optional.ofNullable(merchantIdFunction.apply(authentication))
                        .orElseGet(() -> Optional.ofNullable(agentIdFunction.apply(authentication))
                                .orElseThrow(UnAuthorizedException::new));
            }
            function = merchant.agent() ? agentIdFunction : merchantIdFunction;
        } else if (methodParameter.getParameterType() == Merchant.class) {
            function = merchantFunction;
        } else if (methodParameter.getParameterType() == AgentMerchant.class) {
            function = agentFunction;
        }
        if (function != null) {
            return Authentication.current()
                    .map(function)
                    .orElseThrow(UnAuthorizedException::new);
        }
        throw new UnsupportedOperationException("不支持的参数类型:" + methodParameter.getParameterType());
    }
}
