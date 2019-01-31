//组件信息
var info = {
    groupId: "org.hswebframework.pro",
    artifactId: "hsweb-payment-pay",
    version: "1.0.8",
    website: "payment.hsweb.pro",
    author: "zhouhao",
    comment: "支付模块"
};

//版本更新信息
var versions = [
    {
        version: "1.0.1",
        upgrade: function (context) {
            var database = context.database;
            //增加渠道供应商
            database.createOrAlter("pay_order")
                .addColumn().name("channel_provider").varchar(128).notNull().comment("渠道供应商").commit()
                .addColumn().name("channel_provider_name").notNull().varchar(128).comment("渠道供应商名称").commit()
                .comment("支付订单表").commit();

            database.createOrAlter("pay_channel_config")
                .addColumn().name("channel_provider").varchar(128).notNull().comment("渠道供应商").commit()
                .addColumn().name("channel_provider_name").varchar(128).notNull().comment("渠道供应商名称").commit()
                .comment("渠道配置表").commit();
        }
    },
    {
        version: "1.0.2",
        upgrade: function (context) {
            var database = context.database;
            database.createOrAlter("pay_order")
                .addColumn().name("comment").varchar(2048).comment("说明").commit()
                .comment("支付订单表").commit();

            database.createOrAlter("pay_channel_config")
                .addColumn().name("channel_name").varchar(32).notNull().comment("渠道名称").commit()
                .addColumn().name("account_no").varchar(32).notNull().comment("资金账户号").commit()
                .addColumn().name("rate_type").varchar(32).comment("此渠道费率类型").commit()
                .addColumn().name("rate").varchar(2048).comment("费率").commit()
                .comment("渠道配置表").commit();
        }
    }, {
        version: "1.0.3",
        upgrade: function (context) {
            var database = context.database;
            database.createOrAlter("pay_channel_settle")
                .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
                .addColumn().name("name").varchar(128).notNull().comment("名称").commit()
                .addColumn().name("account_no").varchar(32).notNull().comment("资金账号").commit()
                .addColumn().name("balance").jdbcType(JDBCType.BIGINT).notNull().comment("渠道余额(单位:分)").commit()
                .addColumn().name("create_time").datetime().notNull().comment("创建时间").commit()
                .addColumn().name("comment").varchar(2048).comment("说明").commit()
                .index().name("idx_channel_settle_acc_no").column("account_no").unique().commit()
                .comment("渠道结算信息表").commit();

            database.createOrAlter("pay_channel_settle_log")
                .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
                .addColumn().name("channel_name").varchar(128).notNull().comment("渠道名称").commit()
                .addColumn().name("payment_id").varchar(32).notNull().comment("支付订单ID").commit()
                .addColumn().name("merchant_id").varchar(32).notNull().comment("商户ID").commit()
                .addColumn().name("merchant_name").varchar(128).notNull().comment("商户名称").commit()
                .addColumn().name("channel_provider").varchar(128).notNull().comment("渠道服务商").commit()
                .addColumn().name("channel_provide_name").varchar(128).notNull().comment("渠道服务商名称").commit()
                .addColumn().name("fund_direction").varchar(128).notNull().comment("资金方向").commit()
                .addColumn().name("trans_type").varchar(128).notNull().comment("交易类型").commit()
                .addColumn().name("balance").jdbcType(JDBCType.BIGINT).notNull().comment("当前余额").commit()
                .addColumn().name("account_no").varchar(32).notNull().comment("渠道账号").commit()
                .addColumn().name("amount").jdbcType(JDBCType.BIGINT).notNull().comment("交易金额").commit()
                .addColumn().name("create_time").datetime().notNull().comment("创建时间").commit()
                .addColumn().name("memo").varchar(2048).comment("说明").commit()
                .index().name("idx_c_s_log_acc_no").column("account_no").commit()
                .index().name("idx_c_s_log_acc_pmt_tt")
                .column("account_no").column("payment_id").column("trans_type").unique().commit()
                .comment("渠道结算流水")
                .commit();

        }
    }, {
        version: "1.0.4",
        upgrade: function (context) {
            var database = context.database;
            //增加渠道供应商
            database.createOrAlter("pay_settle_supplement")
                .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
                .addColumn().name("source_account_no").varchar(32).comment("借方账户").commit()
                .addColumn().name("target_account_no").varchar(32).comment("贷方账户").commit()
                .addColumn().name("source_account_name").varchar(128).comment("借方账户名称").commit()
                .addColumn().name("target_account_name").varchar(128).comment("贷方账户名称").commit()
                .addColumn().name("source_amount").jdbcType(JDBCType.BIGINT).notNull().comment("借方金额").commit()
                .addColumn().name("target_amount").jdbcType(JDBCType.BIGINT).notNull().comment("贷方金额").commit()
                .addColumn().name("create_time").datetime().notNull().comment("订单创建时间").commit()
                .addColumn().name("supplement_time").datetime().comment("补登时间").commit()
                .addColumn().name("creator_id").notNull().varchar(32).comment("创建人ID").commit()
                .addColumn().name("creator_name").notNull().varchar(64).comment("创建人姓名").commit()
                .addColumn().name("status").varchar(32).comment("状态").commit()
                .addColumn().name("remark").varchar(2048).comment("备注").commit()
                .comment("结算渠道交易补登").commit();

        }
    }, {
        version: "1.0.5",
        upgrade: function (context) {
            var database = context.database;
            database.createOrAlter("pay_order")
                .addColumn().name("real_amount").jdbcType(JDBCType.BIGINT).notNull().comment("实际交易金额(单位:分)").commit()
                .comment("支付订单表").commit();
        }
    }
    , {
        version: "1.0.6",
        upgrade: function (context) {
            var database = context.database;
            database.createOrAlter("pay_channel_settle_log")
                .addColumn().name("channel").varchar(64).notNull().comment("渠道").commit()
                .addColumn().name("channel_id").varchar(32).notNull().comment("渠道ID").commit()
                .comment("渠道结算流水").commit();
        }
    },
    {
        version: "1.0.7",
        upgrade: function (context) {
            var database = context.database;
            database.createOrAlter("pay_order")
                .addColumn().name("channel_result").clob().comment("渠道返回结果").commit()
                .comment("支付订单表").commit();

            database.createOrAlter("pay_notify_log")
                .addColumn().name("error_reason").clob().comment("失败原因").commit()
                .comment("支付通知记录表").commit();

        }
    }, {
        version: "1.0.8",
        upgrade: function (context) {
            var database = context.database;
            database.createOrAlter("pay_mer_channel_bind")
                .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
                .addColumn().name("config_id").varchar(32).notNull().comment("渠道配置ID").commit()
                .addColumn().name("config_name").varchar(128).comment("渠道配置名称").commit()
                .addColumn().name("merchant_id").varchar(32).notNull().comment("商户id").commit()
                .addColumn().name("merchant_name").varchar(32).comment("商户名称").commit()
                .index().name("idx_pmcb_config_id_merchant_id")
                .column("config_id").column("merchant_id")
                .unique().commit()//配置id,商户id唯一索引
                .comment("商户渠道绑定表")
                .commit();
        }
    }
];
var JDBCType = java.sql.JDBCType;

