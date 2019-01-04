package org.hswebframework.payment.merchant.service.impl;

import org.hswebframework.payment.api.enums.NoticeStatus;
import org.hswebframework.payment.merchant.dao.NoticeDao;
import org.hswebframework.payment.merchant.entity.NoticeEntity;
import org.hswebframework.payment.merchant.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Lind
 * @since 1.0
 */
@Service
@Slf4j
public class LocalNoticeService extends GenericEntityService<NoticeEntity, String> implements NoticeService {
    @Autowired
    private NoticeDao noticeDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public NoticeDao getDao() {
        return noticeDao;
    }

    @Override
    public String insert(NoticeEntity entity) {
        entity.setStatus(NoticeStatus.CLOSE);
        return super.insert(entity);
    }
}
