package org.hswebframework.payment.merchant.controller.current;

import com.alibaba.fastjson.JSON;
import org.hswebframework.payment.api.account.AccountService;
import org.hswebframework.payment.api.account.AccountTransLog;
import org.hswebframework.payment.api.account.AccountTransService;
import org.hswebframework.payment.api.account.reqeust.AccountQueryRequest;
import org.hswebframework.payment.api.account.reqeust.QueryMerchantTransLogRequest;
import org.hswebframework.payment.api.account.response.AccountQueryResponse;
import org.hswebframework.payment.api.account.response.AccountTransLogResponse;
import org.hswebframework.payment.api.annotation.CurrentMerchant;
import org.hswebframework.payment.api.enums.*;
import org.hswebframework.payment.api.merchant.AgentMerchant;
import org.hswebframework.payment.api.merchant.Merchant;
import org.hswebframework.payment.api.merchant.MerchantService;
import org.hswebframework.payment.api.merchant.MerchantWithdrawLog;
import org.hswebframework.payment.api.merchant.config.MerchantChannelConfig;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.merchant.config.MerchantRateConfig;
import org.hswebframework.payment.api.merchant.config.MerchantSettleConfig;
import org.hswebframework.payment.api.merchant.request.QueryWithdrawRequest;
import org.hswebframework.payment.api.merchant.response.QueryWithdrawResponse;
import org.hswebframework.payment.api.payment.MerchantTradingMonitorRequest;
import org.hswebframework.payment.api.payment.monitor.IntervalMonitorResult;
import org.hswebframework.payment.api.payment.monitor.PaymentMonitor;
import org.hswebframework.payment.merchant.controller.request.UpdatePasswordRequest;
import org.hswebframework.payment.merchant.controller.response.DownloadOrder;
import org.hswebframework.payment.merchant.entity.*;
import org.hswebframework.payment.merchant.service.impl.LocalMerchantService;
import org.hswebframework.payment.merchant.service.impl.LocalMerchantWithdrawService;
import org.hswebframework.payment.merchant.service.impl.LocalSubstituteService;
import org.hswebframework.payment.payment.controller.response.PaymentOrder;
import org.hswebframework.payment.payment.entity.PaymentOrderEntity;
import org.hswebframework.payment.payment.service.LocalPaymentOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.hswebframework.expands.office.excel.ExcelIO;
import org.hswebframework.expands.office.excel.config.Header;
import org.hswebframework.payment.merchant.entity.MerchantEntity;
import org.hswebframework.payment.merchant.entity.MerchantWithdrawEntity;
import org.hswebframework.payment.merchant.entity.SubstituteDetailEntity;
import org.hswebframework.payment.merchant.entity.SubstituteEntity;
import org.hswebframework.payment.merchant.utils.MerchantUtils;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.TwoFactor;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.service.authorization.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hswebframework.payment.merchant.controller.current.CurrentAgentController.*;

@RestController
@Authorize(permission = "merchant-manager", description = "商户接口")
@RequestMapping("current-merchant")
@Validated
@Api(tags = "商户接口")
public class CurrentMerchantController implements QueryController<MerchantEntity, String, QueryParamEntity> {

    @Autowired
    private MerchantConfigManager configManager;

    @Autowired
    private LocalMerchantService localMerchantService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserTokenManager userTokenManager;

    @Autowired
    private LocalMerchantWithdrawService localMerchantWithdrawService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PaymentMonitor paymentMonitor;

    @Autowired
    private LocalPaymentOrderService paymentService;

    @Autowired
    private AccountTransService accountTransService;

    @Autowired
    private LocalSubstituteService substituteService;

    @GetMapping("/balance")
    @ApiOperation("余额查询")
    @Authorize(merge = false)
    public ResponseMessage<Long> getBalance(@CurrentMerchant Merchant merchant) {
        MerchantEntity merchantEntity = localMerchantService
                .createQuery()
                .where("id", merchant.getId())
                .single();
        AccountQueryRequest request = new AccountQueryRequest();
        request.setAccountNo(merchantEntity.getAccountNo());
        AccountQueryResponse accountQueryResponse = accountService.queryAccount(request);
        accountQueryResponse.assertSuccess();
        long balance = accountQueryResponse.getAccount().getBalance();
        return ResponseMessage.ok(balance);
    }

