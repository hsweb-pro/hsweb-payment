package org.hswebframework.payment.payment.service.impl;

import org.hswebframework.payment.api.enums.ErrorCode;
import org.hswebframework.payment.api.enums.FundDirection;
import org.hswebframework.payment.api.payment.ChannelConfig;
import org.hswebframework.payment.api.payment.ChannelConfigManager;
import org.hswebframework.payment.api.settle.channel.*;
import org.hswebframework.payment.payment.dao.ChannelSettleInfoDao;
import org.hswebframework.payment.payment.dao.ChannelSettleLogDao;
import org.hswebframework.payment.payment.entity.ChannelConfigEntity;
import org.hswebframework.payment.payment.entity.ChannelSettleInfoEntity;
import org.hswebframework.payment.payment.entity.ChannelSettleLogEntity;
import org.hswebframework.payment.payment.service.LocalChannelConfigService;
import org.hswebframework.web.commons.bean.BeanValidator;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.function.Consumer;

import static org.hswebframework.web.commons.entity.param.QueryParamEntity.*;
import static org.hswebframework.web.service.DefaultDSLQueryService.createQuery;

@Service
@Transactional(rollbackFor = Exception.class)
@SuppressWarnings("all")
public class DefaultSettleService implements ChannelSettleService {


    @Autowired
    private LocalChannelConfigService channelConfigService;

    @Autowired
    private ChannelConfigManager channelConfigManager;

    @Autowired
    private ChannelSettleInfoDao channelSettleInfoDao;

    @Autowired
    private ChannelSettleLogDao logDao;

    @Override
    public ChannelDepositResponse deposit(ChannelDepositRequest request) {
        BeanValidator.tryValidate(request);
        ChannelDepositResponse response = new ChannelDepositResponse();

        Consumer<ChannelSettleLogEntity> channelInfoApply;

        String accountNo = request.getAccountNo();

        if (!StringUtils.isEmpty(request.getAccountNo())) {
            BeanValidator.tryValidate(request, SettleValidateGroup.ForAccount.class);
            channelInfoApply = entity -> {
                entity.setChannelProviderName(request.getChannelProviderName());
                entity.setChannelProvider(request.getChannelProvider());
                entity.setChannelName(request.getChannelName());
            };
        } else {
            BeanValidator.tryValidate(request, SettleValidateGroup.ForChannel.class);
            ChannelConfigEntity channelConfig = channelConfigService.selectByPk(request.getChannelId());
            if (channelConfig == null) {
                response.setError(ErrorCode.CHANNEL_CONFIG_ERROR);
                response.setMessage("渠道配置不存在");
                return response;
            }
            accountNo = channelConfig.getAccountNo();
            channelInfoApply = entity -> {
                entity.setChannelProviderName(channelConfig.getChannelProviderName());
                entity.setChannelProvider(channelConfig.getChannelProvider());
                entity.setChannelName(channelConfig.getChannelName());
            };

        }
        ChannelSettleInfo info = getInfoByAccountNo(accountNo);
        if (info == null) {
            response.setError(ErrorCode.CHANNEL_CONFIG_ERROR);
            response.setMessage("渠道结算配置不存在");
            return response;
        }
        //上账
        channelSettleInfoDao.incrementAmount(accountNo, request.getAmount());
        //记录交易日志
        ChannelSettleLogEntity logEntity = new ChannelSettleLogEntity();
        logEntity.setId(IDGenerator.SNOW_FLAKE_STRING.generate());
        logEntity.setAccountNo(accountNo);
        logEntity.setMerchantId(request.getMerchantId());
        logEntity.setCreateTime(new Date());
        logEntity.setMemo(request.getMemo());
        logEntity.setChannel(request.getChannel());
        logEntity.setChannelId(request.getChannelId());
        logEntity.setMerchantName(request.getMerchantName());
        logEntity.setTransType(request.getTransType());
        logEntity.setBalance(info.getBalance() + request.getAmount());
        logEntity.setPaymentId(request.getPaymentId());
        logEntity.setMerchantId(request.getMerchantId());
        logEntity.setFundDirection(FundDirection.IN);
        logEntity.setAmount(request.getAmount());
        channelInfoApply.accept(logEntity);
        logDao.insert(logEntity);
        response.setSettleInfo(getInfoByAccountNo(accountNo));
        response.setSuccess(true);

        return response;
    }

