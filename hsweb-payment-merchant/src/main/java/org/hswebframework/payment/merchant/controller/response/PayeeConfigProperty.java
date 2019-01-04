package org.hswebframework.payment.merchant.controller.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class PayeeConfigProperty {
    private String property;

    private String name;

    private boolean required;

    private String type;

    public void initFromSwaggerAnnotation(Field field, ApiModelProperty annotation) {
        setProperty(field.getName());

        if (StringUtils.isEmpty(annotation.value())) {
            setName(field.getName());
        } else {
            setName(annotation.value());
        }
        if (StringUtils.isEmpty(annotation.dataType())) {
            setType(field.getType().getSimpleName());
        } else {
            setType(annotation.dataType());
        }
        setRequired(annotation.required());
    }
}
