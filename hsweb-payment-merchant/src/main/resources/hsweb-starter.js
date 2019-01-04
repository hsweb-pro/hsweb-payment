//组件信息
var info = {
    groupId: "org.hswebframework.pro",
    artifactId: "hsweb-payment-merchant",
    version: "1.0.6",
    website: "",
    author: "zhouhao",
    comment: "商户模块"
};

//版本更新信息
var versions = [
    {
        version: "1.0.1",
        upgrade: function (context) {
            var database = context.database;
            database.createOrAlter("mer_merchant")
                .addColumn().name("type").varchar(32).comment("商户类型").commit()
                .comment("商户信息表").commit();
        }
    },
    {
        version: "1.0.2",
        upgrade: function (context) {
            var database = context.database;
            database.createOrAlter("mer_charge")
                .addColumn().name("channel_charge").jdbcType(JDBCType.BIGINT).comment("渠道收费").commit()
                .addColumn().name("channel_charge_memo").varchar(2048).comment("渠道收费说明").commit()
                .comment("收费记录").commit();
        }
    },
    {
        version: "1.0.3",
        upgrade: function (context) {
            var database = context.database;
            database.createOrAlter("mer_agent")
                .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
                .addColumn().name("name").varchar(128).notNull().comment("商户名称").commit()
                .addColumn().name("account_no").varchar(32).notNull().comment("资金账户号").commit()
                .addColumn().name("parent_id").varchar(32).comment("上级代理ID").commit()
                .addColumn().name("phone").varchar(32).comment("联系电话").commit()
                .addColumn().name("user_id").varchar(32).comment("管理用户ID").commit()
                .addColumn().name("username").varchar(32).comment("管理用户名").commit()
                .addColumn().name("create_time").datetime().notNull().comment("创建时间").commit()
                .addColumn().name("status").varchar(32).notNull().comment("状态").commit()
                .addColumn().name("qq").varchar(32).comment("qq").commit()
                .addColumn().name("we_chat").varchar(32).comment("微信").commit()
                .addColumn().name("email").varchar(32).comment("邮箱").commit()
                .index().name("idx_mer_agent_parent_id").column("parent_id").commit()
                .comment("代理信息表").commit();
        }
    },
    {
        version: "1.0.4",
        upgrade: function (context) {
            var database = context.database;
            database.createOrAlter("sys_notice")
                .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
                .addColumn().name("create_time").datetime().notNull().comment("创建时间").commit()
                .addColumn().name("update_time").datetime().comment("更新时间").commit()
                .addColumn().name("create_user").varchar(64).notNull().comment("创建人").commit()
                .addColumn().name("update_user").varchar(64).comment("更新人").commit()
                .addColumn().name("content").clob().notNull().comment("公告内容").commit()
                .addColumn().name("title").varchar(200).comment("公告标题").commit()
                .addColumn().name("types").varchar(500).comment("公告类型").commit()
                .addColumn().name("status").varchar(100).comment("公告状态").commit()
                .comment("系统公告").commit();
        }
    },
    {
        version: "1.0.5",
        upgrade: function (context) {
            var database = context.database;
            database.createOrAlter("mer_withdraw")
                .addColumn().name("payee_info_json").clob().comment("收款人信息").commit()
                .addColumn().name("payee_type").varchar(32).notNull().comment("收款人类型").commit()
                .comment("商户提现表").commit();

            database.createOrAlter("mer_payee_info")
                .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
                .addColumn().name("merchant_id").varchar(32).notNull().comment("商户ID").commit()
                .addColumn().name("payee_info_json").clob().comment("收款人信息").commit()
                .addColumn().name("payee_type").varchar(32).notNull().comment("收款人类型").commit()
                .addColumn().name("payee").varchar(128).notNull().comment("收款人帐号").commit()
                .addColumn().name("payee_name").varchar(256).notNull().comment("收款人姓名").commit()
                .addColumn().name("is_default_withdraw").number(4).comment("默认提现账户").commit()
                .addColumn().name("comment").varchar(128).notNull().comment("收款人类型").commit()
                .addColumn().name("create_time").datetime().comment("创建时间").commit()
                .index().name("idx_payee_merchant_id").column("merchant_id").commit()
                .comment("商户收费人表").commit();

            //代付信息表
            database.createOrAlter("mer_substitute")
                .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
                .addColumn().name("merchant_id").varchar(32).notNull().comment("商户ID").commit()
                .addColumn().name("merchant_name").varchar(64).notNull().comment("商户名称").commit()
                .addColumn().name("tans_no").varchar(32).notNull().comment("商户交易流水号").commit()
                .addColumn().name("payment_id").varchar(32).notNull().comment("支付订单ID").commit()
                .addColumn().name("payee_type").varchar(32).notNull().comment("收款人类型").commit()
                .addColumn().name("status").varchar(32).notNull().comment("状态").commit()
                .addColumn().name("complete_time").datetime().comment("完成时间").commit()
                .addColumn().name("create_time").datetime().comment("创建时间").commit()
                .addColumn().name("notify_url").varchar(2048).comment("通知地址").commit()
                .addColumn().name("remark").varchar(2048).comment("备注").commit()
                .addColumn().name("total").jdbcType(JDBCType.BIGINT).notNull().comment("总笔数").commit()
                .addColumn().name("total_success").jdbcType(JDBCType.BIGINT).notNull().comment("成功笔数").commit()
                .addColumn().name("charge").jdbcType(JDBCType.BIGINT).notNull().comment("收费(单位:分)").commit()
                .addColumn().name("real_charge").jdbcType(JDBCType.BIGINT).notNull().comment("实际收费(单位:分)").commit()
                .addColumn().name("total_amount").jdbcType(JDBCType.BIGINT).notNull().comment("总金额(单位:分)").commit()
                .addColumn().name("real_amount").jdbcType(JDBCType.BIGINT).notNull().comment("实际交易金额(单位:分)").commit()
                .index().name("idx_substitute_merchant_id").column("merchant_id").commit()
                .index().name("idx_substitute_payment_id").column("payment_id").commit()
                .index().name("idx_substitute_tans_no").column("tans_no").unique().commit()
                .comment("代付信息表").commit();

            //代付明细表
            database.createOrAlter("mer_substitute_detail")
                .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
                .addColumn().name("substitute_id").varchar(32).notNull().comment("代付ID").commit()
                .addColumn().name("merchant_id").varchar(32).notNull().comment("商户ID").commit()
                .addColumn().name("merchant_name").varchar(128).notNull().comment("商户名称").commit()
                .addColumn().name("tans_no").varchar(32).notNull().comment("商户交易流水号").commit()
                .addColumn().name("payment_id").varchar(32).notNull().comment("支付订单ID").commit()
                .addColumn().name("payee").varchar(128).notNull().comment("收款人").commit()
                .addColumn().name("payee_name").varchar(128).notNull().comment("收款人姓名").commit()
                .addColumn().name("payee_info_json").clob().notNull().comment("收款人信息").commit()
                .addColumn().name("status").varchar(32).notNull().comment("状态").commit()
                .addColumn().name("remark").varchar(2048).comment("备注").commit()
                .addColumn().name("amount").jdbcType(JDBCType.BIGINT).notNull().comment("代付金额").commit()
                .addColumn().name("charge_amount").jdbcType(JDBCType.BIGINT).notNull().comment("收费(单位:分)").commit()
                .addColumn().name("charge_memo").varchar(2048).comment("收费说明").commit()
                .index().name("idx_substitute_d_payment_id").column("payment_id").commit()
                .index().name("idx_substitute_d_substitute_id").column("substitute_id").commit()
                .index().name("idx_substitute_d_merchant_id").column("merchant_id").commit()
                .index().name("idx_substitute_d_tans_no").column("tans_no").unique().commit()
                .comment("代付信息表").commit();

        }
    },
    {
        version: "1.0.6",
        upgrade: function (context) {
            var database = context.database;
            database.createOrAlter("mer_payee_info")
                .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
                .addColumn().name("merchant_id").varchar(32).notNull().comment("商户ID").commit()
                .addColumn().name("payee_info_json").clob().comment("收款人信息").commit()
                .addColumn().name("payee_type").varchar(32).notNull().comment("收款人类型").commit()
                .addColumn().name("payee").varchar(128).notNull().comment("收款人帐号").commit()
                .addColumn().name("payee_name").varchar(256).notNull().comment("收款人姓名").commit()
                .addColumn().name("is_default_withdraw").number(4).comment("默认提现账户").commit()
                .addColumn().name("comment").varchar(128).comment("备注信息").commit()
                .addColumn().name("create_time").datetime().comment("创建时间").commit()
                .index().name("idx_payee_merchant_id").column("merchant_id").commit()
                .comment("商户收费人表").commit();
        }
    }
];
var JDBCType = java.sql.JDBCType;

