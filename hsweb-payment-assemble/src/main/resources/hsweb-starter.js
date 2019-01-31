//组件信息
var info = {
    groupId: "org.hswebframework.pro",
    artifactId: "hsweb-payment-assemble",
    version: "1.0.0",
    website: "http://payment.hsweb.pro",
    author: "zhouhao",
    comment: "启动模块"
};
var menus = [
    {
        "id": "hsweb-pay",
        "name": "支付平台",
        "parentId": "-1",
        "permissionId": "",
        "path": "AOzB",
        "sortIndex": 1,
        "describe": " ",
        "url": "",
        "icon": "fa fa-cloud",
        "status": 1
    },
    {
        "id": "merchant",
        "name": "商户管理",
        "parentId": "hsweb-pay",
        "permissionId": "merchant-manager",
        "path": "AOzB-Aba1",
        "sortIndex": 2,
        "describe": " ",
        "url": "admin/merchant/list.html",
        "icon": "fa fa-cloud",
        "status": 1
    },
    {
        "id": "agent",
        "name": "代理管理",
        "parentId": "hsweb-pay",
        "permissionId": "agent-manager",
        "path": "AOzB-Aba1",
        "sortIndex": 3,
        "describe": " ",
        "url": "admin/merchant/list.html",
        "icon": "fa fa-cloud",
        "status": 1
    },
    {
        "id": "payment",
        "name": "订单管理",
        "parentId": "hsweb-pay",
        "permissionId": "payment",
        "path": "AOzB-Baz1",
        "sortIndex": 4,
        "describe": " ",
        "url": "admin/payment/order/list.html",
        "icon": "fa fa-cloud",
        "status": 1
    },
    {
        "id": "channel",
        "name": "渠道管理",
        "parentId": "hsweb-pay",
        "permissionId": "channel-config",
        "path": "AOzB-wqe1",
        "sortIndex": 5,
        "describe": " ",
        "url": "admin/channel/config/index.html",
        "icon": "fa fa-credit-card",
        "status": 1
    },
    {
        "id": "withdraw",
        "name": "提现管理",
        "parentId": "hsweb-pay",
        "permissionId": "merchant-withdraw",
        "path": "AOzB-wAe1",
        "sortIndex": 6,
        "describe": " ",
        "url": "admin/merchant/withdraw/list.html",
        "icon": "fa fa-credit-card",
        "status": 1
    },
    {
        "id": "channel-settle",
        "name": "渠道结算管理",
        "parentId": "hsweb-pay",
        "permissionId": "channel-settle",
        "path": "AOzB-wAe1",
        "sortIndex": 7,
        "describe": " ",
        "url": "admin/channel/settle/list.html",
        "icon": "fa fa-money",
        "status": 1
    },
    {
        "id": "channel-supplement",
        "name": "渠道结算补登",
        "parentId": "hsweb-pay",
        "permissionId": "supplement",
        "path": "AOzB-wAeA",
        "sortIndex": 8,
        "describe": " ",
        "url": "admin/channel/supplement/list.html",
        "icon": "fa fa-cc",
        "status": 1
    },
    {
        "id": "notice",
        "name": "公告管理",
        "parentId": "hsweb-pay",
        "permissionId": "notice",
        "path": "AOzB-wAeB",
        "sortIndex": 9,
        "describe": " ",
        "url": "admin/merchant/notice/list.html",
        "icon": "fa fa-cc",
        "status": 1
    },
    {
        "id": "logger",
        "name": "日志管理",
        "parentId": "-1",
        "permissionId": "logger",
        "path": "AOzA-wAeB",
        "sortIndex": 2,
        "describe": " ",
        "url": "",
        "icon": "fa fa-tasks",
        "status": 1
    },
    {
        "id": "access-logger",
        "name": "请求日志",
        "parentId": "logger",
        "permissionId": "logger",
        "path": "AOzA-wAAB",
        "sortIndex": 201,
        "describe": "",
        "url": "admin/logger/access/list.html",
        "icon": "fa fa-list",
        "status": 1
    },
    {
        "id": "system-logger",
        "name": "系统日志",
        "parentId": "logger",
        "permissionId": "logger",
        "path": "AOzA-ZAeB",
        "sortIndex": 202,
        "describe": "",
        "url": "admin/logger/system/list.html",
        "icon": "fa fa-list-ul",
        "status": 1
    },
    {
        "id": "system",
        "name": "系统设置",
        "parentId": "-1",
        "permissionId": "",
        "path": "sOrB",
        "sortIndex": 1,
        "describe": " ",
        "url": "",
        "icon": "fa fa-cogs",
        "status": 1
    },
    {
        "id": "dashboard",
        "name": "首页设置",
        "parentId": "system",
        "permissionId": "dashboard",
        "path": "sOrB-JOQv",
        "sortIndex": 101,
        "describe": null,
        "url": "admin/dashboard/list.html",
        "icon": "fa fa-dashboard",
        "status": 1
    },
    {
        "id": "menu",
        "name": "菜单管理",
        "parentId": "system",
        "permissionId": "menu",
        "path": "sOrB-i2ea",
        "sortIndex": 102,
        "describe": null,
        "url": "admin/menu/list.html",
        "icon": "fa fa-navicon",
        "status": 1
    },
    {
        "id": "permission",
        "name": "权限管理",
        "parentId": "system",
        "permissionId": "permission,autz-setting",
        "path": "sOrB-X27v",
        "sortIndex": 103,
        "describe": null,
        "url": "admin/permission/list.html",
        "icon": "fa fa-briefcase",
        "status": 1
    },
    {
        "id": "role-manager",
        "name": "角色管理",
        "parentId": "system",
        "permissionId": "role",
        "path": "sOrB-4ofL",
        "sortIndex": 104,
        "describe": null,
        "url": "admin/role/list.html",
        "icon": "fa fa-users",
        "status": 1
    },
    {
        "id": "user-manager",
        "name": "用户管理",
        "parentId": "system",
        "permissionId": "user",
        "path": "sOrB-Dz7b",
        "sortIndex": 105,
        "describe": null,
        "url": "admin/user/list.html",
        "icon": "fa fa-user",
        "status": 1
    },
    {
        "id": "org-01",
        "name": "组织架构",
        "parentId": "-1",
        "permissionId": "",
        "path": "a2o0",
        "sortIndex": 2,
        "describe": " ",
        "url": "",
        "icon": "fa fa-sitemap",
        "status": 1
    },
    {
        "id": "org-01-01",
        "name": "机构管理",
        "parentId": "org-01",
        "permissionId": "organizational",
        "path": "a2o0-iL0F",
        "sortIndex": 201,
        "describe": null,
        "url": "admin/org/list.html",
        "icon": "fa fa-leaf",
        "status": 1
    },
    {
        "id": "org-01-02",
        "name": "综合设置",
        "parentId": "org-01",
        "permissionId": "organizational,department,position,person",
        "path": "a2o0-A12e",
        "sortIndex": 202,
        "describe": null,
        "url": "admin/org/manager/index.html",
        "icon": "fa fa-sitemap",
        "status": 1
    },
    {
        "id": "dev-01",
        "name": "开发人员工具",
        "parentId": "-1",
        "permissionId": "",
        "path": "d010",
        "sortIndex": 3,
        "describe": " ",
        "url": "",
        "icon": "fa fa-th-list",
        "status": 1
    },
    {
        "id": "code-gen",
        "name": "代码生成器",
        "parentId": "dev-01",
        "permissionId": "file,database-manager,datasource",
        "path": "d010-jG1V",
        "sortIndex": 302,
        "describe": null,
        "url": "admin/code-generator/index.html",
        "icon": "fa fa-desktop",
        "status": 1
    },
    {
        "id": "database-manager",
        "name": "数据库维护",
        "parentId": "dev-01",
        "permissionId": "database-manager,datasource",
        "path": "d010-FH4w",
        "sortIndex": 305,
        "describe": null,
        "url": "admin/database-manager/index.html",
        "icon": "fa fa-database",
        "status": 1
    },
    {
        "id": "person-manager",
        "name": "人员管理",
        "parentId": "org-01",
        "permissionId": "person",
        "path": "a2o0-vfXJ",
        "sortIndex": 203,
        "describe": null,
        "url": "admin/org/person/list.html",
        "icon": "fa fa-vcard",
        "status": 1
    }
];
var autz_setting = [
    {
        "id": "admin-autz",
        "type": "user",
        "settingFor": "admin",
        "describe": null,
        "status": 1
    }
];
var user = [
    {
        "id": "admin",
        "name": "超级管理员",
        "username": "admin",
        "password": "ba7a97be0609c22fa1d300691dfcd790",
        "salt": "HX8Hr5Yd",
        "status": 1,
        "creator_id": "admin",
        "create_time": 1497160610259
    }
];
var autz_menu = [];
menus.forEach(function (menu) {
    autz_menu.push({
        id: org.hswebframework.web.id.IDGenerator.MD5.generate(),
        parentId: "-1",
        menuId: menu.id,
        status: 1,
        "settingId": "admin-autz",
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