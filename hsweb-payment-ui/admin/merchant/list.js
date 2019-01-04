importResource("/admin/css/common.css");

require(["authorize"], function (authorize) {
    authorize.parse(document.body);
    window.authorize = authorize;
    importMiniui(function () {
        mini.parse();
        require(["miniui-tools", "request", "search-box", "message"], function (tools, request, SearchBox, message) {
            new SearchBox({
                container: $("#search-box"),
                onSearch: search,
                initSize: 2
            }).init();
            window.tools = tools;
            var grid = window.grid = mini.get("data-grid");
            tools.initGrid(grid);
            grid.setUrl(API_BASE_PATH + "manager/merchant");

            function search() {
                tools.searchGrid("#search-box", grid);
            }

            search();
            $(".add-button").on("click", function () {
                tools.openWindow("admin/merchant/save.html", "新建商户", "80%", "80%", search, function () {
                });
            });


            grid.getColumn("action").renderer = function (e) {
                var html = [];

                html.push(
                    tools.createActionButton("编辑", "icon-edit", function () {
                        tools.openWindow("admin/merchant/save.html?id=" + e.record.id, "编辑商户", "80%", "80%", grid.reload, function () {
                        });
                    })
                );

                html.push(
                    tools.createActionButton("查看交易记录", "icon-money", function () {
                        tools.openWindow("admin/account/log/list.html?merchantId=" + e.record.id, "交易记录", "80%", "80%", function () {

                        }, function () {
                        });
                    })
                );

                html.push(
                    tools.createActionButton("重发双重验证邮件通知", "icon-email", function () {
                        message.confirm("是否重新发送双重验证邮件通知", function () {
                            var loading = message.loading("加载中...");
                            request.post("manager/merchant/email/totp/" + e.record.userId, {}, function (res) {
                                    loading.hide();
                                    if (res.status !== 200) {
                                        message.alert(res.message);
                                    }
                                }
                            )
                        })
                    })
                );

                var row = e.record;
                if (!row.status || row.status.value !== 'ACTIVE') {
                    html.push(
                        tools.createActionButton("激活", "icon-ok", function () {
                            message.confirm("确认激活此商户,激活后商户将能进行正常交易.", function () {
                                request.put("manager/merchant/" + row.id + "/status/ACTIVE", {}, function (response) {
                                    if (response.status === 200) {
                                        grid.reload();
                                    } else {
                                        message.alert(response.message);
                                    }
                                })
                            })
                        })
                    );
                } else {
                    html.push(
                        tools.createActionButton("冻结", "icon-remove", function () {
                            message.confirm("确认冻结该商户,冻结后商户将不能进行正常交易.", function () {
                                request.put("manager/merchant/" + row.id + "/status/FREEZE", {}, function (response) {
                                    if (response.status === 200) {
                                        grid.reload();
                                    } else {
                                        message.alert(response.message);
                                    }
                                })
                            })
                        })
                    );
                }

                return html.join("")
            }
        });
    });
});

