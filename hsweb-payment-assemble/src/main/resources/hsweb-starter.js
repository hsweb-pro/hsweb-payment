//组件信息
var info = {
    groupId: "org.hswebframework.pro",
    artifactId: "hsweb-payment-assemble",
    version: "1.0.0",
    website: "",
    author: "zhouhao",
    comment: ""
};
var menus = [
    {
        "u_id": "hsweb-pay",
        "name": "支付平台",
        "parent_id": "-1",
        "permission_id": "",
        "path": "AOzB",
        "sort_index": 1,
        "describe": " ",
        "url": "",
        "icon": "fa fa-cloud",
        "status": 1
    },
    {
        "u_id": "merchant",
        "name": "商户管理",
        "parent_id": "hsweb-pay",
        "permission_id": "merchant-manager",
        "path": "AOzB-Aba1",
        "sort_index": 2,
        "describe": " ",
        "url": "admin/merchant/list.html",
        "icon": "fa fa-cloud",
        "status": 1
    },
    {
        "u_id": "agent",
        "name": "代理管理",
        "parent_id": "hsweb-pay",
        "permission_id": "agent-manager",
        "path": "AOzB-Aba1",
        "sort_index": 3,
        "describe": " ",
        "url": "admin/merchant/list.html",
        "icon": "fa fa-cloud",
        "status": 1
    },
    {
        "u_id": "payment",
        "name": "订单管理",
        "parent_id": "hsweb-pay",
        "permission_id": "payment",
        "path": "AOzB-Baz1",
        "sort_index": 4,
        "describe": " ",
        "url": "admin/payment/order/list.html",
        "icon": "fa fa-cloud",
        "status": 1
    },
    {
        "u_id": "channel",
        "name": "渠道管理",
        "parent_id": "hsweb-pay",
        "permission_id": "channel-config",
        "path": "AOzB-wqe1",
        "sort_index": 5,
        "describe": " ",
        "url": "admin/channel/config/index.html",
        "icon": "fa fa-credit-card",
        "status": 1
    },
    {
        "u_id": "withdraw",
        "name": "提现管理",
        "parent_id": "hsweb-pay",
        "permission_id": "merchant-withdraw",
        "path": "AOzB-wAe1",
        "sort_index": 6,
        "describe": " ",
        "url": "admin/merchant/withdraw/list.html",
        "icon": "fa fa-credit-card",
        "status": 1
    },
    {
        "u_id": "channel-settle",
        "name": "渠道结算管理",
        "parent_id": "hsweb-pay",
        "permission_id": "channel-settle",
        "path": "AOzB-wAe1",
        "sort_index": 7,
        "describe": " ",
        "url": "admin/channel/settle/list.html",
        "icon": "fa fa-money",
        "status": 1
    },
    {
        "u_id": "channel-supplement",
        "name": "渠道结算补登",
        "parent_id": "hsweb-pay",
        "permission_id": "supplement",
        "path": "AOzB-wAeA",
        "sort_index": 8,
        "describe": " ",
        "url": "admin/channel/supplement/list.html",
        "icon": "fa fa-cc",
        "status": 1
    },
    {
        "u_id": "notice",
        "name": "公告管理",
        "parent_id": "hsweb-pay",
        "permission_id": "notice",
        "path": "AOzB-wAeB",
        "sort_index": 9,
        "describe": " ",
        "url": "admin/merchant/notice/list.html",
        "icon": "fa fa-cc",
        "status": 1
    },
    {
        "u_id": "logger",
        "name": "日志管理",
        "parent_id": "-1",
        "permission_id": "logger",
        "path": "AOzA-wAeB",
        "sort_index": 2,
        "describe": " ",
        "url": "",
        "icon": "fa fa-tasks",
        "status": 1
    },
    {
        "u_id": "access-logger",
        "name": "请求日志",
        "parent_id": "logger",
        "permission_id": "logger",
        "path": "AOzA-wAAB",
        "sort_index": 201,
        "describe": "",
        "url": "admin/logger/access/list.html",
        "icon": "fa fa-list",
        "status": 1
    },
    {
        "u_id": "system-logger",
        "name": "系统日志",
        "parent_id": "logger",
        "permission_id": "logger",
        "path": "AOzA-ZAeB",
        "sort_index": 202,
        "describe": "",
        "url": "admin/logger/system/list.html",
        "icon": "fa fa-list-ul",
        "status": 1
    },
    {
        "u_id": "system",
        "name": "系统设置",
        "parent_id": "-1",
        "permission_id": "",
        "path": "sOrB",
        "sort_index": 1,
        "describe": " ",
        "url": "",
        "icon": "fa fa-cogs",
        "status": 1
    },
    {
        "u_id": "dashboard",
        "name": "首页设置",
        "parent_id": "system",
        "permission_id": "dashboard",
        "path": "sOrB-JOQv",
        "sort_index": 101,
        "describe": null,
        "url": "admin/dashboard/list.html",
        "icon": "fa fa-dashboard",
        "status": 1
    },
    {
        "u_id": "menu",
        "name": "菜单管理",
        "parent_id": "system",
        "permission_id": "menu",
        "path": "sOrB-i2ea",
        "sort_index": 102,
        "describe": null,
        "url": "admin/menu/list.html",
        "icon": "fa fa-navicon",
        "status": 1
    },
    {
        "u_id": "permission",
        "name": "权限管理",
        "parent_id": "system",
        "permission_id": "permission,autz-setting",
        "path": "sOrB-X27v",
        "sort_index": 103,
        "describe": null,
        "url": "admin/permission/list.html",
        "icon": "fa fa-briefcase",
        "status": 1
    },
    {
        "u_id": "role-manager",
        "name": "角色管理",
        "parent_id": "system",
        "permission_id": "role",
        "path": "sOrB-4ofL",
        "sort_index": 104,
        "describe": null,
        "url": "admin/role/list.html",
        "icon": "fa fa-users",
        "status": 1
    },
    {
        "u_id": "user-manager",
        "name": "用户管理",
        "parent_id": "system",
        "permission_id": "user",
        "path": "sOrB-Dz7b",
        "sort_index": 105,
        "describe": null,
        "url": "admin/user/list.html",
        "icon": "fa fa-user",
        "status": 1
    },
    {
        "u_id": "org-01",
        "name": "组织架构",
        "parent_id": "-1",
        "permission_id": "",
        "path": "a2o0",
        "sort_index": 2,
        "describe": " ",
        "url": "",
        "icon": "fa fa-sitemap",
        "status": 1
    },
    {
        "u_id": "org-01-01",
        "name": "机构管理",
        "parent_id": "org-01",
        "permission_id": "organizational",
        "path": "a2o0-iL0F",
        "sort_index": 201,
        "describe": null,
        "url": "admin/org/list.html",
        "icon": "fa fa-leaf",
        "status": 1
    },
    {
        "u_id": "org-01-02",
        "name": "综合设置",
        "parent_id": "org-01",
        "permission_id": "organizational,department,position,person",
        "path": "a2o0-A12e",
        "sort_index": 202,
        "describe": null,
        "url": "admin/org/manager/index.html",
        "icon": "fa fa-sitemap",
        "status": 1
    },
    {
        "u_id": "dev-01",
        "name": "开发人员工具",
        "parent_id": "-1",
        "permission_id": "",
        "path": "d010",
        "sort_index": 3,
        "describe": " ",
        "url": "",
        "icon": "fa fa-th-list",
        "status": 1
    },
    // {
    //     "u_id": "datasource-manager",
    //     "name": "数据源管理",
    //     "parent_id": "dev-01",
    //     "permission_id": "datasource",
    //     "path": "d010-jG1A",
    //     "sort_index": 301,
    //     "describe": null,
    //     "url": "admin/datasource/list.html",
    //     "icon": "fa fa-database",
    //     "status": 1
    // },
    {
        "u_id": "code-gen",
        "name": "代码生成器",
        "parent_id": "dev-01",
        "permission_id": "file,database-manager,datasource",
        "path": "d010-jG1V",
        "sort_index": 302,
        "describe": null,
        "url": "admin/code-generator/index.html",
        "icon": "fa fa-desktop",
        "status": 1
    },
    // {
    //     "u_id": "dyn-form",
    //     "name": "动态表单",
    //     "parent_id": "dev-01",
    //     "permission_id": "form",
    //     "path": "d010-6tVw",
    //     "sort_index": 303,
    //     "describe": null,
    //     "url": "admin/form/list.html",
    //     "icon": "fa fa-puzzle-piece",
    //     "status": 1
    // },
    // {
    //     "u_id": "template",
    //     "name": "模板管理",
    //     "parent_id": "dev-01",
    //     "permission_id": "template",
    //     "path": "d010-54Ph",
    //     "sort_index": 304,
    //     "describe": null,
    //     "url": "pages/template/list.hl",
    //     "icon": "fa fa-send-o",
    //     "status": 1
    // },
    {
        "u_id": "database-manager",
        "name": "数据库维护",
        "parent_id": "dev-01",
        "permission_id": "database-manager,datasource",
        "path": "d010-FH4w",
        "sort_index": 305,
        "describe": null,
        "url": "admin/database-manager/index.html",
        "icon": "fa fa-database",
        "status": 1
    },
    // {
    //     "u_id": "quartz",
    //     "name": "定时任务",
    //     "parent_id": "dev-01",
    //     "permission_id": "schedule",
    //     "path": "d010-V54b",
    //     "sort_index": 306,
    //     "describe": null,
    //     "url": "admin/schedule/index.html",
    //     "icon": "fa fa-file-word-o",
    //     "status": 1
    // },
    {
        "u_id": "person-manager",
        "name": "人员管理",
        "parent_id": "org-01",
        "permission_id": "person",
        "path": "a2o0-vfXJ",
        "sort_index": 203,
        "describe": null,
        "url": "admin/org/person/list.html",
        "icon": "fa fa-vcard",
        "status": 1
    }
];
var autz_setting = [
    {
        "u_id": "admin-autz",
        "type": "user",
        "setting_for": "admin",
        "describe": null,
        "status": 1
    }
];
var user = [
    {
        "u_id": "admin",
        "name": "超级管理员",
        "username": "admin",
        "password": "ba7a97be0609c22fa1d300691dfcd790",
        "salt": "HX8Hr5Yd",
        "status": 1,
        "last_login_ip": null,
        "last_login_time": null,
        "creator_id": "admin",
        "create_time": 1497160610259
    }
];
var autz_menu = [];
menus.forEach(function (menu) {
    autz_menu.push({
        u_id: org.hswebframework.web.id.IDGenerator.MD5.generate(),
        parent_id: "-1",
        menu_id: menu.u_id,
        status: 1,
        "setting_id": "admin-autz",
        "path": "-"
    });
});
//版本更新信息
var versions = [
    // {
    //     version: "3.0.0",
    //     upgrade: function (context) {
    //         java.lang.System.out.println("更新到3.0.2了");
    //     }
    // }
];
var JDBCType = java.sql.JDBCType;

function install(context) {
    var database = context.database;

}

function initialize(context) {
    var database = context.database;

    database.getTable("s_menu").createInsert().values(menus).exec();
    database.getTable("s_autz_setting").createInsert().values(autz_setting).exec();
    database.getTable("s_autz_menu").createInsert().values(autz_menu).exec();
    database.getTable("s_user").createInsert().values(user).exec();
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

    }).onInitialize(initialize);