function install(context) {
    var database = context.database;
    database.createOrAlter("mer_merchant")
        .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
        .addColumn().name("name").varchar(128).notNull().comment("商户名称").commit()
        .addColumn().name("account_no").varchar(32).notNull().comment("资金账户号").commit()
        .addColumn().name("agent_id").varchar(32).comment("代理ID").commit()
        .addColumn().name("phone").varchar(32).comment("联系电话").commit()
        .addColumn().name("user_id").varchar(32).comment("管理用户ID").commit()
        .addColumn().name("username").varchar(32).comment("管理用户名").commit()
        .addColumn().name("create_time").datetime().notNull().comment("创建时间").commit()
        .addColumn().name("status").varchar(32).notNull().comment("状态").commit()

        .addColumn().name("product_name").varchar(64).comment("商户产品名称").commit()
        .addColumn().name("legal_person_name").varchar(32).comment("法人姓名").commit()
        .addColumn().name("legal_person_id_card").varchar(32).comment("法人身份证号").commit()
        .addColumn().name("company_address").varchar(128).comment("公司地址").commit()
        .addColumn().name("company_name").varchar(128).comment("公司名称").commit()
        .addColumn().name("id_card_front").varchar(500).comment("法人身份证正面").commit()
        .addColumn().name("id_card_back").varchar(500).comment("法人身份证反面").commit()
        .addColumn().name("business_license").varchar(500).comment("business_license").commit()
        .addColumn().name("qq").varchar(32).comment("qq").commit()
        .addColumn().name("we_chat").varchar(32).comment("微信").commit()
        .addColumn().name("email").varchar(32).comment("邮箱").commit()
        .index().name("idx_mer_agent_id").column("agent_id").commit()
        .comment("商户信息表").commit();


    database.createOrAlter("mer_config")
        .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
        .addColumn().name("merchant_id").varchar(128).notNull().comment("商户ID").commit()
        .addColumn().name("key").varchar(128).notNull().comment("配置KEY").commit()
        .addColumn().name("value").clob().comment("配置内容").commit()
        .addColumn().name("is_mer_writable").number(4).notNull().comment("商户是否可以修改此配置").commit()
        .index().name("idx_mer_conf_merchant_id").column("merchant_id").commit()
        .comment("商户配置表").commit();

    database.createOrAlter("mer_charge")
        .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
        .addColumn().name("merchant_id").varchar(32).notNull().comment("商户Id").commit()
        .addColumn().name("payment_id").varchar(32).notNull().comment("支付订单ID").commit()
        .addColumn().name("tans_type").varchar(32).notNull().comment("交易类型").commit()
        .addColumn().name("is_settle").number(4).notNull().comment("是否已结算").commit()
        .addColumn().name("is_calculated").number(4).notNull().comment("是否已经计算收费").commit()
        .addColumn().name("settle_time").datetime().comment("结算时间").commit()
        .addColumn().name("pay_time").datetime().comment("支付时间").commit()
        .addColumn().name("amount").jdbcType(JDBCType.BIGINT).comment("支付金额").commit()
        .addColumn().name("charge").jdbcType(JDBCType.BIGINT).comment("平台收费").commit()
        .addColumn().name("agent_charge").jdbcType(JDBCType.BIGINT).comment("代理收费").commit()
        .addColumn().name("memo").varchar(1024).comment("备注").commit()
        .addColumn().name("charge_memo").varchar(2048).comment("收费备注").commit()
        .addColumn().name("agent_charge_memo").varchar(2048).comment("收费备注").commit()
        .index().name("idx_charge_payment_id").column("payment_id").commit()
        .index().name("idx_charge_merchant_id").column("merchant_id").commit()
        .comment("商户收费表").commit();

    database.createOrAlter("mer_withdraw")
        .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
        .addColumn().name("payment_id").varchar(32).comment("支付订单ID").commit()
        .addColumn().name("merchant_id").varchar(32).notNull().comment("商户ID").commit()
        .addColumn().name("trans_amount").jdbcType(JDBCType.BIGINT).comment("提现金额").commit()
        .addColumn().name("withdraw_type").varchar(32).notNull().comment("提现类型").commit()
        .addColumn().name("charge_amount").varchar(32).comment("手续费").commit()
        .addColumn().name("comment").varchar(500).comment("备注").commit()
        .addColumn().name("status").varchar(32).comment("提现状态").commit()
        .addColumn().name("apply_time").datetime().comment("申请时间").commit()
        .addColumn().name("handle_time").datetime().comment("处理时间").commit()
        .addColumn().name("complete_time").datetime().comment("完成时间").commit()
        .addColumn().name("close_time").datetime().comment("完成时间").commit()
        .addColumn().name("handle_user").varchar(64).comment("处理人").commit()
        .addColumn().name("complete_prove").varchar(2048).comment("提现完成证明").commit()
        .addColumn().name("merchant_name").varchar(64).comment("商户名称").commit()
        .index().name("idx_mer_withdraw_merchant_id").column("merchant_id").commit()
        .index().name("idx_mer_withdraw_payment_id").column("payment_id").commit()
        .comment("商户提现表").commit();
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