    @GetMapping("/freeze-balance")
    @ApiOperation("冻结金额查询")
    @Authorize(merge = false)
    public ResponseMessage<Long> getFreezeBalance(@CurrentMerchant Merchant merchant) {
        AccountQueryRequest request = new AccountQueryRequest();
        request.setAccountNo(merchant.getAccountNo());
        AccountQueryResponse accountQueryResponse = accountService.queryAccount(request);
        accountQueryResponse.assertSuccess();
        long freezeBalance = accountQueryResponse.getAccount().getFreezeBalance();
        return ResponseMessage.ok(freezeBalance);
    }

    @GetMapping("/withdraw-log")
    @ApiModelProperty("提现申请记录")
    @Authorize(merge = false)
    public ResponseMessage<QueryWithdrawResponse> withdrawLog(@CurrentMerchant Merchant merchant, QueryParamEntity entity) {
        QueryWithdrawRequest request = new QueryWithdrawRequest();
        request.setMerchantId(merchant.getId());

        //追加 merchant_id=? 查询条件
        entity.toNestQuery(query -> query.and(MerchantWithdrawEntity::getMerchantId, merchant.getId()));
        PagerResult<MerchantWithdrawEntity> result = localMerchantWithdrawService.selectPager(entity);
        List<MerchantWithdrawLog> collect = result.getData()
                .stream()
                .map(e -> FastBeanCopier.copy(e, new MerchantWithdrawLog()))
                .collect(Collectors.toList());
        QueryWithdrawResponse response = new QueryWithdrawResponse();
        response.setSuccess(true);
        response.setWithdrawLogList(collect);
        response.setTotal(result.getTotal());
        return ResponseMessage.ok(response);
    }


    @PatchMapping("/me/config/{key}")
    @Authorize(merge = false)//不合并类上的注解,这里只需要登录就可以修改配置
    @ApiOperation("商户修改自己的配置")
    public ResponseMessage<Void> updateMeConfig(@CurrentMerchant String currentMerchantId,//当前登录用户
                                                @PathVariable MerchantConfigKey key,
                                                @RequestBody String configValue) {
        Assert.isTrue(key.isMerchantWritable(), "无法修改此配置");
        configManager.saveConfig(currentMerchantId, key, configValue);
        return ResponseMessage.ok();
    }


    @PostMapping("/settle-configs")
    @Authorize(merge = false)
    @ApiOperation("商户自己更新结算配置")
    @TwoFactor(value = "settle")
    public ResponseMessage<Boolean> updateSettleConfig(@CurrentMerchant(agentOrMerchant = true) String currentMerchantId,
                                                       @RequestBody MerchantSettleConfig config) {
        configManager.saveConfig(currentMerchantId, MerchantConfigKey.SETTLE_CONFIG, JSON.toJSONString(config));
        return ResponseMessage.ok(true);
    }

    @GetMapping("/settle-configs")
    @ApiOperation("获取商户结算配置")
    @Authorize(merge = false)
    @TwoFactor(value = "settle") //2步验证
    public ResponseMessage<MerchantSettleConfig> queryMerchantConfig(@CurrentMerchant(agentOrMerchant = true) String currentMerchantId) {
        MerchantSettleConfig configs = (MerchantSettleConfig) configManager.getConfig(currentMerchantId, MerchantConfigKey.SETTLE_CONFIG)
                .orElse(null);
        return ResponseMessage.ok(configs);
    }

    @PutMapping("/base-info")
    @ApiOperation("商户自己更新基础信息")
    @Authorize(merge = false)
    public ResponseMessage<Boolean> updateMerchantInfo(@CurrentMerchant(agentOrMerchant = true) String currentMerchantId, @RequestBody Merchant merchant) {

        return ResponseMessage.ok(localMerchantService.updateMerchantBaseInfo(currentMerchantId, merchant));
    }

    @PutMapping("/main-info")
    @ApiOperation("商户更新备案信息")
    @Authorize(merge = false)
    public ResponseMessage<Boolean> updateMerchantBaseInfo(@CurrentMerchant String currentMerchantId, @RequestBody Merchant merchant) {

        return ResponseMessage.ok(localMerchantService.updateMerchantMainInfo(currentMerchantId, merchant));
    }

