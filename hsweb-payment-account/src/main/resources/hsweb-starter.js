//组件信息
var info = {
    groupId: "org.hswebframework.pro",
    artifactId: "hsweb-payment-account",
    version: "1.0.4",
    website: "hsweb.pro",
    author: "Lind",
    comment: "账户模块"
};

//版本更新信息
var versions = [
    {
        version: "1.0.2",
        upgrade: function (context) {
            var database = context.database;
            //增加冻结金额字段
            database.createOrAlter("acc_account")
                .addColumn().name("freeze_balance").jdbcType(JDBCType.BIGINT).comment("冻结金额").commit()
                .comment("资金账户").commit();

            database.createOrAlter("acc_freeze_log")
                .addColumn().name("id").varchar(32).notNull().comment("ID").commit()
                .addColumn().name("payment_id").varchar(32).notNull().comment("订单ID").commit()
                .addColumn().name("amount").varchar(32).notNull().comment("冻结/解冻金额").commit()
                .addColumn().name("account_no").varchar(32).notNull().comment("资金账户号").commit()
                .addColumn().name("direction").varchar(32).notNull().comment("冻结解冻方向").commit()
                .addColumn().name("freeze_time").datetime().notNull().comment("冻结时间").commit()
                .addColumn().name("unfreeze_time").datetime().comment("解冻时间").commit()
                .addColumn().name("comment").varchar(32).comment("备注").commit()
                .index().name("idx_freeze_log_account_no").column("account_no").commit()
                .index().name("idx_freeze_log_payment_id").column("payment_id").commit()
                .comment("资金账户冻结记录").commit();
        }
    },
    {
        version: "1.0.3",
        upgrade: function (context) {
            var database = context.database;
            //增加冻结记录字段
            database.createOrAlter("acc_freeze_log")
                .addColumn().name("trans_type").varchar(32).notNull().comment("交易类型").commit()
                .addColumn().name("merchant_id").varchar(32).notNull().comment("商户ID").commit()
                .addColumn().name("account_name").varchar(32).notNull().comment("资金账户名称").commit()
                .comment("资金账户冻结记录").commit();
        }
    },
    {
        version: "1.0.4",
        upgrade: function (context) {
            var database = context.database;
            //增加冻结记录字段
            database.createOrAlter("acc_freeze_log")
                .addColumn().name("unfreeze_comment").varchar(500).comment("解冻原因").commit()
                .comment("资金账户冻结记录").commit();
        }
    },

];
var JDBCType = java.sql.JDBCType;

function install(context) {
    var database = context.database;
    database.createOrAlter("acc_account")
        .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
        .addColumn().name("create_time").datetime().notNull().comment("创建时间").commit()
        .addColumn().name("update_time").datetime().notNull().comment("更新时间").commit()
        .addColumn().name("create_user").varchar(32).notNull().comment("创建人").commit()
        .addColumn().name("update_user").varchar(32).notNull().comment("创建人").commit()
        .addColumn().name("name").varchar(128).notNull().comment("账户名称").commit()
        .addColumn().name("account_no").varchar(32).notNull().comment("资金账户号").commit()
        .addColumn().name("merchant_id").varchar(32).notNull().comment("商户ID").commit()
        .addColumn().name("type").varchar(32).comment("资金账户类型（NORMAL标准、CREDIT信用、DEPOSIT保证金）").commit()
        .addColumn().name("status").varchar(32).comment("资金账户状态（ACTIVE激活、FREEZE冻结）").commit()
        .addColumn().name("balance").jdbcType(JDBCType.BIGINT).comment("账户余额").commit()
        .addColumn().name("currency").varchar(32).comment("资金账户币种").commit()
        .addColumn().name("comment").varchar(500).comment("备注").commit()
        .index().name("idx_acc_account_no")
        .column("account_no").commit()//account_no索引
        .comment("资金账户").commit();


    database.createOrAlter("acc_trans_log")
        .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
        .addColumn().name("create_time").datetime().notNull().comment("创建时间").commit()
        .addColumn().name("create_user").varchar(32).notNull().comment("创建人").commit()
        .addColumn().name("account_no").varchar(128).notNull().comment("资金账户号").commit()
        .addColumn().name("account_name").varchar(128).notNull().comment("资金账户名").commit()
        .addColumn().name("merchant_id").varchar(32).notNull().comment("商户ID").commit()
        .addColumn().name("payment_id").varchar(32).notNull().comment("订单ID").commit()
        .addColumn().name("currency").varchar(32).notNull().comment("交易币种").commit()
        .addColumn().name("trans_type").varchar(128).notNull().comment("交易类型").commit()
        .addColumn().name("account_trans_type").varchar(32).notNull().comment("资金账户交易类型").commit()
        .addColumn().name("trans_amount").jdbcType(JDBCType.BIGINT).comment("交易金额").commit()
        .addColumn().name("balance").jdbcType(JDBCType.BIGINT).comment("交易后账户余额").commit()
        .addColumn().name("status").varchar(32).comment("交易状态").commit()
        .addColumn().name("comment").varchar(500).comment("备注").commit()
        .index().name("idx_trans_log_tt_acc_pmt")
        .column("payment_id").column("trans_type").column("account_no")
        .unique().commit()//唯一索引
        .index().name("idx_trans_log_merchant_id").column("merchant_id").commit()
        .comment("资金账户交易流水").commit();


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