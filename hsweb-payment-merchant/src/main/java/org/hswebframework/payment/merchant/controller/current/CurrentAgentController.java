package org.hswebframework.payment.merchant.controller.current;

import com.alibaba.fastjson.JSON;
import org.hswebframework.payment.api.account.AccountService;
import org.hswebframework.payment.api.account.reqeust.AccountQueryRequest;
import org.hswebframework.payment.api.account.response.AccountQueryResponse;
import org.hswebframework.payment.api.annotation.CurrentMerchant;
import org.hswebframework.payment.api.enums.*;
import org.hswebframework.payment.api.exception.BusinessException;
import org.hswebframework.payment.api.merchant.*;
import org.hswebframework.payment.api.merchant.config.MerchantChannelConfig;
import org.hswebframework.payment.api.merchant.config.MerchantConfigManager;
import org.hswebframework.payment.api.merchant.config.MerchantRateConfig;
import org.hswebframework.payment.api.merchant.config.StringSourceMerchantConfigHolder;
import org.hswebframework.payment.api.merchant.request.MerchantRegisterRequest;
import org.hswebframework.payment.api.merchant.request.QueryWithdrawRequest;
import org.hswebframework.payment.api.merchant.response.MerchantRegisterResponse;
import org.hswebframework.payment.api.merchant.response.QueryWithdrawResponse;
import org.hswebframework.payment.api.payment.MerchantTradingMonitorRequest;
import org.hswebframework.payment.api.payment.monitor.IntervalMonitorResult;
import org.hswebframework.payment.api.payment.monitor.PaymentMonitor;
import org.hswebframework.payment.api.payment.payee.Payee;
import org.hswebframework.payment.api.utils.MerchantRateChargeUtils;
import org.hswebframework.payment.merchant.controller.request.PayeeInfo;
import org.hswebframework.payment.merchant.controller.response.MerchantName;
import org.hswebframework.payment.merchant.controller.response.MerchantRateAndChannelConfig;
import org.hswebframework.payment.merchant.controller.response.PayeeConfigProperty;
import org.hswebframework.payment.merchant.controller.response.PayeeFormInfo;
import org.hswebframework.payment.merchant.entity.*;
import org.hswebframework.payment.merchant.service.impl.*;
import org.hswebframework.payment.merchant.entity.MerchantPayeeEntity;
import org.hswebframework.payment.merchant.entity.MerchantWithdrawEntity;
import org.hswebframework.payment.merchant.service.impl.*;
import org.hswebframework.payment.payment.entity.PaymentOrderEntity;
import org.hswebframework.payment.payment.service.LocalPaymentOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.payment.merchant.entity.AgentMerchantEntity;
import org.hswebframework.payment.merchant.entity.MerchantEntity;
import org.hswebframework.payment.merchant.utils.MerchantUtils;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lind
 * @since 1.0
 */

@RestController
@Authorize(permission = "merchant-manager", description = "当前登录代理接口")
@RequestMapping("current-agent")
@Validated
@Api(tags = "当前登录代理接口")
public class CurrentAgentController {

    @Autowired
    private LocalPaymentOrderService paymentService;

    @Autowired
    private MerchantConfigManager configManager;

    @Autowired
    private LocalMerchantService localMerchantService;

    @Autowired
    private LocalAgentMerchantService localAgentMerchantService;

    @Autowired
    private LocalMerchantWithdrawService localMerchantWithdrawService;

    @Autowired
    private AgentMerchantService agentMerchantService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PaymentMonitor paymentMonitor;

    @Autowired
    private LocalMerchantPayeeService payeeService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private LocalMerchantConfigService localMerchantConfigService;

    @GetMapping("/balance")
    @ApiOperation("余额查询")
    @Authorize(merge = false)
    public ResponseMessage<Long> getBalance(@CurrentMerchant AgentMerchant agentMerchant) {
        AgentMerchantEntity agentMerchantEntity = localAgentMerchantService
                .createQuery()
                .where("id", agentMerchant.getId())
                .single();
        AccountQueryRequest request = new AccountQueryRequest();
        request.setAccountNo(agentMerchantEntity.getAccountNo());
        AccountQueryResponse accountQueryResponse = accountService.queryAccount(request);
        accountQueryResponse.assertSuccess();
        long balance = accountQueryResponse.getAccount().getBalance();
        return ResponseMessage.ok(balance);
    }

