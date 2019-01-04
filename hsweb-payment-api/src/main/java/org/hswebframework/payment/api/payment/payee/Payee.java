package org.hswebframework.payment.api.payment.payee;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.dict.EnumDict;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class Payee implements Serializable {
    @ApiModelProperty(value = "收款人账号")
    private String payee;

    @ApiModelProperty(value = "收款人姓名")
    private String payeeName;

    public Map<String, String> toMap() {
        return FastBeanCopier.copy(this, new TreeMap<>())
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> {
                            Object value = e.getValue();
                            if (value == null) {
                                return "";
                            }
                            if (value.getClass().isEnum()) {
                                return ((Enum) value).name();
                            }
                            if (value instanceof EnumDict) {
                                return String.valueOf(((EnumDict) e).getValue());
                            }
                            return String.valueOf(value);
                        }));

    }
}
