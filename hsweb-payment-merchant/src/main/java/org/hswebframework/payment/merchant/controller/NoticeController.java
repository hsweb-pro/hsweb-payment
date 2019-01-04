package org.hswebframework.payment.merchant.controller;

import org.hswebframework.payment.api.annotation.CurrentMerchant;
import org.hswebframework.payment.api.enums.NoticeStatus;
import org.hswebframework.payment.api.enums.NoticeType;
import org.hswebframework.payment.api.merchant.AgentMerchant;
import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.merchant.entity.NoticeEntity;
import org.hswebframework.payment.merchant.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * @author Lind
 * @since 1.0
 */
@RestController
@RequestMapping("/notice")
@Api(tags = "公告管理",value = "公告管理")
@Authorize(permission = "notice")
public class NoticeController implements SimpleGenericEntityController<NoticeEntity,String, QueryParamEntity> {
    @Autowired
    private NoticeService noticeService;

    @Override
    public CrudService<NoticeEntity, String> getService() {
        return noticeService;
    }

    @ApiOperation("获取代理公告")
    @GetMapping("agent")
    @Authorize(merge = false)
    public ResponseMessage<PagerResult<NoticeEntity>> getAgentNotice(@CurrentMerchant AgentMerchant agentMerchant,QueryParamEntity paramEntity){
        return paramEntity.toNestQuery()
                .and("types$in$any", Collections.singletonList(NoticeType.AGENT))
                .and("status", NoticeStatus.OPEN)
                .execute(this::list);
    }

    @ApiOperation("获取商户公告")
    @GetMapping("merchant")
    @Authorize(merge = false)
    public ResponseMessage<PagerResult<NoticeEntity>> getMerchantNotice(@CurrentMerchant Merchant merchant,QueryParamEntity paramEntity){
        return paramEntity
                .toNestQuery()
                .and("types$in$any", Collections.singletonList(NoticeType.MERCHANT))
                .and("status",NoticeStatus.OPEN)
                .execute(this::list);
    }
}


