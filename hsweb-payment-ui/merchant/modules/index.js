window.API_BASE_PATH = "http://localhost:8089/";
window.RESOURCE_PATH = "http://localhost:63342/hsweb-ui-layui/";

var lastAjax = '';

function doLogin(callback) {
    var $ = layui.jquery;
    $('.loading-wrap').hide();
    $('.login-wrap').show();
    lastAjax = callback;
}

layui.config({
    dir: 'plugins/layui/'
    , version: false
    , debug: false
    , base: 'modules/'
}).extend({
    //模块
    hsTable: 'components/hsTable',
    hsForm: 'components/hsForm',
    // 页面
    menuManage: 'pages/menuManage',
    userManage: 'pages/user/userManage'
});

layui.use(["element", "form", "request", "hsForm"], function () {
    var $ = layui.jquery;
    var element = layui.element;
    var form = layui.form;
    var r = layui.request;


    //监听测试
    layui.hsForm.init();
    form.on('submit(test1)', function (data) {
        console.log(layui.hsForm.format(data.field));

        return false;
    });


    //自定义配置项
    var AppConfig = {
        footer: false
    };

    r.get("menu/user-own/tree", function (e) {
        console.log(e);
        if (e.status === 200) {
            initTopMenu(e.result);
            element.init();
            $($("#top-menu").find("a")[0]).click();
            $('.loading-wrap').hide();
        }
    });

    //监听登录
    form.on('submit(loginForm)', function (data) {
        try {
            r.post("authorize/login", {username: data.field.username, password: data.field.password, token_type: "jwt"}, function (e) {
                if (e.status === 200) {
                    layui.sessionData("hsweb-token", {key: "accessToken", value: e.result.token});
                    //使用后清空
                    lastAjax();
                    lastAjax = '';
                    //隐藏login
                    $('.login-wrap').hide();
                } else {
                    layer.alert(e.message === '{password_error}' ? "密码错误" : e.message,{zIndex:999999999});
                }
            }, false);
        } catch (e) {
            console.log(e)
        }
        return false;
    });

    function initTopMenu(menus) {
        var topMenus = $("#top-menu");
        topMenus.children().remove();
        $(menus).each(function () {
            var parent = $("<li>");
            parent.addClass("layui-nav-item");
            var parentNode = this;
            if (this.children && this.children.length > 0) {
                var menuLink = $("<a>").attr({"href": "javascript:void(0)"}).text(parentNode.name);
                var childrenNode = this.children;
                parent.append(menuLink);
                if (this.children[0].children && this.children[0].children.length > 0 && this.children[0].children[0].children) {
                    var dl = $("<dl>").addClass("layui-nav-child");
                    var hasChildren = false;
                    $(this.children[0].children).each(function () {
//                            if(this.children&&this.children.length>0){
//                                hasChildren=true;
                        dl.append($("<dd>").append($("<a>").text(this.name)));
//                            }
                    });
//                        if(hasChildren)
                    parent.append(dl);
                } else {
                    menuLink.on("click", function () {
                        initLeftMenu(childrenNode);
                    });
                }
            }
            topMenus.append(parent);
        });

    }

    function loadMenu(menu) {
        if ($("[lay-id=" + menu.id + "]").length === 0) {
            layui.element.tabAdd('tabs', {
                title: menu.name
                , content: '<div id="tools-' + menu.id + '"></div>' +
                '<div lay-filter="' + menu.id + '" id="container-' + menu.id + '">' +
                '</div>'
                , id: menu.id
            });
            layui.use(menu.url, function () {
                var mod = layui[menu.url];
                mod.init(menu.id);
            });
        }
        layui.element.tabChange('tabs', menu.id);
    }

    function initLeftMenu(menus) {
        var leftMenu = $("#left-menu");
        leftMenu.children().remove();
        $(menus).each(function () {
            var menuEl = $("<li>");
            menuEl.addClass("layui-nav-item");
            var parentNode = this;
            var menuLink = $("<a>").text(this.name);
            menuEl.append(menuLink);
            if (parentNode.children && parentNode.children.length > 0) {
                var dl = $("<dl>");
                dl.addClass("layui-nav-child");
                $(parentNode.children).each(function () {
                    var link = $("<a>").text(this.name).attr("href", "javascript:void(0)");
                    var m = this;
                    link.on("click", function () {
                        loadMenu(m)
                    });
                    dl.append($("<dd>").append(link));

                });
                menuEl.append(dl);
            }
            leftMenu.append(menuEl);
        });
        $(leftMenu.children()[0]).addClass("layui-nav-itemed");
        layui.element.init();
    }

    $(".sign-out").on('click',function () {
        layer.confirm("确认退出本系统?",function () {
            r.get("authorize/exit",function () {
                doLogin(function () {

                });
            })
        });

    });
    //读取配置信息
    if (!AppConfig.footer) {
        $('.layui-footer').hide();
        $('.layui-body').css('bottom', '0');
    }
});

