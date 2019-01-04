package org.hswebframework.payment.api.merchant.config;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class MerchantServiceConfig {

    @NotBlank(message = "服务id不能为空")
    private String serviceId;

    @NotBlank(message = "服务名称不能为空")
    private String serviceName;

    private Set<String> actions;

    //ip白名单
    private Set<String> ipWhiteList;

    public static MerchantServiceConfig of(String serviceId, String... actions) {
        MerchantServiceConfig config = new MerchantServiceConfig();
        config.setServiceId(serviceId);
        config.setActions(new HashSet<>(Arrays.asList(actions)));
        return config;
    }

    public static MerchantServiceConfig ofExpress(String express) {

        String[] idAndAction = express.split("[:]");
        if (idAndAction.length == 1) {
            return of(idAndAction[0]);
        }
        return of(idAndAction[0], idAndAction[1].split("[,]"));
    }
}