    @GetMapping("base-info")
    @ApiOperation("查询商户基础信息")
    @Authorize(merge = false)
    public ResponseMessage<Merchant> queryMerchantInfo(@CurrentMerchant String currentMerchantId) {
        MerchantEntity merchantEntity = localMerchantService.createQuery().where("id", currentMerchantId).single();
        return ResponseMessage.ok(FastBeanCopier.copy(merchantEntity, new Merchant()));
    }


    @GetMapping("/rate-configs")
    @ApiOperation("获取商户费率配置")
    @Authorize(merge = false)
    public ResponseMessage<PagerResult<MerchantRateConfig>> queryMerchantRateConfig(@CurrentMerchant(agentOrMerchant = true) String currentMerchantId) {
        List<MerchantRateConfig> responses = MerchantUtils.getRateConfigById(configManager, currentMerchantId);
//        List<MerchantRateConfig> responses = configList.stream()
//                .filter(e -> e.getTransType().in(TransType.GATEWAY, TransType.QUICK))
//                .collect(Collectors.toList());
        PagerResult<MerchantRateConfig> result = new PagerResult<>();
        result.setTotal(responses.size());
        result.setData(responses);
        return ResponseMessage.ok(result);
    }

    @GetMapping("/channel-configs")
    @ApiOperation("获取商户已开通通道信息")
    @Authorize(merge = false)
    public ResponseMessage<List<MerchantChannelConfig>> queryChannelConfig(@CurrentMerchant(agentOrMerchant = true) String merchantId) {
        return ResponseMessage.ok(configManager
                .<MerchantChannelConfig>getConfigList(merchantId, MerchantConfigKey.SUPPORTED_CHANNEL)
                .orElse(null));
    }


    @ApiOperation("商户修改密码")
    @Authorize(merge = false)
    @PostMapping("/update-password")
    @TwoFactor(value = "password", timeout = 0)
    public ResponseMessage<Boolean> updateMerchantPassword(@CurrentMerchant Merchant merchant, @RequestBody UpdatePasswordRequest request) {
        boolean checkPassword = request.getNewPassword().equals(request.getAgainPassword());
        if (checkPassword) {
            userService.updatePassword(merchant.getUserId(), request.getOldPassword(), request.getAgainPassword());
            userTokenManager.signOutByUserId(merchant.getUserId());
        } else {
            throw ErrorCode.ILLEGAL_PARAMETERS.createException();
        }
        return ResponseMessage.ok(true);
    }


    @GetMapping("/status")
    @ApiOperation("商户资料状态")
    @Authorize(merge = false)
    public ResponseMessage<String> infoStatus(@CurrentMerchant Merchant merchant) {
        return ResponseMessage.ok(merchant.getStatus() == null ? MerchantStatus.PENDING_REVIEW.getValue()
                : merchant.getStatus().getValue());
    }


    @GetMapping("/order")
    @ApiOperation("获取商户订单列表")
    @Authorize(merge = false)
    public ResponseMessage<PagerResult<PaymentOrderEntity>> queryMerchantOrder(@CurrentMerchant String merchantId,
                                                                               QueryParamEntity entity) {
        entity.excludes("channel", "channelId", "channelProvider", "merchantId", "productId", "requestJson", "responseJson");
        return ResponseMessage
                .ok(entity.toNestQuery(query -> query.and("merchantId", merchantId))
                        .execute(paymentService::queryMerchantOrder));
    }

    @GetMapping("/order/{orderId}")
    @ApiOperation("获取商户订单详情")
    @Authorize(merge = false)
    public ResponseMessage<PaymentOrder> queryOrderById(@CurrentMerchant String merchantId,
                                                        @PathVariable String orderId) {
        QueryMerchantTransLogRequest request = new QueryMerchantTransLogRequest();
        request.setMerchantId(merchantId);
        request.setPaymentId(orderId);
        AccountTransLogResponse response = accountTransService.queryMerchantTransLog(request);
        List<AccountTransLog> transLogList = response.getTransLogList();

        PaymentOrderEntity paymentOrderEntity = paymentService.queryOrderByMerchantIdAndOrderId(merchantId, orderId);

        AccountTransLog accountTransLog = transLogList
                .stream()
                .filter(e ->
                        e.getTransType().equals(TransType.AGENT_CHARGE.getText())
                                || e.getTransType().equals(TransType.CHARGE.getText())
                )
                .findAny()
                .orElse(new AccountTransLog());

        PaymentOrder order = FastBeanCopier.copy(paymentOrderEntity, new PaymentOrder());
        order.setServiceComment(accountTransLog.getComment());
        order.setServiceAmount(accountTransLog.getTransAmount());

        return ResponseMessage.ok(order);
    }


