package org.hswebframework.payment.authorize;

import com.alibaba.fastjson.JSON;
import org.hswebframework.payment.api.merchant.AgentMerchant;
import org.hswebframework.payment.api.merchant.AgentMerchantService;
import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.merchant.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.expands.request.SimpleRequestBuilder;
import org.hswebframework.utils.file.FileUtils;
import org.hswebframework.web.WebUtil;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.listener.event.AuthorizationBeforeEvent;
import org.hswebframework.web.authorization.listener.event.AuthorizationDecodeEvent;
import org.hswebframework.web.authorization.listener.event.AuthorizationSuccessEvent;
import org.hswebframework.web.authorization.listener.event.AuthorizingHandleBeforeEvent;
import org.hswebframework.web.controller.file.FileController;
import org.hswebframework.web.validate.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class AuthorizeHandler {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private AgentMerchantService agentMerchantService;

    @Value("${captcha.api-key:cef607ca8bbcf4b14235a9b9f707a9db}")
    private String captchaApiKey = "cef607ca8bbcf4b14235a9b9f707a9db";

    private SimpleRequestBuilder requestBuilder = new SimpleRequestBuilder();

    private List<String> allowFile = Arrays.asList(
            "jpg", "jpeg", "gif", "bmp", "png"
            //, "txt", "zip"
    );

    @EventListener
    public void handleAllowAdminPermission(AuthorizingHandleBeforeEvent event) {
        if (event.getContext().getAuthentication().getUser().getUsername().equals("admin")) {
            event.setAllow(true);
        }
        if (event.getContext().getDefinition().getPermissions().contains("file")) {
            event.setAllow(true);
        }
        //过滤文件权限
        event.getContext()
                .getParamContext()
                .getParams()
                .values()
                .stream()
                .filter(MultipartFile.class::isInstance)
                .map(MultipartFile.class::cast)
                .forEach(file -> {
                    String suffix = FileUtils.getSuffix(file.getOriginalFilename());
                    if (!allowFile.contains(suffix.toLowerCase())) {
                        event.setAllow(false);
                        event.setMessage("仅支持" + allowFile + "格式的文件");
                    }
                });
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void handleLoginEvent(AuthorizationSuccessEvent event) {
        String userId = event.getAuthentication().getUser().getId();
        Merchant merchant = merchantService.getMerchantByUserId(userId);
        if (merchant != null) {
            event.getResult().put("userType", "merchant");
            event.getResult().put("merchant", merchant);

            event.getAuthentication()
                    .setAttribute("merchantId", merchant.getId());
            event.getAuthentication()
                    .setAttribute("userType", "merchant");
        } else {
            AgentMerchant agentMerchant = agentMerchantService.getAgentByUserId(userId);
            if (agentMerchant != null) {
                event.getResult().put("userType", "agent");
                event.getResult().put("agent", agentMerchant);
                event.getAuthentication()
                        .setAttribute("agentId", agentMerchant.getId());
                event.getAuthentication()
                        .setAttribute("userType", "agent");
            }
        }
    }

    @EventListener
    public void handleVerificationCode(AuthorizationDecodeEvent event) {
        try {
            String token = event.<String>getParameter("token").orElse(null);
            if (!StringUtils.isEmpty(token)) {
                try {
                    String json = requestBuilder.https("https://captcha.luosimao.com/api/site_verify")
                            .param("api_key", captchaApiKey)
                            .param("response", token)
                            .resultAsJsonString()
                            .post().asString();
                    if (!"success".equals(JSON.parseObject(json).getString("res"))) {
                        log.error("人机验证失败:{}", json);
                        throw new ValidationException("人机验证失败");
                    }
                    return;
                } catch (IOException e) {
                    log.error("人机验证失败", e);
                }
            }
            throw new ValidationException("人机验证失败");
//            String verifyCodeInSession = (String) WebUtil.getHttpServletRequest().getSession().getAttribute("verifyCode");
//
//            String requestVerifyCode = event.getParameter("verifyCode")
//                    .map(Object::toString)
//                    .orElseThrow(() -> new ValidationException("验证码错误"));
//
//            if (!requestVerifyCode.equalsIgnoreCase(verifyCodeInSession)) {
//                throw new ValidationException("验证码错误");
//            }
        } finally {
            WebUtil.getHttpServletRequest().getSession().removeAttribute("verifyCode");
        }
    }
}
