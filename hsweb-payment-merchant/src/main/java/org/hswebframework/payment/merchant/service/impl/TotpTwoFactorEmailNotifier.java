package org.hswebframework.payment.merchant.service.impl;

import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.merchant.AgentMerchant;
import org.hswebframework.payment.api.merchant.AgentMerchantService;
import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.merchant.MerchantService;
import org.hswebframework.payment.api.message.MessageSender;
import org.hswebframework.payment.api.utils.ImageUtils;
import org.hswebframework.payment.merchant.service.MerchantProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.authorization.setting.UserSettingManager;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.service.authorization.UserService;
import org.hswebframework.web.service.authorization.events.TotpTwoFactorCreatedEvent;
import org.hswebframework.web.service.authorization.simple.totp.TotpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import javax.mail.internet.MimeMessage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * 双重验证邮件通知
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Component
@Slf4j
public class TotpTwoFactorEmailNotifier {

    @Autowired
    private MerchantProperties merchantProperties;

    @Autowired
    private MessageSender messageSender;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private AgentMerchantService agentMerchantService;

    @Autowired
    public UserSettingManager userSettingManager;

    @Autowired
    private UserService userService;


    public void sendTotpEmail(String userId) {
        UserEntity userEntity = userService.selectByPk(userId);
        userSettingManager.getSetting(userId, "tow-factor-totp-key")
                .asString()
                .ifPresent(key -> {
                            String url = TotpUtil.generateTotpString(userEntity.getUsername(), merchantProperties.getTwoFactorDomain(), key);
                            try {
                                doSend(userId, url);
                            } catch (Exception e) {
                                throw new BusinessException("发送邮件失败,请将[" + url + "]生成二维码后发送给用户", e);
                            }
                        }
                );
    }

    @SneakyThrows
    private void doSend(String userId, String url) {
        String name;
        String email;
        //商户
        Merchant merchant = merchantService.getMerchantByUserId(userId);
        if (merchant != null) {
            name = merchant.getName();
            email = merchant.getEmail();
        } else {
            //代理
            AgentMerchant agent = agentMerchantService.getAgentByUserId(userId);
            if (agent != null) {
                name = agent.getName();
                email = agent.getEmail();
            } else {
                return;
            }
        }
        if (StringUtils.hasText(email)) {
            JavaMailSender sender = messageSender.email()
                    .get(merchantProperties.getTwoFactorEmailSender(), true);

            MimeMessage mimeMessage = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            BufferedImage image = ImageUtils.createQrCode(300, 300, url);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            helper.setFrom(merchantProperties.getTwoFactorEmailFrom());
            helper.setTo(email);
            helper.setSubject("欢迎使用XManpay," + name + "已开通双重验证");
            helper.setText(String.format("欢迎加入XManpay为了您的资金交易安全." +
                            "<br/>关键操作需要使用动态口令,你可以使用[Google Authenticator]" +
                            "APP来获取动态口令." +
                            "[<a href='https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2'>" +
                            "安卓下载</a>," +
                            "<a href='http://itunes.apple.com/us/app/google-authenticator/id388497605?mt=8'>" +
                            "IOS下载</a>]" +
                            "<br/>" +
                            "成功安装APP后.使用APP扫描:<br>" + "<img src=\"cid:qrcode\"/>.<br/>" +
                            "如果二维码没有显示，点击邮件上方显示图片按钮，进行操作.",
                    name), true);
            helper.addInline("qrcode", new ByteArrayResource(outputStream.toByteArray()), MediaType.IMAGE_PNG_VALUE);
            sender.send(mimeMessage);
        } else {
            throw ErrorCode.MERCHANT_CONFIG_ERROR.createException("商户未设置邮箱");
        }
    }

    @TransactionalEventListener
    @Async
    public void handleTwoFactorEvent(TotpTwoFactorCreatedEvent event) {
        if (merchantProperties.isTwoFactorEmailNotify()) {
            return;
        }
        try {
            doSend(event.getUserEntity().getId(), event.getTotpUrl());
        } catch (Exception e) {
            log.error("商户[{}]TOTP 2FA创建成功,但是未能发送邮件通知. TOTP url:[{}]",
                    event.getUserEntity().getName(),
                    event.getTotpUrl(), e);
        }
    }
}