    @Override
    public QueryMerchantSettleResponse queryMerchantSettle(QueryMerchantSettleRequest request) {
        QueryMerchantSettleResponse response = new QueryMerchantSettleResponse();
        response.setSuccess(true);
        //收入
        long inAmount = newQuery()
                .where(ChannelSettleLogEntity::getMerchantId, request.getMerchantId())
                .when(StringUtils.hasText(request.getChannelId()), query -> {
                    ChannelConfig config = channelConfigManager.getChannelConfigById(request.getChannelId(), ChannelConfig.class)
                            .orElseThrow(ErrorCode.CHANNEL_CONFIG_ERROR::createException);
                    query.and(ChannelSettleLogEntity::getAccountNo, config.getAccountNo());
                })
                .and(ChannelSettleLogEntity::getTransType, request.getTransType())
                .and(ChannelSettleLogEntity::getFundDirection, FundDirection.IN)
                .execute(logDao::sumAmount);

        //支出
        long outAmount = newQuery()
                .where(ChannelSettleLogEntity::getMerchantId, request.getMerchantId())
                .when(StringUtils.hasText(request.getChannelId()), query -> {
                    ChannelConfig config = channelConfigManager.getChannelConfigById(request.getChannelId(), ChannelConfig.class)
                            .orElseThrow(ErrorCode.CHANNEL_CONFIG_ERROR::createException);
                    query.and(ChannelSettleLogEntity::getAccountNo, config.getAccountNo());
                })
                .and(ChannelSettleLogEntity::getTransType, request.getTransType())
                .and(ChannelSettleLogEntity::getFundDirection, FundDirection.OUT)
                .execute(logDao::sumAmount);
        response.setInAmount(inAmount);
        response.setOutAmount(outAmount);
        return response;
    }

    @Override
    public ChannelSettleInfo getInfoByAccountNo(String accountNo) {
        if (StringUtils.isEmpty(accountNo)) {
            return null;
        }
        ChannelSettleInfoEntity entity = createQuery(channelSettleInfoDao)
                .where(ChannelSettleInfo::getAccountNo, accountNo)
                .forUpdate()
                .single();
        if (entity == null) {
            return null;
        }

        return entity.copyTo(new ChannelSettleInfo());
    }

    @Override
    public ChannelWithdrawResponse withdraw(ChannelWithdrawRequest request) {

        ChannelWithdrawResponse response = new ChannelWithdrawResponse();

        Consumer<ChannelSettleLogEntity> channelInfoApply;

        String accountNo = request.getAccountNo();

        if (!StringUtils.isEmpty(request.getAccountNo())) {
            BeanValidator.tryValidate(request, SettleValidateGroup.ForAccount.class);
            channelInfoApply = entity -> {
                entity.setChannelProviderName(request.getChannelProviderName());
                entity.setChannelProvider(request.getChannelProvider());
                entity.setChannelName(request.getChannelName());

            };
        } else {
            BeanValidator.tryValidate(request, SettleValidateGroup.ForChannel.class);
            ChannelConfigEntity channelConfig = channelConfigService.selectByPk(request.getChannelId());
            if (channelConfig == null) {
                response.setError(ErrorCode.CHANNEL_CONFIG_ERROR);
                response.setMessage("渠道配置不存在");
                return response;
            }
            accountNo = channelConfig.getAccountNo();
            channelInfoApply = entity -> {
                entity.setChannelProviderName(channelConfig.getChannelProviderName());
                entity.setChannelProvider(channelConfig.getChannelProvider());
                entity.setChannelName(channelConfig.getChannelName());
            };
        }

        ChannelSettleInfo info = getInfoByAccountNo(accountNo);
        if (info == null) {
            response.setError(ErrorCode.CHANNEL_CONFIG_ERROR);
            response.setMessage("渠道结算配置不存在");
            return response;
        }
        if (info.getBalance() - request.getAmount() < 0) {
            response.setError(ErrorCode.INSUFFICIENT_BALANCE);
            response.setMessage("渠道余额不足");
            return response;
        }
        //下账
        channelSettleInfoDao.incrementAmount(accountNo, -request.getAmount());
        //记录交易日志
        ChannelSettleLogEntity logEntity = new ChannelSettleLogEntity();
        logEntity.setId(IDGenerator.SNOW_FLAKE_STRING.generate());
        logEntity.setAccountNo(accountNo);
        logEntity.setMerchantId(request.getMerchantId());
        logEntity.setCreateTime(new Date());
        logEntity.setMemo(request.getMemo());
        logEntity.setPaymentId(request.getPaymentId());
        logEntity.setMerchantId(request.getMerchantId());
        logEntity.setFundDirection(FundDirection.OUT);
        logEntity.setChannel(request.getChannel());
        logEntity.setChannelId(request.getChannelId());
        logEntity.setBalance(info.getBalance() - request.getAmount());
        logEntity.setMerchantName(request.getMerchantName());
        logEntity.setTransType(request.getTransType());
        logEntity.setAmount(request.getAmount());
        channelInfoApply.accept(logEntity);
        logDao.insert(logEntity);
        response.setSettleInfo(getInfoByAccountNo(accountNo));
        response.setSuccess(true);
        return response;
    }

}