function install(context) {
    var database = context.database;
    database.createOrAlter("pay_order")
        .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
        .addColumn().name("trans_type").varchar(128).notNull().comment("支付方式").commit()
        .addColumn().name("channel").varchar(128).notNull().comment("支付渠道").commit()
        .addColumn().name("channel_name").varchar(128).notNull().comment("渠道名称").commit()
        .addColumn().name("channel_id").varchar(128).comment("渠道ID").commit()
        .addColumn().name("order_id").varchar(128).notNull().comment("商户订单号").commit()
        .addColumn().name("product_id").varchar(128).notNull().comment("产品ID").commit()
        .addColumn().name("product_name").varchar(128).notNull().comment("产品名称").commit()
        .addColumn().name("merchant_id").varchar(128).notNull().comment("商户ID").commit()
        .addColumn().name("merchant_name").varchar(128).comment("商户名称").commit()
        .addColumn().name("amount").jdbcType(JDBCType.BIGINT).notNull().comment("交易金额(单位:分)").commit()
        .addColumn().name("currency").varchar(32).notNull().comment("币种").commit()
        .addColumn().name("create_time").datetime().notNull().comment("订单创建时间").commit()
        .addColumn().name("complete_time").datetime().comment("订单完成时间").commit()
        .addColumn().name("update_time").datetime().comment("订单修改时间").commit()
        .addColumn().name("req_json").clob().comment("订单请求原始内容").commit()
        .addColumn().name("res_json").clob().comment("订单请求响应原始内容").commit()
        .addColumn().name("is_notified").number(4).notNull().comment("支付结果是否已经通知商户").commit()
        .addColumn().name("notify_time").datetime().comment("通知时间").commit()
        .addColumn().name("status").varchar(16).notNull().comment("订单状态").commit()
        .index().name("idx_order_id").column("order_id").unique().commit()//订单号唯一索引
        .index().name("idx_order_merchant_id").column("merchant_id").commit()
        .comment("支付订单表").commit();

    database.createOrAlter("pay_bind_card")
        .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
        .addColumn().name("purpose").varchar(32).notNull().comment("绑卡用途").commit()
        .addColumn().name("channel").varchar(32).notNull().comment("绑卡渠道").commit()
        .addColumn().name("channel_name").varchar(32).notNull().comment("绑卡名称").commit()
        .addColumn().name("channel_id").varchar(32).comment("渠道ID").commit()
        .addColumn().name("merchant_id").varchar(32).comment("商户ID").commit()
        .addColumn().name("bank_code").varchar(32).notNull().comment("银行编码").commit()
        .addColumn().name("account_name").varchar(128).notNull().comment("户名").commit()
        .addColumn().name("account_no").varchar(128).notNull().comment("帐号").commit()
        .addColumn().name("id_type").varchar(32).notNull().comment("证件类型").commit()
        .addColumn().name("id_number").varchar(128).notNull().comment("证件号码").commit()
        .addColumn().name("card_type").varchar(32).notNull().comment("卡片类型").commit()
        .addColumn().name("valid_date").varchar(32).comment("信用卡有效期").commit()
        .addColumn().name("cvn2").varchar(32).comment("信用卡识别码").commit()
        .addColumn().name("authorize_code").varchar(32).comment("渠道授权码").commit()
        .addColumn().name("bind_confirm_code").varchar(32).comment("渠道绑卡确认码").commit()
        .addColumn().name("memo").varchar(1024).comment("说明").commit()
        .addColumn().name("phone_number").varchar(32).notNull().comment("手机号码").commit()
        .addColumn().name("create_time").datetime().notNull().comment("订单创建时间").commit()
        .addColumn().name("complete_time").datetime().comment("订单完成时间").commit()
        .addColumn().name("status").varchar(16).notNull().comment("订单状态").commit()
        .comment("绑卡表").commit();

    database.createOrAlter("pay_notify_log")
        .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
        .addColumn().name("channel").varchar(128).notNull().comment("支付渠道").commit()
        .addColumn().name("payment_id").varchar(128).notNull().comment("支付订单号").commit()
        .addColumn().name("trans_type").varchar(128).notNull().comment("交易类型").commit()
        .addColumn().name("notify_type").varchar(128).notNull().comment("通知方式").commit()
        .addColumn().name("order_id").varchar(128).notNull().comment("商户订单号").commit()
        .addColumn().name("product_id").varchar(128).notNull().comment("产品ID").commit()
        .addColumn().name("merchant_id").varchar(128).notNull().comment("商户ID").commit()
        .addColumn().name("order_id").varchar(128).notNull().comment("商户订单号").commit()
        .addColumn().name("amount").jdbcType(JDBCType.BIGINT).notNull().comment("交易金额(单位:分)").commit()
        .addColumn().name("currency").varchar(32).notNull().comment("币种").commit()
        .addColumn().name("retry_times").number(32).notNull().comment("重试次数").commit()
        .addColumn().name("last_notify_time").datetime().comment("最后一次重试时间").commit()
        .addColumn().name("notify_config").clob().comment("通知配置").commit()
        .addColumn().name("extra_param").clob().comment("其他配置").commit()
        .addColumn().name("is_notify_success").number(4).notNull().comment("是否已经通知成功").commit()
        .addColumn().name("complete_time").datetime().comment("支付时间").commit()
        .addColumn().name("payment_status").varchar(32).notNull().comment("订单状态").commit()
        .comment("支付通知记录表").commit();

    database.createOrAlter("pay_channel_config")
        .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
        .addColumn().name("name").varchar(128).comment("配置名称").commit()
        .addColumn().name("channel").varchar(128).notNull().comment("支付渠道").commit()
        .addColumn().name("trans_type").varchar(128).notNull().comment("支付方式").commit()
        .addColumn().name("trading_limits_json").clob().comment("限额").commit()
        .addColumn().name("status").number(4).comment("状态").commit()
        .addColumn().name("properties").clob().comment("配置内容").commit()
        .comment("渠道配置").commit();

}

//设置依赖
dependency.setup(info)
    .onInstall(install)
    .onUpgrade(function (context) { //更新时执行
        var upgrader = context.upgrader;
        upgrader.filter(versions)
            .upgrade(function (newVer) {
                newVer.upgrade(context);
            });
    })
    .onUninstall(function (context) { //卸载时执行

    });