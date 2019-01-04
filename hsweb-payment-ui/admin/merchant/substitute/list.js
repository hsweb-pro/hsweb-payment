importResource("/admin/css/common.css");
importResource("/admin/form/designer-drag/defaults.css");
require(["authorize"], function (authorize) {
    authorize.parse(document.body);
    window.authorize = authorize;
    importMiniui(function () {
        mini.parse();
        mini.getbyName("payeeType")
            .setUrl(API_BASE_PATH + "dictionary/define/payee-type/items");

        require(["miniui-tools", "request", "search-box", "message"], function (tools, request, SearchBox, message) {
            new SearchBox({
                container: $("#search-box"),
                onSearch: search,
                initSize: 2
            }).init();
            window.tools = tools;
            var grid = window.grid = mini.get("data-grid");
            tools.initGrid(grid);
            tools.multiSort(grid, search);

            grid.setUrl(API_BASE_PATH + "merchant/substitute");

            function search(sort) {
                tools.searchGrid("#search-box", grid, sort, {}, function (param) {
                    for (var key in param) {
                        if (key.indexOf("Amount") !== -1) {
                            param[key] = Math.round((parseFloat(param[key]) * 100));
                        }
                    }
                });
            }

            search();
            $(".add-button").on("click", function () {
                mini.get("withdraw-window").show()
            });
            var merchantId = mini.getbyName("merchantId");


            $(".save-button").on("click", function () {
                if (!merchantId.getValue()) {
                    message.showTips("请选择商户", 'danger');
                    return;
                }
                var amount = parseFloat(mini.getbyName("amount").getValue());

                if (amount <= 0) {
                    message.showTips("金额不能为0", 'danger');
                    return;
                }
                message.confirm("确定对[" + merchantId.getText() + "]提现:" + amount + "元", function () {
                    amount = amount * 100
                    var loading = message.loading("申请中");
                    request.post("merchant-withdraw/apply/" + merchantId.getValue(), {
                        transAmount: amount
                    }, function (resp) {
                        loading.hide();
                        if (resp.status === 200) {
                            mini.get("withdraw-window").hide();
                            grid.reload()
                        } else {
                            message.alert(resp.message);
                        }
                    })
                })
            });

            grid.getColumn("totalAmount").renderer=renderAmount;
            grid.getColumn("charge").renderer=renderAmount;


            function renderAmount(e) {
                var amount = parseFloat(e.value || 0);
                return mini.formatNumber(parseFloat((amount / 100).toFixed(2)), "#,0.00");
            }

            grid.getColumn('createTime').renderer = function (e) {
                return mini.formatDate(new Date(e.value), "yyyy-MM-dd HH:mm:ss")
            };

            var channelGrid = mini.get('channel-grid');
            tools.initGrid(channelGrid);
            channelGrid.setUrl(API_BASE_PATH + "channel/config/?paging=false");
            channelGrid.on('load', function () {
                channelGrid.mergeColumns(["channelName", "channelProviderName"])
            });
            var params = request.createQuery().where("transType", "WITHDRAW").getParams();
            var nowWithdrawId = "";
            channelGrid.getColumn("action").renderer = function (e) {

                return tools.createActionButton("确认", "icon-ok", function () {
                    message.confirm("确定使用[" + e.record.name + "]进行提现?确认后商户资金将发生变动.", function (msg) {
                        var loading = message.loading("提交中...");
                        mini.get("channelWindow").hide();
                        request.post("merchant-withdraw/handle/" + nowWithdrawId + "/" + e.record.id, {}, function (resp) {
                            grid.reload();
                            loading.hide();
                            if (resp.status !== 200) {
                                message.alert("操作失败:" + resp.message);
                            } else {
                                message.showTips("确认成功");
                            }
                        })
                    })
                })
            };
            channelGrid.load(params);

            grid.getColumn("action").renderer = function (e) {
                var html = [];
                var row = e.record;

                html.push(
                    tools.createActionButton("查看明细", "icon-find", function () {
                        tools.openWindow("admin/merchant/substitute/detail.html?substituteId=" + row.id, "代付明细", "80%", "80%", grid.reload, function () {
                        });
                    })
                );
                if (row.paymentId) {
                    html.push(
                        tools.createActionButton("查看资金流向", "icon-money", function () {
                            tools.openWindow("admin/account/log/list.html?paymentId=" + row.paymentId, "资金流向", "80%", "80%", grid.reload, function () {
                            });
                        })
                    )
                }
                return html.join("")
            }
        });
    });
});

