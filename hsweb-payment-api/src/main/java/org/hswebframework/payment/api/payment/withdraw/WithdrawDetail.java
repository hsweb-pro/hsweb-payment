package org.hswebframework.payment.api.payment.withdraw;

import lombok.*;
import org.hswebframework.web.bean.FastBeanCopier;

import java.io.Serializable;

/**
 * @author Lind
 * @since 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawDetail implements Serializable {

    private String accountName;

    private String accountNumber;

    private String accountType;

    private long amount;

    private String bankId;

    private String branchName;

    private String city;

    private String merchDetailNo;

    private String idNumber;

    private String province;

}
