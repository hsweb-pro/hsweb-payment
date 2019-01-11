importResource("/admin/css/common.css");
importResource("/admin/form/designer-drag/defaults.css");
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
            tools.multiSort(grid, search);

            grid.setUrl(API_BASE_PATH + "merchant/complain");

            function search(sort) {
                tools.searchGrid("#search-box", grid, sort, {}, function (param) {
                    for (var key in param) {
                        if (key.indexOf("Amount") !== -1||key.indexOf("amount") !== -1) {
                            param[key] = Math.round((parseFloat(param[key]) * 100));
                        }
                    }
                });
            }

            search();
            $(".add-button").on("click", function () {
                mini.get("complain-window").show()
            });
            var paymentId = mini.getbyName("paymentId");

            paymentId.on('buttonclick', function () {
                tools.openWindow("admin/payment/order/list.html?selector=true", "选择订单", "80%", "80%", function (data) {
                    if (data !== 'close' && data !== 'cancel') {
                        paymentId.setValue(data.id);
                        paymentId.setText(data.id);
                    }
                });
            });

            $(".save-button").on("click", function () {

                var data = tools.getFormData("#applyForm", true);
                if (!data) {
                    return
                }

                request.post("merchant/complain/apply", data, function (response) {
                    if (response.status === 200) {
                        mini.get("complain-window").hide();
                        new mini.Form("#applyForm").reset();
                        grid.reload();
                    } else {
                        message.alert(response.message);
                    }
                })

            });

            function renderAmount(e) {
                var amount = parseFloat(e.value || 0);
                return mini.formatNumber(parseFloat((amount / 100).toFixed(2)), "#,0.00");
            }

            grid.getColumn('amount').renderer = renderAmount;
            grid.getColumn('merchantCompensateAmount').renderer = renderAmount;

            grid.getColumn('complainTime').renderer = function (e) {
                return mini.formatDate(new Date(e.value), "yyyy-MM-dd HH:mm:ss")
            };

            grid.getColumn('status').renderer = function (e) {
                var value = e.value.value;

                return e.value.text;
            };


            grid.getColumn("action").renderer = function (e) {
                var html = [];
                var row = e.record;

                if (row.status.value === 'APPLY') {
                    html.push(tools.createActionButton("开始处理", "icon-ok", function () {
                        message.confirm("确认开始处理此笔投诉,处理后商户余额将被冻结.", function () {
                            grid.loading("提交中...");
                            request.post("merchant/complain/handle", {complainId: row.id}, function (response) {
                                grid.reload();
                                if (response.status !== 200) {
                                    message.showTips("处理失败:" + response.message,"danger");
                                }
                            })
                        })
                    }))
                } else if (row.status.value === 'PROCESS') {
                    html.push(tools.createActionButton("", "icon-money", function () {

                        mini.getbyName("merchantCompensateAmount").setMaxValue(row.amount / 100);
                        mini.getbyName("merchantCompensateAmount").setValue(row.amount / 100);
                        mini.get("compensate-window").show();
                        $(".compensate-button").unbind("click").on("click", function () {
                            var value = mini.getbyName("merchantCompensateAmount").getValue();
                            value = parseFloat(value) * 100;

                            message.confirm("确认赔付?商户赔付金额:" + mini.formatNumber(value / 100, "#,0.00"), function () {
                                grid.loading("提交中...");
                                request.post("merchant/complain/compensate", {
                                    complainId: row.id,
                                    merchantCompensateAmount: value
                                }, function (response) {
                                    grid.reload();
                                    if (response.status !== 200) {
                                        message.showTips("处理失败:" + response.message,"danger");
                                    }
                                })
                            })
                        });

                    }))
                }
                if(row.status.value==='APPLY'||row.status.value==='PROCESS'){
                    html.push(tools.createActionButton("关闭", "icon-remove", function () {
                        message.confirm("确认解除此笔投诉?", function () {
                            grid.loading("提交中...");
                            request.post("merchant/complain/relieved", {complainId: row.id}, function (response) {
                                grid.reload();
                                if (response.status !== 200) {
                                    message.showTips("处理失败:" + response.message,"danger");
                                }
                            })
                        })
                    }))
                }

                return html.join("")
            }
        });
    });
});

