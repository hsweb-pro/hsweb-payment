define([], function () {

    function init(containerId) {
        require(["text!pages/withdraw/list.html"], function (html) {
            $("#container-" + containerId).html($(html));
            layui.element.init();
            initTable();
        });

    }

    function initTable() {
        layui.use(['table', 'form', 'laydate'], function () {
                var table = layui.table;
                var form = layui.form;
                var laydate = layui.laydate;
                //日期时间范围
                laydate.render({
                    elem: '#applyTime'
                    , type: 'datetime'
                    , range: true
                });
                form.render();
                table.render({
                    elem: '#substitute'
                    , url: '/current-merchant/withdraw-log'
                    , title: '提现列表'
                    , totalRow: true
                    , cols: [[
                        {field: 'id', title: '订单号', sort: true}
                        , {
                            field: 'transAmount', title: '交易金额(元)', width: 150, templet: function (d) {
                                var amount = (parseFloat(d.transAmount) / 100);
                                return (amount).format();
                            }, align: 'right', sort: true
                        }
                        , {
                            field: 'chargeAmount', title: '交易手续费(元)', width: 150, templet: function (d) {
                                var amount = (parseFloat(d.chargeAmount) / 100);
                                return (amount).format();
                            }, sort: true
                        }
                        , {
                            field: 'payeeType', title: '收款类型', width: 100, templet: function (d) {
                                return d.payeeType.text;
                            }, sort: true
                        }
                        , {
                            field: 'withdrawType', title: '提现方式', width: 100, templet: function (d) {
                                var value = "";
                                if (d.withdrawType.value === "AUTOMATIC") {
                                    value = "自动代付";
                                } else if (d.withdrawType.value === "MANUAL") {
                                    value = "人工付款";
                                }
                                return value;
                            }, minWidth: 50
                        }
                        , {
                            field: 'status', title: '状态', width: 90, templet: function (d) {
                                var value = "";
                                if (d.status.value === "APPLYING") {
                                    value = "申请中";
                                } else if (d.status.value === "HANDING") {
                                    value = "处理中";
                                } else if (d.status.value === "FAILURE") {
                                    value = "处理失败";
                                } else if (d.status.value === "CLOSE") {
                                    value = "已关闭";
                                } else if (d.status.value === "SUCCESS") {
                                    value = "处理成功"
                                } else {
                                    value = "状态异常"
                                }
                                return value;
                            }, minWidth: 50
                        }

                        , {
                            field: 'applyTime', title: '申请时间', width: 180, templet: function (d) {
                                return d.applyTime != null ? (new Date(d.applyTime)).format("yyyy-MM-dd hh:mm:ss") : "";
                            }, sort: true
                        }
                        , {
                            field: 'handleTime', title: '确认处理时间', width: 180, templet: function (d) {
                                return d.handleTime != null ? (new Date(d.handleTime)).format("yyyy-MM-dd hh:mm:ss") : "";
                            }, sort: true
                        }
                        , {
                            field: 'completeTime', title: '处理完成时间', width: 180, templet: function (d) {
                                return d.completeTime != null ? (new Date(d.completeTime)).format("yyyy-MM-dd hh:mm:ss") : "";
                            }, sort: true
                        }
                        , {
                            field: 'closeTime', title: '关闭时间', templet: function (d) {
                                return d.closeTime != null ? (new Date(d.closeTime)).format("yyyy-MM-dd hh:mm:ss") : "";
                            }, sort: true
                        }
                        , {
                            field: 'completeProve', title: '成功证明'
                        }
                    ]]
                    ,
                    id: 'substituteReload'
                    , request: {
                        pageName: "pageIndex",
                        limitName: "pageSize"
                    }
                    , page: {
                        curr: 0 //重新从第 1 页开始

                    }
                    , response:
                        {
                            statusCode: 200
                        }
                    ,
                    parseData: function (res) {
                        return {
                            "code": res.status,
                            "msg": '',
                            "count": res.result.total,
                            "data": res.result.withdrawLogList
                        }
                    }
                    , where: {
                        "sorts[0].name": "applyTime",
                        "sorts[0].order": "desc"
                    }
                });


                var $ = layui.$, active = {
                    reload: function () {
                        var id = $("input[name='id']");
                        var status = $("select[name='status']");
                        var transAmountGT = $("input[name='transAmountGT']");
                        var transAmountLT = $("input[name='transAmountLT']");
                        var applyTime = $("input[name='applyTime']");

                        //执行重载
                        table.reload('substituteReload', {
                            page: {
                                curr: 0 //重新从第 1 页开始
                            }
                            , where: encodeParams(
                                {
                                    id: id.val(),
                                    status: status.val(),
                                    transAmount$GTE: parseFloat(transAmountGT.val()) * 100,
                                    transAmount$LT: parseFloat(transAmountLT.val()) * 100,
                                    applyTime$btw: applyTime.val().replace(" - ", ",")
                                }
                            )
                        });
                    }
                };

                $(".yesterday-order").on("click", function () {

                    var day = new Date();
                    day.setDate(day.getDate() - 1);
                    var str1 = (day).format("yyyy-MM-dd");
                    var yesterday = str1 + " 00:00:00 - " + str1 + " 23:59:59";
                    laydate.render({
                        elem: '#applyTime'
                        , type: 'datetime'
                        , value: yesterday
                        , range: true
                    });
                    active.reload();

                });
                $(".seven-days-order").on("click", function () {
                    //日期时间范围
                    //今天
                    var nowTime =  (new Date()).format("yyyy-MM-dd");

                    //七天前
                    var day = new Date();
                    day.setDate(day.getDate() - 6);
                    var sevenDay = day.format("yyyy-MM-dd");
                    var sevensDay = sevenDay + " 00:00:00 - " + nowTime + " 23:59:59";

                    laydate.render({
                        elem: '#applyTime'
                        , type: 'datetime'
                        , value: sevensDay
                        , range: true
                    });
                    active.reload();
                });
                $(".month-order").on("click", function () {
                    //日期时间范围
                    //今天
                    var nowTime = (new Date()).format("yyyy-MM-dd");

                    //三十天前
                    var day = new Date();
                    day.setDate(day.getDate() - 29);
                    var thirtyDay = (day).format("yyyy-MM-dd");

                    var thirtyDays = thirtyDay + " 00:00:00 - " + nowTime + " 23:59:59";
                    laydate.render({
                        elem: '#applyTime'
                        , type: 'datetime'
                        , value: thirtyDays
                        , range: true
                    });
                    active.reload();
                });

                $('.search-btn').on('click', function () {
                    var type = $(this).data('type');
                    active[type] ? active[type].call(this) : '';
                });
            }
        );

        $(".apply-btn").on('click', function () {
            var menu = {
                id: "apply",
                name: "提现申请",
                url: "./pages/withdraw/apply/list"
            };
            loadMenu(menu);
        });
    }



    return {init: init}
});