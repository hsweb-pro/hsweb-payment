importLayui(function () {
    require(["css!plugin/css/custom"]);
    var lastAjax = [];
    window.doLogin = function (callback) {
        // $('.loading-wrap').hide();
        // $('.login-wrap').show();
        // lastAjax.push(callback);
        window.location.href = "/login.html";
    };
    require(["hsForm"], function (hsForm) {
        hsForm.init();
    });
    var userType = "merchant";

    require(["request"], function (r) {
        r.get("authorize/me", function (e) {
            if (e.status === 200) {
                $(".login-name").html(e.result.user.name);
                userType = e.result.attributes.userType;
                if (userType === 'merchant') {
                    r.get("current-merchant/status", function (e) {
                        if (e.status === 200) {
                            window.infoStatus = e.result;
                            loadUserTree();
                        }
                    });
                } else {
                    window.infoStatus = "ACTIVE";
                    loadUserTree();
                }
            }
        });

    });
    var $ = layui.jquery;
    var element = layui.element;
    var form = layui.form;

    //自定义配置项
    var AppConfig = {
        footer: false
    };


    function loadUserTree() {
        layui.element.init();
        if (userType === "merchant") {
            if (window.infoStatus === "ACTIVE") {
                initLeftMenu(menuList);
                loadMenu({
                    id: "dashboard",
                    name: "首页",
                    url: "pages/dashboard/list"
                });
            } else if (window.infoStatus === "PENDING_REVIEW") {
                initLeftMenu(personMenu);
                loadMenu({
                    id: "base",
                    name: "备案信息",
                    url: "pages/person-info/base/list"
                });
            }
        } else if (userType === "agent") {
            //代理商户
            initLeftMenu(agentMenu);
            loadMenu({
                id: "dashboard",
                name: "首页",
                url: "pages/agent/dashboard/list"
            });
        }

        $($("#top-menu").find("a")[0]).click();
        $('.loading-wrap').hide();
        $(".main").fadeIn(200)

    }

    //监听登录
    form.on('submit(loginForm)', function (data) {
        require(["request"], function (r) {
            try {
                r.post("authorize/login", {
                    username: data.field.username,
                    verifyCode: data.field.verifyCode,
                    password: data.field.password
                }, function (e) {
                    if (e.status === 200) {
                        window.location.reload();

                        // layui.sessionData("hsweb-token", {key: "accessToken", value: e.result.token});
                        //使用后清空
                        $(lastAjax).each(function () {
                            this();
                        });
                        lastAjax = [];
                        //隐藏login
                        $('.login-wrap').hide();
                        // loadUserTree();
                    } else {
                        reloadVerifyCode();
                        layer.alert(e.message === '{password_error}' ? "密码错误" : e.message, {zIndex: 999999999});
                    }

                }, false);
            } catch (e) {
                console.log(e)
            }
        });

        return false;
    });

    function loadMenu(menu) {
        if ($("[lay-id=" + menu.id + "]").length === 0) {
            layui.element.tabAdd('tabs', {
                title: menu.name
                , content: '<div id="tools-' + menu.id + '"></div>' +
                    '<div lay-filter="' + menu.id + '" id="container-' + menu.id + '">' +
                    '</div>'
                , id: menu.id
            });
            require([menu.url], function (page) {
                page.init(menu.id);
            });
        }
        layui.element.tabChange('tabs', menu.id);
    }

    $(".sign-out").on('click', function () {
        layer.confirm("确认退出本系统?", {btn: ['退出', '取消']}, function () {
            layer.closeAll();
            require(["request"], function (r) {
                r.get("authorize/exit", function () {
                    layui.sessionData("hsweb-token", {key: "accessToken", value: null});
                    doLogin(function () {
                    });
                    window.location.href = "/login.html";
                })
            });
            return true;
        });

    });

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
            } else {
                menuLink.on("click", function () {
                    loadMenu(parentNode)
                });
            }
            leftMenu.append(menuEl);
        });
        $(leftMenu.children()[0]).addClass("layui-nav-itemed");
        layui.element.init();

    }

    $("#LAY-user-get-vercode")
        .attr("src", API_BASE_PATH + "verify-code?_t=" + (new Date().getTime()))
        .on("click", reloadVerifyCode);

    function reloadVerifyCode() {
        $("#LAY-user-get-vercode")
            .attr("src", API_BASE_PATH + "verify-code?_t=" + (new Date().getTime()));
    }

    //读取配置信息
    if (!AppConfig.footer) {
        $('.layui-footer').hide();
        $('.layui-body').css('bottom', '0');
    }

});
