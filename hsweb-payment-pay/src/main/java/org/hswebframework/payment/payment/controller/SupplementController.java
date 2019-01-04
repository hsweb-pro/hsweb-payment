package org.hswebframework.payment.payment.controller;

import org.hswebframework.payment.api.payment.supplement.Supplement;
import org.hswebframework.payment.api.payment.supplement.request.SupplementCloseRequest;
import org.hswebframework.payment.api.payment.supplement.request.SupplementCompleteRequest;
import org.hswebframework.payment.api.payment.supplement.request.SupplementCreateRequest;
import org.hswebframework.payment.api.payment.supplement.request.SupplementRollbackRequest;
import org.hswebframework.payment.api.payment.supplement.response.SupplementCloseResponse;
import org.hswebframework.payment.api.payment.supplement.response.SupplementCompleteResponse;
import org.hswebframework.payment.api.payment.supplement.response.SupplementCreateResponse;
import org.hswebframework.payment.api.payment.supplement.response.SupplementRollbackResponse;
import org.hswebframework.payment.payment.entity.SupplementEntity;
import org.hswebframework.payment.payment.service.impl.LocalSupplementService;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@RequestMapping("/supplement")
@Authorize(permission = "supplement", description = "交易补登")
public class SupplementController implements QueryController<SupplementEntity, String, QueryParamEntity> {

    @Autowired
    private LocalSupplementService supplementService;

    @Override
    public LocalSupplementService getService() {
        return supplementService;
    }

    @PostMapping("/create")
    @Authorize(action = Permission.ACTION_ADD)
    public ResponseMessage<String> create(@RequestBody SupplementCreateRequest request) {
        Authentication.current()
                .map(Authentication::getUser)
                .ifPresent(user -> {
                    request.setCreatorId(user.getId());
                    request.setCreatorName(user.getName());
                });
        SupplementCreateResponse response = supplementService.create(request);
        response.assertSuccess();
        return ResponseMessage.ok(response.getSupplement().getId());
    }

    @PutMapping("/rollback")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<String> rollback(@RequestBody SupplementRollbackRequest request) {
        SupplementRollbackResponse response = supplementService.rollback(request);
        response.assertSuccess();
        return ResponseMessage.ok();
    }

    @PutMapping("/complete")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<Void> complete(@RequestBody SupplementCompleteRequest request) {
        SupplementCompleteResponse response = supplementService.complete(request);
        response.assertSuccess();
        return ResponseMessage.ok();
    }

    @PutMapping("/close")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<String> close(@RequestBody SupplementCloseRequest request) {
        SupplementCloseResponse response = supplementService.close(request);
        response.assertSuccess();
        return ResponseMessage.ok();
    }
}
