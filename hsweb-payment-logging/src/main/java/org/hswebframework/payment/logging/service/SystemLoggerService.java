package org.hswebframework.payment.logging.service;

import org.hswebframework.payment.logging.dao.SystemLoggerDao;
import org.hswebframework.payment.logging.entity.SystemLoggerEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
public class SystemLoggerService extends GenericEntityService<SystemLoggerEntity, String> {
    @Autowired
    private SystemLoggerDao systemLoggerDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public SystemLoggerDao getDao() {
        return systemLoggerDao;
    }
}
