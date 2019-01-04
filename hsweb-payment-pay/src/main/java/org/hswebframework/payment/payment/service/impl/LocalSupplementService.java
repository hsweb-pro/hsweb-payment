package org.hswebframework.payment.payment.service.impl;

import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.TransType;
import org.hswebframework.payment.api.payment.supplement.Supplement;
import org.hswebframework.payment.api.payment.supplement.SupplementService;
import org.hswebframework.payment.api.payment.supplement.request.SupplementCloseRequest;
import org.hswebframework.payment.api.payment.supplement.request.SupplementCompleteRequest;
import org.hswebframework.payment.api.payment.supplement.request.SupplementCreateRequest;
import org.hswebframework.payment.api.payment.supplement.request.SupplementRollbackRequest;
import org.hswebframework.payment.api.payment.supplement.response.SupplementCloseResponse;
import org.hswebframework.payment.api.payment.supplement.response.SupplementCompleteResponse;
import org.hswebframework.payment.api.payment.supplement.response.SupplementCreateResponse;
import org.hswebframework.payment.api.payment.supplement.response.SupplementRollbackResponse;
import org.hswebframework.payment.api.settle.channel.ChannelDepositRequest;
import org.hswebframework.payment.api.settle.channel.ChannelSettleService;
import org.hswebframework.payment.api.settle.channel.ChannelWithdrawRequest;
import org.hswebframework.payment.payment.dao.SupplementDao;
import org.hswebframework.payment.payment.entity.SupplementEntity;
import org.hswebframework.payment.api.enums.SupplementStatus;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.commons.bean.BeanValidator;
import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Optional;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
public class LocalSupplementService extends GenericEntityService<SupplementEntity, String> implements SupplementService {

    @Autowired
    private SupplementDao supplementDao;

    @Autowired
    private ChannelSettleService settleService;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public SupplementDao getDao() {
        return supplementDao;
    }


    @Override
    public SupplementCreateResponse create(SupplementCreateRequest request) {
        BeanValidator.tryValidate(request);
        SupplementEntity entity = new SupplementEntity();
        entity.setId(getIDGenerator().generate());
        entity.setStatus(SupplementStatus.REQUEST);
        entity.setCreateTime(new Date());
        entity.setCreatorId(request.getCreatorId());
        entity.setCreatorName(request.getCreatorName());
        entity.setSourceAmount(request.getSourceAmount());
        entity.setTargetAmount(request.getTargetAmount());
        entity.setSourceAccountNo(request.getSourceAccountNo());
        entity.setTargetAccountNo(request.getTargetAccountNo());
        entity.setSourceAccountName(request.getSourceAccountName());
        entity.setTargetAccountName(request.getTargetAccountName());
        entity.setRemark(request.getRemark());
        getDao().insert(entity);
        return SupplementCreateResponse.of(convert(entity));
    }

    private Supplement convert(SupplementEntity supplementEntity) {
        return Optional.of(supplementEntity)
                .map(e -> e.copyTo(new Supplement()))
                .orElse(null);
    }

    @Override
    public SupplementCompleteResponse complete(SupplementCompleteRequest request) {

        BeanValidator.tryValidate(request);

        SupplementEntity supplement = selectByPk(request.getSupplementId());
        if (supplement == null) {
            throw new NotFoundException("补登记录不存在");
        }
        if (supplement.getStatus() != SupplementStatus.REQUEST) {
            throw ErrorCode.SERVICE_ERROR.createException("无法完成状态为:" + supplement.getStatus().getText() + "的补登信息");
        }
        if (StringUtils.hasText(supplement.getSourceAccountNo())) {
            //下账
            settleService.withdraw(ChannelWithdrawRequest.builder()
                    .amount(supplement.getSourceAmount())
                    .accountNo(supplement.getSourceAccountNo())
                    .memo(supplement.getRemark())
                    .channelName("结算资金补登")
                    .channelId("supplement")
                    .channelProvider("system")
                    .channel("supplement")
                    .channelProviderName("结算资金补登")
                    .merchantId(supplement.getSourceAccountNo())
                    .paymentId(supplement.getId())
                    .merchantName(supplement.getSourceAccountName())
                    .transType(TransType.SUPPLEMENT)
                    .build())
                    .assertSuccess();
        }
        if (StringUtils.hasText(supplement.getTargetAccountNo())) {
            //账户上帐
            settleService.deposit(ChannelDepositRequest.builder()
                    .amount(supplement.getTargetAmount())
                    .accountNo(supplement.getTargetAccountNo())
                    .channelName("结算资金补登")
                    .channelId("supplement")
                    .channelProvider("system")
                    .channel("supplement")
                    .channelProviderName("结算资金补登")
                    .memo(supplement.getRemark())
                    .merchantId(supplement.getTargetAccountNo())
                    .paymentId(supplement.getId())
                    .merchantName(supplement.getTargetAccountName())
                    .transType(TransType.SUPPLEMENT)
                    .build())
                    .assertSuccess();
        }
        supplement.setStatus(SupplementStatus.SUCCESS);
        supplement.setSupplementTime(new Date());
        createUpdate()
                .set(supplement::getStatus)
                .set(supplement::getSupplementTime)
                .where(supplement::getId)
                .exec();
        return SupplementCompleteResponse.success();
    }

