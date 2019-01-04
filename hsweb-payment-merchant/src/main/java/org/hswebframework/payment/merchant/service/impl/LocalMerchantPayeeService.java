package org.hswebframework.payment.merchant.service.impl;

import org.hswebframework.payment.api.merchant.MerchantPayeeService;
import org.hswebframework.payment.api.merchant.payee.MerchantPayee;
import org.hswebframework.payment.merchant.dao.MerchantPayeeDao;
import org.hswebframework.payment.merchant.entity.MerchantPayeeEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
public class LocalMerchantPayeeService extends GenericEntityService<MerchantPayeeEntity, String> implements MerchantPayeeService {

    @Autowired
    private MerchantPayeeDao merchantPayeeDao;

    @Override
    public MerchantPayee getMerchantPayee(String merchantId, String payeeId) {
        return convert(createQuery()
                .where(MerchantPayeeEntity::getMerchantId, merchantId)
                .and(MerchantPayeeEntity::getId, payeeId)
                .single());
    }

    private MerchantPayee convert(MerchantPayeeEntity entity) {
        if (entity == null) {
            return null;
        }
        MerchantPayee payee = new MerchantPayee();
        payee.setPayee(entity.getPayeeInfo());
        payee.setId(entity.getId());
        payee.setComment(entity.getComment());
        payee.setPayeeType(entity.getPayeeType());
        return payee;
    }

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public MerchantPayeeDao getDao() {
        return merchantPayeeDao;
    }
}
