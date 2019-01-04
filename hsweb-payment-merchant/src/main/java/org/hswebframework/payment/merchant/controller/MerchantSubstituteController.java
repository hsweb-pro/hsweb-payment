package org.hswebframework.payment.merchant.controller;

import org.hswebframework.payment.merchant.entity.SubstituteDetailEntity;
import org.hswebframework.payment.merchant.entity.SubstituteEntity;
import org.hswebframework.payment.merchant.service.impl.LocalSubstituteService;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@RequestMapping("merchant/substitute")
@Authorize(permission = "substitute", description = "代付管理")
public class MerchantSubstituteController implements QueryController<SubstituteEntity, String, QueryParamEntity> {

    @Autowired
    private LocalSubstituteService localMerchantService;

    @Override
    public LocalSubstituteService getService() {
        return localMerchantService;
    }

    @GetMapping("/{substituteId}/details")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<List<SubstituteDetailEntity>> getDetails(@PathVariable String substituteId, QueryParamEntity entity) {
        return ResponseMessage.ok(localMerchantService.selectDetailBySubstituteId(substituteId,entity));
    }
}
