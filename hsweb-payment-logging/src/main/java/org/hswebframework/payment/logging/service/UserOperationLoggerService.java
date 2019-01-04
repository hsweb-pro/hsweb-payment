package org.hswebframework.payment.logging.service;

import org.hswebframework.payment.logging.dao.UserOperationLoggerDao;
import org.hswebframework.payment.logging.entity.UserOperationLoggerEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
public class UserOperationLoggerService extends GenericEntityService<UserOperationLoggerEntity, String> {
    @Autowired
    private UserOperationLoggerDao userOperationLoggerDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public UserOperationLoggerDao getDao() {
        return userOperationLoggerDao;
    }
}
