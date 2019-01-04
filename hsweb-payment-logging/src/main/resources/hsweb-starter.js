//组件信息
var info = {
    groupId: "org.hswebframework.pro",
    artifactId: "hsweb-payment-logging",
    version: "1.0.0",
    website: "hsweb.pro",
    author: "zhouhao",
    comment: "日志模块"
};

//版本更新信息
var versions = [];
var JDBCType = java.sql.JDBCType;

function install(context) {
    var database = context.database;
    database.createOrAlter("log_access_logger")
        .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
        .addColumn().name("request_id").varchar(32).notNull().comment("请求ID").commit()
        .addColumn().name("session_id").varchar(32).notNull().comment("会话ID").commit()
        .addColumn().name("url").varchar(1024).notNull().comment("URL").commit()
        .addColumn().name("ip_address").varchar(256).notNull().comment("IP").commit()
        .addColumn().name("ip_location").varchar(256).comment("IP").commit()
        .addColumn().name("user_id").varchar(32).notNull().comment("userId").commit()
        .addColumn().name("user_name").varchar(128).notNull().comment("用户名").commit()
        .addColumn().name("action").varchar(32).notNull().comment("操作").commit()
        .addColumn().name("http_method").varchar(128).notNull().comment("httpMethod").commit()
        .addColumn().name("class_name").varchar(128).notNull().comment("类名").commit()
        .addColumn().name("method_name").varchar(32).notNull().comment("方法名").commit()
        .addColumn().name("request_time").datetime().notNull().comment("请求时间").commit()
        .addColumn().name("response_time").datetime().notNull().comment("响应时间").commit()
        .addColumn().name("parameters").clob().comment("请求参数").commit()
        .addColumn().name("error_stack").clob().comment("错误栈信息").commit()
        .addColumn().name("http_header").clob().comment("请求头").commit()
        .addColumn().name("use_time").number(16).notNull().comment("请求耗时").commit()
        .index().name("idx_acc_log_request_id").column("request_id").commit()
        .index().name("idx_acc_log_user_id").column("user_id").commit()
        .index().name("idx_acc_log_action").column("action").commit()
        .comment("访问日志表").commit();

    database.createOrAlter("log_sys_logger")
        .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
        .addColumn().name("name").varchar(128).notNull().comment("日志名").commit()
        .addColumn().name("thread_name").varchar(128).notNull().comment("请求ID").commit()
        .addColumn().name("class_name").varchar(128).notNull().comment("类名").commit()
        .addColumn().name("method_name").varchar(64).notNull().comment("方法名").commit()
        .addColumn().name("line_number").number(16).notNull().comment("行号").commit()
        .addColumn().name("session_id").varchar(32).notNull().comment("会话ID").commit()
        .addColumn().name("request_id").varchar(32).notNull().comment("请求ID").commit()
        .addColumn().name("business_id").varchar(32).notNull().comment("业务标识").commit()
        .addColumn().name("user_id").varchar(32).notNull().comment("userId").commit()
        .addColumn().name("user_name").varchar(128).notNull().comment("用户名").commit()
        .addColumn().name("level").varchar(32).notNull().comment("日志级别").commit()
        .addColumn().name("git_hash").varchar(128).notNull().comment("gitHash").commit()
        .addColumn().name("create_time").datetime().notNull().comment("创建时间").commit()
        .addColumn().name("message").clob().comment("日志内容").commit()
        .addColumn().name("stack_info").clob().comment("错误栈信息").commit()
        .addColumn().name("context_json").clob().comment("日志上下文").commit()
        .index().name("idx_sys_log_request_id").column("request_id").commit()
        .index().name("idx_sys_log_user_id").column("user_id").commit()
        .comment("系统日志表").commit();

    database.createOrAlter("log_user_operation")
        .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
        .addColumn().name("user_id").varchar(32).notNull().comment("userId").commit()
        .addColumn().name("name").varchar(128).notNull().comment("用户名").commit()
        .addColumn().name("session_id").varchar(32).notNull().comment("会话ID").commit()
        .addColumn().name("type").varchar(32).notNull().comment("操作类型").commit()
        .addColumn().name("ip_address").varchar(128).notNull().comment("ip地址").commit()
        .addColumn().name("ip_location").varchar(256).comment("IP").commit()
        .addColumn().name("request_time").datetime().notNull().comment("操作时间").commit()
        .index().name("idx_user_operate_session_id").column("session_id").commit()
        .index().name("idx_user_operate_user_id").column("user_id").commit()
        .index().name("idx_user_operate_user_id_type").column("user_id").column("type").commit()
        .comment("系统日志表").commit();
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