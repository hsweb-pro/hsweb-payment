importResource("/admin/css/common.css");

importMiniui(function () {
    mini.parse();

    function formatJson(json, options) {
        var reg = null,
            formatted = '',
            pad = 0,
            PADDING = '    ';
        options = options || {};
        options.newlineAfterColonIfBeforeBraceOrBracket = (options.newlineAfterColonIfBeforeBraceOrBracket === true) ? true : false;
        options.spaceAfterColon = (options.spaceAfterColon === false) ? false : true;
        if (typeof json !== 'string') {
            json = JSON.stringify(json);
        } else {
            json = JSON.parse(json);
            json = JSON.stringify(json);
        }
        reg = /([\{\}])/g;
        json = json.replace(reg, '\r\n$1\r\n');
        reg = /([\[\]])/g;
        json = json.replace(reg, '\r\n$1\r\n');
        reg = /(\,)/g;
        json = json.replace(reg, '$1\r\n');
        reg = /(\r\n\r\n)/g;
        json = json.replace(reg, '\r\n');
        reg = /\r\n\,/g;
        json = json.replace(reg, ',');
        if (!options.newlineAfterColonIfBeforeBraceOrBracket) {
            reg = /\:\r\n\{/g;
            json = json.replace(reg, ':{');
            reg = /\:\r\n\[/g;
            json = json.replace(reg, ':[');
        }
        if (options.spaceAfterColon) {
            reg = /\:/g;
            json = json.replace(reg, ':');
        }
        (json.split('\r\n')).forEach(function (node, index) {
                var i = 0,
                    indent = 0,
                    padding = '';

                if (node.match(/\{$/) || node.match(/\[$/)) {
                    indent = 1;
                } else if (node.match(/\}/) || node.match(/\]/)) {
                    if (pad !== 0) {
                        pad -= 1;
                    }
                } else {
                    indent = 0;
                }

                for (i = 0; i < pad; i++) {
                    padding += PADDING;
                }

                formatted += padding + node + '\r\n';
                pad += indent;
            }
        );
        return formatted;
    };
    require(["miniui-tools", "request", "search-box", 'message', 'pages/form/designer-drag/parser'],
        function (tools, request, SearchBox, message, FormParser) {
            var detailForm;
            require(["text!info.hf", "pages/form/designer-drag/components-default"], function (config) {
                detailForm = new FormParser(JSON.parse(config));
                var formEl = $(".order-detail");
                detailForm.render(formEl);
            });
            new SearchBox({
                container: $("#search-box"),
                onSearch: search,
                initSize: 2
            }).init();
            window.tools = tools;
            var grid = window.grid = mini.get("data-grid");
            tools.initGrid(grid);
            grid.setUrl(API_BASE_PATH + "payment/order");
            mini.getbyName("transType$in")
                .setUrl(API_BASE_PATH + "dictionary/define/trans-type/items");

            mini.getbyName("status$in")
                .setUrl(API_BASE_PATH + "dictionary/define/payment-status/items");


            function search() {
                tools.searchGrid("#search-box", grid,
                    {
                        // excludes: 'requestJson,responseJson'
                    },
                    {}, function (param) {
                        var amountLt = param["amount$lt"];
                        var amountGte = param["amount$gte"];
                        if (amountLt) {
                            param["amount$lt"] = Math.round((parseFloat(amountLt) * 100));
                        }
                        if (amountGte) {
                            param["amount$gte"] = Math.round((parseFloat(amountGte) * 100));
                        }
                    });
            }

            search();
            var tabs = mini.get("detail-tab");
            grid.getColumn("action").renderer = function (e) {
                var html = [];

                if (e.record.status.value === 'paying' || e.record.status.value === 'timeout') {
                    html.push(
                        tools.createActionButton("尝试主动查询渠道", "icon-ok", function () {
                            message.confirm("尝试主动查询渠道订单支付情况", function () {
                                var loading = message.loading("请求中...");
                                request.post("payment/active-query/" + e.record.id, {}, function (response) {
                                    if (response.status === 200) {
                                        window.setTimeout(function () {
                                            loading.hide();
                                            grid.reload();
                                        }, 3000)
                                    } else {
                                        loading.hide();
                                        message.alert(response.message);
                                    }
                                })
                            })
                        })
                    );
                }
                html.push(
                    tools.createActionButton("查看交易详情", "icon-find", function () {
                        var row = mini.clone(e.record);
                        row.notified = row.notified + "";
                        row.createTime = mini.formatDate(new Date(row.createTime), 'yyyy-MM-dd HH:mm:ss');
                        row.completeTime = mini.formatDate(new Date(row.completeTime), 'yyyy-MM-dd HH:mm:ss');
                        row.notifyTime = mini.formatDate(new Date(row.notifyTime), 'yyyy-MM-dd HH:mm:ss');

                        row.amount = mini.formatNumber(row.amount / 100, "#,0.00元");

                        mini.get("requestJson").setValue(row.requestJsonString);
                        mini.get("responseJson").setValue(row.responseJsonString);
                        mini.get("channelResult").setValue(formatJson(row.channelResult || "{}"));
                        $(".notifyUrl").text("");
                        $(".notifyTimes").text("");
                        $(".lastNotifyTime").text("");
                        mini.get("errorReason").setValue("");
                        $(".retry-notify").unbind("click");

                        request.createQuery("payment/notify/log/no-paging")
                            .where("paymentId", row.id)
                            .exec(function (resp) {
                                if (resp.status === 200 && resp.result.length > 0) {
                                    $(".notifyUrl").text(resp.result[0].notifyConfig.notifyUrl);
                                    $(".notifyTimes").text(resp.result[0].retryTimes);
                                    $(".lastNotifyTime").text(mini.formatDate(new Date(resp.result[0].lastNotifyTime), 'yyyy-MM-dd HH:mm:ss'));
                                    mini.get("errorReason").setValue(resp.result[0].errorReason);
                                    $(".retry-notify").on("click", function () {
                                        message.confirm("确认重新发起通知?", function () {
                                            var loading = message.loading("重试中...");
                                            request.post("payment/notify/log/" + resp.result[0].id + "/retry", {}, function (notifyResp) {
                                                loading.hide();
                                                if (notifyResp.status === 200) {
                                                    if (notifyResp.result.success === true) {
                                                        message.alert("通知成功")
                                                    } else {
                                                        message.alert("通知失败");
                                                        mini.get("errorReason").setValue(notifyResp.result.errorReason);
                                                    }
                                                } else {
                                                    message.alert("发起通知失败:" + notifyResp.message);
                                                }
                                            })
                                        })
                                    })
                                }
                            });

                        detailForm.setData(row);
                        detailForm.setReadOnly(true);
                        var logTab = tabs.getTab("log-tab");
                        logTab.url=API_BASE_PATH+"admin/logger/system/list.html?businessId="+row.id;
                        tabs.reloadTab(logTab)
                       // tabs.load(API_BASE_PATH+"admin/logger/system/list.html?businessId="+row.id,logTab)
                        //console.log(logTab);

                        mini.get("order-detail-window").show()
                    })
                );

                html.push(
                    tools.createActionButton("查看资金流向", "icon-money", function () {
                        tools.openWindow("admin/account/log/list.html?paymentId=" + e.record.id, "资金流向", "80%", "80%", grid.reload, function () {
                        });
                    }));

                return html.join("");
            };
            grid.getColumn("detail").renderer = function (e) {
                var row = e.record;
                var detail = [
                    "创建时间:" + mini.formatDate(new Date(row.createTime), 'yyyy-MM-dd HH:mm:ss') + "<br/>",
                    row.completeTime ?
                        "完成时间:" + mini.formatDate(new Date(row.completeTime), 'yyyy-MM-dd HH:mm:ss') : ""
                ];
                return detail.join('');
            };
            grid.getColumn("notifyInfo").renderer = function (e) {
                var row = e.record;
                var detail = [
                    row.notified ?
                        "通知时间:" + mini.formatDate(new Date(row.notifyTime), 'yyyy-MM-dd HH:mm:ss') : ""
                ];
                return detail.join('');
            };

            grid.getColumn('amount').renderer = function (e) {
                return e.record.currency + " " + (e.value / 100).toFixed(2);
            }
        });
});