    @GetMapping("/download")
    @ApiOperation("导出商户订单")
    @Authorize(merge = false)
    @SneakyThrows
    public void downloadOrder(@CurrentMerchant String currentMerchantId,
                              QueryParamEntity entity,
                              HttpServletResponse response) {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("导出订单" + System.currentTimeMillis() + ".xlsx", "utf-8"));
        List<PaymentOrderEntity> orderEntities = entity.toNestQuery((query) -> query.and("merchantId", currentMerchantId))
                .execute(paymentService::select);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<DownloadOrder> orderList = orderEntities
                .stream()
                .map(order -> {
                    DownloadOrder copy = FastBeanCopier.copy(order, new DownloadOrder());
                    copy.setAmount(TransRateType.TransCharge.format(order.getAmount()));
                    copy.setTransType(order.getTransType() == null ? "" : order.getTransType().getText());
                    copy.setCreateTime(formatter.format(order.getCreateTime()));
                    copy.setUpdateTime(order.getUpdateTime() != null ? formatter.format(order.getUpdateTime()) : "");
                    copy.setCompleteTime(order.getCompleteTime() != null ? formatter.format(order.getCompleteTime()) : "");
                    copy.setNotified(order.getNotified() ? "是" : "否");
                    copy.setNotifyTime(order.getNotifyTime() != null ? formatter.format(order.getNotifyTime()) : "");
                    copy.setStatus(order.getStatus().getText());
                    return copy;
                }).collect(Collectors.toList());

        List<Header> headers = Stream.of(
                "交易订单号:id",
                "交易类型:transType",
                "渠道名称:channelName",
                "订单号:orderId",
                "产品名称:productName",
                "商户ID:merchantId",
                "商户名称:merchantName",
                "交易金额(元):amount",
                "币种:currency",
                "创建时间:createTime",
                "更新时间:updateTime",
                "完成时间:completeTime",
                "是否通知:notified",
                "通知时间:notifyTime",
                "订单状态:status")
                .map(str -> str.split("[:]"))
                .map(arr -> new Header(arr[0], arr[1]))
                .collect(Collectors.toList());