    @Override
    public SupplementEntity selectByPk(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        return createQuery()
                .where(GenericEntity.id, id)
                .forUpdate()
                .single();
    }

    @Override
    public SupplementRollbackResponse rollback(SupplementRollbackRequest request) {
        BeanValidator.tryValidate(request);

        SupplementEntity supplement = selectByPk(request.getSupplementId());
        if (supplement == null) {
            throw new NotFoundException("补登记录不存在");
        }
        if (supplement.getStatus() != SupplementStatus.SUCCESS) {
            throw ErrorCode.SERVICE_ERROR.createException("无法回退状态为:" + supplement.getStatus().getText() + "的补登信息");
        }
        if (StringUtils.hasText(supplement.getTargetAccountNo())) {
            //账户下帐
            settleService.withdraw(ChannelWithdrawRequest.builder()
                    .amount(supplement.getTargetAmount())
                    .accountNo(supplement.getTargetAccountNo())
                    .memo(supplement.getRemark())
                    .merchantId(supplement.getTargetAccountNo())
                    .paymentId(supplement.getId())
                    .channelName("结算资金补登")
                    .channelId("supplement")
                    .channelProvider("system")
                    .channel("supplement")
                    .channelProviderName("结算资金补登回退")
                    .merchantName(supplement.getTargetAccountName())
                    .transType(TransType.SUPPLEMENT_ROLLBACK)
                    .build())
                    .assertSuccess();
        }
        if (StringUtils.hasText(supplement.getSourceAccountNo())) {
            //上账
            settleService.deposit(ChannelDepositRequest.builder()
                    .amount(supplement.getSourceAmount())
                    .accountNo(supplement.getSourceAccountNo())
                    .memo(supplement.getRemark())
                    .channelName("结算资金补登")
                    .channelId("supplement")
                    .channelProvider("system")
                    .channel("supplement")
                    .channelProviderName("结算资金补登回退")
                    .merchantId(supplement.getSourceAccountNo())
                    .paymentId(supplement.getId())
                    .merchantName(supplement.getSourceAccountName())
                    .transType(TransType.SUPPLEMENT_ROLLBACK)
                    .build())
                    .assertSuccess();
        }
        supplement.setStatus(SupplementStatus.ROLLBACK);
        supplement.setRemark(request.getRemark());
        createUpdate()
                .set(supplement::getStatus)
                .set(supplement::getRemark)
                .where(supplement::getId)
                .exec();
        return SupplementRollbackResponse.success();
    }

    @Override
    public SupplementCloseResponse close(SupplementCloseRequest request) {

        BeanValidator.tryValidate(request);

        SupplementEntity supplement = selectByPk(request.getSupplementId());
        if (supplement == null) {
            throw new NotFoundException("补登记录不存在");
        }
        if (supplement.getStatus() != SupplementStatus.REQUEST) {
            throw ErrorCode.SERVICE_ERROR.createException("无法关闭状态为:" + supplement.getStatus().getText() + "的补登信息");
        }
        supplement.setRemark(request.getRemark());
        supplement.setStatus(SupplementStatus.FAILED);
        supplement.setSupplementTime(new Date());

        createUpdate()
                .set(supplement::getRemark)
                .set(supplement::getStatus)
                .where(supplement::getId)
                .exec();

        return SupplementCloseResponse.success();
    }
}