    @GetMapping("/freeze-balance")
    @ApiOperation("冻结金额查询")
    @Authorize(merge = false)
    public ResponseMessage<Long> getFreezeBalance(@CurrentMerchant AgentMerchant agentMerchant) {
        AgentMerchantEntity agentMerchantEntity = localAgentMerchantService
                .createQuery()
                .where("id", agentMerchant.getId())
                .single();
        AccountQueryRequest request = new AccountQueryRequest();
        request.setAccountNo(agentMerchantEntity.getAccountNo());
        AccountQueryResponse accountQueryResponse = accountService.queryAccount(request);
        accountQueryResponse.assertSuccess();
        long freezeBalance = accountQueryResponse.getAccount().getFreezeBalance();
        return ResponseMessage.ok(freezeBalance);
    }

    @GetMapping("/order")
    @ApiOperation("代理商查询所有商户订单")
    @Authorize(merge = false)
    public ResponseMessage<PagerResult<PaymentOrderEntity>> agentAllMerchantOrder(@CurrentMerchant AgentMerchant agentMerchant,
                                                                                  QueryParamEntity paramEntity) {
        return ResponseMessage.ok(paymentService.queryAgentAllMerchantOrder(agentMerchant.getId(), paramEntity));
    }

    @GetMapping("/order-detail/{orderId}")
    @ApiOperation("代理获取商户订单详情")
    @Authorize(merge = false)
    public ResponseMessage<PaymentOrderEntity> queryOrderByIdAndMerchantId(@CurrentMerchant AgentMerchant agentMerchant,
                                                                           @PathVariable String orderId) {
        return ResponseMessage.ok(paymentService.queryOrderByIdAndMerchantId(agentMerchant.getId(), orderId));
    }

