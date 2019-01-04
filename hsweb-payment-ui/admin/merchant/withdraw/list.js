importResource("/admin/css/common.css");
importResource("/admin/form/designer-drag/defaults.css");
require(["authorize"], function (authorize) {
    authorize.parse(document.body);
    window.authorize = authorize;
    importMiniui(function () {
        mini.parse();
        mini.getbyName("withdrawType")
            .setUrl(API_BASE_PATH + "dictionary/define/withdraw-type/items");

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

            grid.setUrl(API_BASE_PATH + "merchant-withdraw");

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

            merchantId.on('buttonclick', function () {
                tools.openWindow("admin/merchant/selector.html", "选择商户", "80%", "80%", function (data) {
                    if (data !== 'close' && data !== 'cancel') {
                        mini.getbyName("merchantId").setValue(data.id);
                        mini.getbyName("merchantId").setText(data.name);
                    }
                });
            });

            merchantId.on("valuechanged", function (e) {
                if (merchantId.getText() && merchantId.getValue() === merchantId.getText()) {
                    request.createQuery("manager/merchant/no-paging")
                        .where()
                        .like("name", "%" + merchantId.getText() + "%")
                        .or().like("id", "%" + merchantId.getText() + "%")
                        .limit(0, 2)
                        .exec(function (resp) {
                            if (resp.status === 200 && resp.result.length === 1) {
                                merchantId.setText(resp.result[0].name);
                                merchantId.setvalue(resp.result[0].id);
                            }
                        })
                }
            })
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

            function renderAmount(e) {
                var amount = parseFloat(e.value || 0);
                return mini.formatNumber(parseFloat((amount / 100).toFixed(2)), "#,0.00");
            }

            grid.getColumn('payeeInfo').renderer = function (e) {
                var row = e.record;
                if (row.payeeInfo) {
                    var str = [
                        "收款方式:",
                        row.payeeType.text,
                        "<br>收款人:",
                        row.payeeInfo.payeeName,
                        " ",
                        row.payeeInfo.payee
                    ];
                    return str.join("");
                }
                return "/";
            }
            grid.getColumn('transAmount').renderer = renderAmount;
            grid.getColumn('chargeAmount').renderer = renderAmount;

            grid.getColumn('applyTime').renderer = function (e) {
                return mini.formatDate(new Date(e.value), "yyyy-MM-dd HH:mm:ss")
            };

            grid.getColumn('status').renderer = function (e) {
                var value = e.value.value;
                if (value === 'SUCCESS') {
                    return e.value.text + "<br/>" + "完成时间:" +
                        (mini.formatDate(new Date(e.record.completeTime), 'yyyy-MM-dd HH:mm:ss'))
                }
                if (value === 'HANDLING') {
                    return e.value.text + "<br/>" + "处理时间:" +
                        (mini.formatDate(new Date(e.record.handleTime), 'yyyy-MM-dd HH:mm:ss'))
                }
                if (value === 'CLOSE') {
                    return e.value.text + "<br/>" + "关闭时间:" +
                        (mini.formatDate(new Date(e.record.closeTime), 'yyyy-MM-dd HH:mm:ss')) +
                        "<br/>关闭原因:" + (e.record.comment || '无')
                }
                return e.value.text;
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
                if (row.withdrawType.value === 'MANUAL') {
                    if (row.status.value !== 'SUCCESS' && row.status.value !== 'CLOSE') {
                        if (row.status.value === 'HANDING') {
                            html.push(
                                tools.createActionButton("完成提现", "icon-ok", function () {
                                    message.prompt("完成提现", "说明", function (msg) {
                                        if (!msg) {
                                            message.alert("请输入说明");
                                            return;
                                        }
                                        var loading = message.loading("提交中...");
                                        request.post("withdraw/offline/complete/" + row.paymentId, msg, function (resp) {
                                            grid.reload();
                                            loading.hide();
                                            if (resp.status !== 200) {
                                                message.alert("操作失败:" + resp.message);
                                            }
                                        })
                                    }, true)
                                })
                            );
                            html.push(
                                tools.createActionButton("关闭提现", "icon-remove", function () {
                                    message.prompt("关闭提现申请", "关闭原因", function (msg) {
                                        var loading = message.loading("提交中...");
                                        request.post("withdraw/offline/close/" + row.paymentId, msg, function (resp) {
                                            grid.reload();
                                            loading.hide()
                                            if (resp.status !== 200) {
                                                message.alert("操作失败:" + resp.message);
                                            }
                                        })
                                    }, true)
                                })
                            );
                        }
                        if (row.status.value === 'APPLYING') {
                            html.push(
                                tools.createActionButton("确认提现申请", "icon-ok", function () {
                                    nowWithdrawId = row.id;
                                    mini.get("channelWindow").show();
                                })
                            );
                            html.push(
                                tools.createActionButton("关闭提现", "icon-remove", function () {
                                    message.prompt("关闭提现申请", "关闭原因", function (msg) {
                                        var loading = message.loading("提交中...");
                                        request.post("merchant-withdraw/close/" + row.id, msg, function (resp) {
                                            grid.reload();
                                            loading.hide()
                                            if (resp.status !== 200) {
                                                message.alert("操作失败:" + resp.message);
                                            }
                                        })
                                    }, true)
                                })
                            );
                        }

                    }
                }

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

