package org.hswebframework.payment.payment.service.impl;

import org.hswebframework.payment.payment.dao.BindCardDao;
import org.hswebframework.payment.payment.entity.BindCardEntity;
import org.hswebframework.payment.payment.service.LocalBindCardService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class LocalBindCardServiceImpl extends GenericEntityService<BindCardEntity, String> implements LocalBindCardService {

    @Autowired
    private BindCardDao bindCardDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public BindCardEntity createEntity() {
        BindCardEntity entity = super.createEntity();
        entity.setCreateTime(new Date());
        return entity;
    }

    @Override
    public BindCardDao getDao() {
        return bindCardDao;
    }
}