    @GetMapping("/merchants-name")
    @ApiOperation("根据ID查询商户基础信息（渲染商户选择下拉框）")
    @Authorize(merge = false)
    public ResponseMessage<List<MerchantName>> getMerchantName(@CurrentMerchant AgentMerchant agentMerchant) {

        List<MerchantEntity> merchantList = localMerchantService.queryMerchantByAgentId(agentMerchant.getId());
        List<MerchantName> collect = merchantList
                .stream()
                .map(e -> MerchantName
                        .builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .collect(Collectors.toList());
        return ResponseMessage.ok(collect);
    }


    @GetMapping("/agent-merchant")
    @ApiOperation("获取所有下级代理")
    @Authorize(merge = false)
    public ResponseMessage<List<AgentMerchant>> getAgentMerchantList(@CurrentMerchant AgentMerchant agentMerchant) {
        List<String> allChildrenAgentId = agentMerchantService.getAllChildrenAgentId(agentMerchant.getId());
        if (allChildrenAgentId.isEmpty()) {
            return ResponseMessage.ok();
        }
        List<AgentMerchantEntity> agentMerchantEntityList = localAgentMerchantService
                .createQuery()
                .where()
                .in("id", allChildrenAgentId)
                .listNoPaging();
        List<AgentMerchant> collect = agentMerchantEntityList.stream().map(entity -> FastBeanCopier.copy(entity, new AgentMerchant())).collect(Collectors.toList());
        return ResponseMessage.ok(collect);
    }


    @GetMapping("/merchants")
    @ApiOperation("代理商下属商户列表")
    @Authorize(merge = false)
    public ResponseMessage<List<Merchant>> getMerchantList(@CurrentMerchant AgentMerchant agentMerchant) {

        List<MerchantEntity> merchantList = localMerchantService
                .createQuery()
                .where("id$agent$children", agentMerchant.getId())
                .listNoPaging();
        List<Merchant> merchants = merchantList
                .stream()
                .map(e -> FastBeanCopier.copy(e, new Merchant()))
                .collect(Collectors.toList());
        return ResponseMessage.ok(merchants);
    }


    @GetMapping("/withdraw-log")
    @ApiModelProperty("提现申请记录")
    @Authorize(merge = false)
    public ResponseMessage<QueryWithdrawResponse> withdrawLog(@CurrentMerchant AgentMerchant agentMerchant, QueryParamEntity entity) {
        QueryWithdrawRequest request = new QueryWithdrawRequest();
        request.setMerchantId(agentMerchant.getId());

        //追加 merchant_id=? 查询条件
        entity.toNestQuery(query -> query.and(MerchantWithdrawEntity::getMerchantId, agentMerchant.getId()));
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


    @GetMapping("/merchant-info/{merchantId}")
    @ApiOperation("根据ID查询商户基础信息")
    @Authorize(merge = false)
    public ResponseMessage<Merchant> queryMerchantInfo(@CurrentMerchant AgentMerchant agentMerchant,
                                                       @PathVariable String merchantId) {
        MerchantEntity single = localMerchantService
                .createQuery()
                .where("agentId", agentMerchant.getId())
                .and("id", merchantId)
                .single();
        return ResponseMessage.ok(FastBeanCopier.copy(single, new Merchant()));
    }

    @GetMapping("/merchant-info/{agentId}")
    @ApiOperation("根据ID查询下级代理信息")
    @Authorize(merge = false)
    public ResponseMessage<AgentMerchant> queryAgentInfo(@CurrentMerchant AgentMerchant agentMerchant,
                                                         @PathVariable String agentId) {
        AgentMerchantEntity agent = localAgentMerchantService
                .createQuery()
                .where("parentId", agentMerchant.getId())
                .and("id", agentId)
                .single();
        return ResponseMessage.ok(FastBeanCopier.copy(agent, new AgentMerchant()));
    }


    @GetMapping("/merchant/rate-configs/{merchantId}")
    @Authorize(merge = false)
    @ApiOperation("查询商户费率配置")
    public ResponseMessage<List<MerchantRateAndChannelConfig>> queryMerchantRateConfig(@CurrentMerchant AgentMerchant agentMerchant, @PathVariable String merchantId) {

        MerchantEntity single = localMerchantService
                .createQuery()
                .where("merchantId$agent$children", agentMerchant.getId())
                .and("id", merchantId)
                .single();
        if (single == null) {
            return ResponseMessage.ok();
        }

        Map<String, MerchantRateConfig> responseList = MerchantUtils.getRateConfigById(configManager, merchantId)
                .stream()
                .collect(Collectors.toMap(config -> config.getTransType() + "-" + config.getChannel(), Function.identity()));


        //渠道配置
        Map<String, Boolean> channelMap = configManager
                .<MerchantChannelConfig>getConfigList(merchantId, MerchantConfigKey.SUPPORTED_CHANNEL)
                .map(list -> Stream.concat(list.stream(), list.stream()
                        .filter(e2 -> e2.getTransType().in(TransType.GATEWAY, TransType.QUICK))
                        .map(e2 -> FastBeanCopier.copy(e2, MerchantChannelConfig::new))
                        .peek(e2 -> e2.setChannel(null)))
                        .filter(e -> e.getTransType().in(TransType.GATEWAY, TransType.QUICK))

                        .collect(Collectors.groupingBy(e -> e.getTransType() + "-" + e.getChannel(),
                                Collectors.collectingAndThen(Collectors.toList(), lst -> lst.stream().allMatch(MerchantChannelConfig::isEnabled)))))
                .orElse(Collections.emptyMap());

        //代理费率
        List<MerchantRateAndChannelConfig> agentRateConfig = MerchantUtils.getRateConfigById(configManager, agentMerchant.getId())
                .stream()
                .filter(e -> e.getTransType().in(TransType.GATEWAY, TransType.QUICK))
                .map(e -> FastBeanCopier.copy(e, new MerchantRateAndChannelConfig()))
                .peek(e -> {
                    e.setEnable(channelMap.getOrDefault(e.getTransType() + "-" + e.getChannel(), false));
                    e.setMemo(e.getRate());
                    e.setRate("");
                    Optional.ofNullable(responseList.get(e.getTransType() + "-" + e.getChannel()))
                            .ifPresent(config -> {
                                e.setRate(config.getRate());
                                e.setRateType(config.getRateType());
                            });
                })
                .collect(Collectors.toList());


        //渠道状态
        return ResponseMessage.ok(agentRateConfig);
    }

    @PatchMapping("/merchant/config/{key}/{merchantId}")
    @Authorize(merge = false)
    @ApiOperation("修改商户配置")
    public ResponseMessage<Void> merchantRateConfig(@CurrentMerchant AgentMerchant agentMerchant,
                                                    @PathVariable String merchantId,
                                                    @PathVariable MerchantConfigKey key,
                                                    @RequestBody String configValue) {

        MerchantEntity single = localMerchantService
                .createQuery()
                .where("merchantId$agent$children", agentMerchant.getId())
                .and("id", merchantId)
                .single();
        if (single == null) {
            throw new AccessDenyException();
        }

        List<MerchantRateAndChannelConfig> channelConfigList = JSON.parseArray(configValue, MerchantRateAndChannelConfig.class);


        Map<String, Boolean> channelMap = channelConfigList
                .stream()
                .collect(Collectors.toMap(e -> e.getTransType() + "-" + e.getChannel(), MerchantRateAndChannelConfig::isEnable));


        //渠道配置
        List<MerchantChannelConfig> configs = configManager
                .<MerchantChannelConfig>getConfigList(merchantId, MerchantConfigKey.SUPPORTED_CHANNEL)
                .map(e -> e.stream().peek(conf -> {
                            if (conf.getTransType().eq(TransType.QUICK)) {
                                conf.setEnabled(channelMap.getOrDefault(conf.getTransType().getValue() + "-undefined", conf.isEnabled()));
                            } else {
                                conf.setEnabled(channelMap.getOrDefault(conf.getTransType().getValue() + "-" + conf.getChannel(), conf.isEnabled()));
                            }
                        }
                ).collect(Collectors.toList()))
                .orElse(null);

        configManager.saveConfig(merchantId, MerchantConfigKey.SUPPORTED_CHANNEL,
                JSON.toJSONString(configs));

        //如果是保存商户的费率配置，就复制代理已开通的渠道给商户（即代理支持的渠道商户也同样支持）
        if (key.equals(MerchantConfigKey.RATE_CONFIG)) {

            Map<String, MerchantRateConfig> rateConfigMap = configManager
                    .<MerchantRateConfig>getConfigList(merchantId, MerchantConfigKey.RATE_CONFIG)
                    .orElseGet(ArrayList::new)
                    .stream()
                    .collect(Collectors.toMap(config -> config.getTransType() + "-" + config.getChannel(), Function.identity()));


            //商户的费率
            new StringSourceMerchantConfigHolder(configValue)
                    .asList(MerchantRateConfig.class)
                    .ifPresent(list ->
                            list.forEach(rate -> {
                                long money = 100;
                                long charge = rate.getRateType().calculate(money, rate.getRate()).getCharge();

                                MerchantRateConfig agentRateConfig = MerchantRateChargeUtils
                                        .findConfig(configManager, agentMerchant.getId(), rate.getTransType(), rate.getChannel())
//                                        .map(agentRate -> agentRate.getRateType().calculate(money, agentRate.getRate()).getCharge())
                                        .orElseThrow(() -> ErrorCode.MERCHANT_CONFIG_ERROR.createException("费率配置错误"));
                                long agentCharge = agentRateConfig.getRateType().calculate(money, agentRateConfig.getRate()).getCharge();

                                if (charge < agentCharge) {
                                    throw ErrorCode.MERCHANT_CONFIG_ERROR.createException("费率不能低于:" + agentRateConfig.getRateType().getDescription(agentRateConfig.getRate()));
                                }
                                rateConfigMap.put(rate.getTransType() + "-" + rate.getChannel(), rate);
                            }));


            //复制代理的渠道道商户
//            configManager.getConfigList(agentMerchant.getId(), MerchantConfigKey.SUPPORTED_CHANNEL)
//                    .ifPresent(list -> configManager.saveConfig(merchantId, MerchantConfigKey.SUPPORTED_CHANNEL, JSON.toJSONString(list)));


            configManager.saveConfig(merchantId, key, JSON.toJSONString(new ArrayList<>(rateConfigMap.values())));

        } else {
            configManager.saveConfig(merchantId, key, configValue);
        }

        return ResponseMessage.ok();


    }

    @PostMapping("/merchant/config/{merchantId}")
    @Authorize(merge = false)
    @ApiOperation("修改商户支持的渠道")
    public ResponseMessage<Void> merchantChannelConfig(@CurrentMerchant AgentMerchant agentMerchant, @PathVariable String merchantId, @RequestBody String channels) {

        // TODO: 2019/1/3 保存商户渠道
        String replaceChannel = channels.replaceAll("\"", "");
        String channelString = replaceChannel.substring(1, replaceChannel.length() - 1);
        String[] channelArray = channelString.split(",");

        List<MerchantChannelConfig> channelConfigList = configManager
                .<MerchantChannelConfig>getConfigList(agentMerchant.getId(), MerchantConfigKey.SUPPORTED_CHANNEL)
                .orElse(null);

        Map<String, List<MerchantChannelConfig>> collect = channelConfigList
                .stream()
                .filter(e -> e.getTransType().in(TransType.GATEWAY, TransType.QUICK))
                .collect(Collectors.groupingBy(MerchantChannelConfig::getChannel));

        System.out.println("代理支持的渠道:" + collect);


        List<List<MerchantChannelConfig>> list = new ArrayList<>();
        for (String channel : channelArray) {
            System.out.println(channel);
            List<MerchantChannelConfig> configs = collect.get(channel);
            list.add(configs);
        }

        List<MerchantChannelConfig> collect1 = list.stream().flatMap(List::stream).collect(Collectors.toList());
        System.out.println(JSON.toJSONString(collect1));
        return ResponseMessage.ok();
    }

    @GetMapping("/channel-configs/{merchantId}")
    @ApiOperation("获取商户已开通通道信息")
    @Authorize(merge = false)
    public ResponseMessage<List<MerchantChannelConfig>> queryChannelConfig(@CurrentMerchant AgentMerchant agentMerchant, @PathVariable String merchantId) {

        MerchantEntity single = localMerchantService
                .createQuery()
                .where("merchantId$agent$children", agentMerchant.getId())
                .and("id", merchantId)
                .single();
        if (single == null) {
            return ResponseMessage.ok();
        }
        return ResponseMessage.ok(configManager
                .<MerchantChannelConfig>getConfigList(merchantId, MerchantConfigKey.SUPPORTED_CHANNEL)
                .orElse(null)
                .stream()
                .filter(e -> e.getTransType().in(TransType.QUICK, TransType.GATEWAY))
                .collect(Collectors.toList()));
    }

    @GetMapping("/agent/rate-configs/{agentId}")
    @Authorize(merge = false)
    @ApiOperation("查询代理费率配置")
    public ResponseMessage<List<MerchantRateConfig>> queryAgentRateConfig(@CurrentMerchant AgentMerchant agentMerchant, @PathVariable String agentId) {

        List<String> allChildrenAgentId = localAgentMerchantService.getAllChildrenAgentId(agentMerchant.getId());
        if (!allChildrenAgentId.contains(agentId)) {
            return ResponseMessage.ok();
        }
        List<MerchantRateConfig> responseList = MerchantUtils.getRateConfigById(configManager, agentMerchant.getId())
                .stream()
                .filter(e -> e.getTransType().in(TransType.GATEWAY, TransType.QUICK))
                .collect(Collectors.toList());
        return ResponseMessage.ok(responseList);
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
            @CurrentMerchant AgentMerchant agentMerchant) {
        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setAgentId(agentMerchant.getId());
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
            @CurrentMerchant AgentMerchant agentMerchant) {
        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setAgentId(agentMerchant.getId());
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
                                                                              @CurrentMerchant AgentMerchant agentMerchant) {
        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setAgentId(agentMerchant.getId());
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
                                                                           @CurrentMerchant AgentMerchant agentMerchant) {
        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setAgentId(agentMerchant.getId());
        request.setInterval(interval);
        request.setTimeUnit(timeUnit);
        return ResponseMessage.ok(paymentMonitor.countIntervalTrading(request, numbers));
    }

    @GetMapping("/sum/amount/{status}/{timeUnit}/{interval:\\d+}")
    @Authorize(merge = false)
    public ResponseMessage<Long> sumTodayStatusAmount(@PathVariable TimeUnit timeUnit,
                                                      @PathVariable int interval,
                                                      @PathVariable PaymentStatus status,
                                                      @CurrentMerchant AgentMerchant agentMerchant) {

        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setStatus(status);
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        request.setAgentId(agentMerchant.getId());
        return ResponseMessage.ok(paymentMonitor.sumTradingAmount(request));
    }

    @GetMapping("/sum/amount/{timeUnit}/{interval:\\d+}")
    @Authorize(merge = false)
    public ResponseMessage<Long> sumTodayAmount(@PathVariable TimeUnit timeUnit,
                                                @PathVariable int interval,
                                                @CurrentMerchant AgentMerchant agentMerchant) {

        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        request.setAgentId(agentMerchant.getId());
        return ResponseMessage.ok(paymentMonitor.sumTradingAmount(request));
    }


    @GetMapping("/channel/result/{timeUnit}/{interval:\\d+}")
    @Authorize(merge = false)
    public ResponseMessage<List<ChannelResult>> channelResult(@PathVariable TimeUnit timeUnit,
                                                              @PathVariable int interval,
                                                              @CurrentMerchant AgentMerchant agentMerchant) {

        MerchantTradingMonitorRequest request = new MerchantTradingMonitorRequest();
        request.setTimeUnit(timeUnit);
        request.setInterval(interval);
        request.setAgentId(agentMerchant.getId());

        Map<String, ChannelResult> resultMap = new HashMap<>();

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

    @GetMapping("/payee-config")
    @Authorize(merge = false)
    public ResponseMessage<List<PayeeFormInfo>> payeeConfig() {
        PayeeType[] values = PayeeType.values();
        List<PayeeFormInfo> list = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            PayeeFormInfo info = new PayeeFormInfo();
            List<PayeeConfigProperty> configProperties = new ArrayList<>();
            ReflectionUtils.doWithFields(values[i].getPayeeType(), field -> {
                ApiModelProperty swaggerAnnotation = field.getAnnotation(ApiModelProperty.class);
                if (swaggerAnnotation == null) {
                    return;
                }
                PayeeConfigProperty property = new PayeeConfigProperty();
                property.initFromSwaggerAnnotation(field, swaggerAnnotation);
                configProperties.add(property);
                info.setProperties(configProperties);
            });
            info.setName(values[i].getText());
            info.setPayeeType(values[i].getValue());
            list.add(info);
        }
        return ResponseMessage.ok(list);
    }

    @PostMapping("/payee-config")
    @Authorize(merge = false)
    public ResponseMessage<Void> savePayeeConfig(@CurrentMerchant AgentMerchant agentMerchant,
                                                 @RequestBody PayeeInfo payeeInfo) {

        MerchantPayeeEntity build = MerchantPayeeEntity
                .builder()
                .merchantId(agentMerchant.getId())
                .defaultWithdraw(false)
                .payeeInfoJson(payeeInfo.getPayeeInfoJson())
                .payeeType(payeeInfo.getPayeeType())
                .createTime(new Date())
                .build();
        Payee buildPayeeInfo = build.getPayeeInfo();
        build.setPayee(buildPayeeInfo.getPayee());
        build.setPayeeName(buildPayeeInfo.getPayeeName());
        payeeService.insert(build);
        return ResponseMessage.ok();
    }

    @GetMapping("/payee-config-detail")
    @Authorize(merge = false)
    public ResponseMessage<List<MerchantPayeeEntity>> payeeConfigDetail(@CurrentMerchant AgentMerchant agentMerchant) {
        List<MerchantPayeeEntity> payeeList = payeeService
                .createQuery()
                .where(MerchantPayeeEntity::getMerchantId, agentMerchant.getId())
                .listNoPaging();
        return ResponseMessage.ok(payeeList);
    }


    @PostMapping("/create-merchant")
    @Authorize(merge = false)
    public ResponseMessage<Merchant> createMerchant(@CurrentMerchant AgentMerchant agentMerchant, @RequestBody @Valid MerchantRegisterRequest request) {
        request.setAgentId(agentMerchant.getId());
        MerchantRegisterResponse response = merchantService.registerMerchant(request);
        //如果失败抛出异常
        response.assertSuccess(BusinessException::new);
        return ResponseMessage.ok(response.getMerchant());
    }

    @Getter
    @Setter
    public static class ChannelResult {

        private String comment;

        private long total;

        private long amount;

        public static ChannelResult create(String comment) {
            ChannelResult result = new ChannelResult();
            result.setComment(comment);
            return result;
        }
    }

}


