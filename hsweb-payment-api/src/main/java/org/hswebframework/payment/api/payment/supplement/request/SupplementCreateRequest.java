package org.hswebframework.payment.api.payment.supplement.request;

import org.hswebframework.payment.api.ApiRequest;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class SupplementCreateRequest extends ApiRequest {

    //补登源渠道
    private String sourceAccountNo;

    //补登目标渠道
    private String targetAccountNo;

    @NotBlank(message = "源账户名称不能为空")
    private String sourceAccountName;

    @NotBlank(message = "目标账户名称不能为空")
    private String targetAccountName;

    @NotBlank(message = "创建人ID不能为空")
    private String creatorId;

    @NotBlank(message = "创建人姓名不能为空")
    private String creatorName;

    private long sourceAmount;

    private long targetAmount;

    @NotBlank(message = "结算说明不能为空")
    private String remark;
}
