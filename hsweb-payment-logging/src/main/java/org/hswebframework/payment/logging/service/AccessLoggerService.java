package org.hswebframework.payment.logging.service;

import org.hswebframework.payment.logging.dao.AccessLoggerDao;
import org.hswebframework.payment.logging.entity.AccessLoggerEntity;
import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
public class AccessLoggerService extends GenericEntityService<AccessLoggerEntity, String> {
    @Autowired
    private AccessLoggerDao accessLoggerDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public CrudDao<AccessLoggerEntity, String> getDao() {
        return accessLoggerDao;
    }
}