        ExcelIO.write(response.getOutputStream(), headers, (List) orderList);

    }

    /**
     * 近七天每天交易额数据: /monitor/trading/sum/DAY/1/7
     * <p>
     * 统计5次每2个月为一个间隔的数据: /monitor/trading/sum/MONTH/2/5
     */
    @GetMapping("/sum/trading/{timeUnit}/{interval:\\d+}/{numbers:\\d+}")
    @Authorize(merge = false)
    public ResponseMessage<List<IntervalMonitorResult>> sumTradingAmount(
            @PathVariable TimeUnit timeUnit,
            @PathVariable int interval,
            @PathVariable int numbers,
            @CurrentMerchant Merchant merchant) {
        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setMerchantId(merchant.getId());
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        return ResponseMessage.ok(paymentMonitor.sumIntervalTradingAmount(request, numbers));
    }


    @GetMapping("/trading/sum-by-channel/{timeUnit}/{interval:\\d+}/{numbers:\\d+}")
    @Authorize(merge = false)
    public ResponseMessage<List<IntervalMonitorResult>> sumTradingGroupByChannel(
            @PathVariable TimeUnit timeUnit,
            @PathVariable int interval,
            @PathVariable int numbers,
            @CurrentMerchant Merchant merchant) {
        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setMerchantId(merchant.getId());
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        return ResponseMessage.ok(paymentMonitor.sumIntervalTradingGroupByChannel(request, numbers));
    }

    @GetMapping("/count/order/{status}/{timeUnit}/{interval:\\d+}/{numbers:\\d+}")
    @Authorize(merge = false)
    public ResponseMessage<List<IntervalMonitorResult>> countTodayStatusOrder(@PathVariable TimeUnit timeUnit,
                                                                              @PathVariable int interval,
                                                                              @PathVariable int numbers,
                                                                              @PathVariable PaymentStatus status,
                                                                              @CurrentMerchant Merchant merchant) {
        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setMerchantId(merchant.getId());
        request.setInterval(interval);
        request.setStatus(status);
        request.setTimeUnit(timeUnit);
        return ResponseMessage.ok(paymentMonitor.countIntervalTrading(request, numbers));
    }

    @GetMapping("/count/order/{timeUnit}/{interval:\\d+}/{numbers:\\d+}")
    @Authorize(merge = false)
    public ResponseMessage<List<IntervalMonitorResult>> countTodayAllOrder(@PathVariable TimeUnit timeUnit,
                                                                           @PathVariable int interval,
                                                                           @PathVariable int numbers,
                                                                           @CurrentMerchant Merchant merchant) {
        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setMerchantId(merchant.getId());
        request.setInterval(interval);
        request.setTimeUnit(timeUnit);
        return ResponseMessage.ok(paymentMonitor.countIntervalTrading(request, numbers));
    }

    @GetMapping("/sum/amount/{status}/{timeUnit}/{interval:\\d+}")
    @Authorize(merge = false)
    public ResponseMessage<Long> sumTodayStatusAmount(@PathVariable TimeUnit timeUnit,
                                                      @PathVariable int interval,
                                                      @PathVariable PaymentStatus status,
                                                      @CurrentMerchant Merchant merchant){

        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setStatus(status);
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        request.setMerchantId(merchant.getId());
        return ResponseMessage.ok(paymentMonitor.sumTradingAmount(request));
    }

    @GetMapping("/sum/amount/{timeUnit}/{interval:\\d+}")
    @Authorize(merge = false)
    public ResponseMessage<Long> sumTodayAmount(@PathVariable TimeUnit timeUnit,
                                                @PathVariable int interval,
                                                @CurrentMerchant Merchant merchant){

        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        request.setMerchantId(merchant.getId());
        return ResponseMessage.ok(paymentMonitor.sumTradingAmount(request));
    }

    @GetMapping("/channel/result/{timeUnit}/{interval:\\d+}")
    @Authorize(merge = false)
    public ResponseMessage<List<CurrentAgentController.ChannelResult>> channelResult(@PathVariable TimeUnit timeUnit,
                                                                                     @PathVariable int interval,
                                                                                     @CurrentMerchant Merchant merchant) {

        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        request.setMerchantId(merchant.getId());

        Map<String, CurrentAgentController.ChannelResult> resultMap = new HashMap<>();

        List<IntervalMonitorResult> count = paymentMonitor.countIntervalTradingGroupByChannel(request, 1);
        List<IntervalMonitorResult> sum = paymentMonitor.sumIntervalTradingGroupByChannel(request, 1);

        Map<String, Long> countMap = count.stream()
                .collect(Collectors.groupingBy(IntervalMonitorResult::getComment,
                        Collectors.collectingAndThen(Collectors.summarizingLong(IntervalMonitorResult::getTotal), LongSummaryStatistics::getSum)));

        countMap.forEach((comment, total) ->
                resultMap.computeIfAbsent(comment, ChannelResult::create).setTotal(total)
        );

        Map<String, Long> sumMap = sum.stream()
                .collect(Collectors.groupingBy(IntervalMonitorResult::getComment,
                        Collectors.collectingAndThen(Collectors.summarizingLong(IntervalMonitorResult::getTotal), LongSummaryStatistics::getSum)));

        sumMap.forEach((comment, total) ->
                resultMap.computeIfAbsent(comment, ChannelResult::create).setAmount(total)
        );


        return ResponseMessage.ok(new ArrayList<>(resultMap.values()));
    }


    @GetMapping("/substitute")
    @Authorize(merge = false)
    public ResponseMessage<PagerResult<SubstituteEntity>> listSubstitute(@CurrentMerchant Merchant merchant, QueryParamEntity paramEntity){

        return ResponseMessage.ok(paramEntity
                .toNestQuery(query->query
                        .and("merchantId",merchant.getId()))
                .execute(substituteService::selectPager));
    }

    @GetMapping("/{substituteId}/details")
    @Authorize(merge = false)
    public ResponseMessage<List<SubstituteDetailEntity>> getDetails(@PathVariable String substituteId, @CurrentMerchant Merchant merchant, QueryParamEntity entity) {
        return ResponseMessage.ok(substituteService.selectDetailBySubstituteIdAndMerchantId(substituteId,merchant.getId(),entity));
    }

    @Override
    public LocalMerchantService getService() {
        return localMerchantService;
    }
}
