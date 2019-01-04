define(["request"], function (request) {

    function init(containerId) {
        require(["text!pages/substitute/list.html"], function (html) {
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
                    , url: '/current-merchant/substitute'
                    , title: '提现列表'
                    , totalRow: true
                    , cols: [[
                        {field: 'transNo', title: '交易号', sort: true}
                        , {
                            field: 'totalAmount', title: '交易金额(元)', width: 120, templet: function (d) {
                                var amount = (parseFloat(d.totalAmount) / 100);
                                return (amount).format();
                            }, align: 'center', sort: true
                        }
                        , {
                            field: 'realAmount', title: '实际到账(元)', width: 120, templet: function (d) {
                                var amount = (parseFloat(d.realAmount) / 100);
                                return (amount).format();
                            }, align: 'center', sort: true
                        }
                        // , {
                        //     field: 'charge', title: '预计收费(元)', width: 120, templet: function (d) {
                        //         var amount = (parseFloat(d.charge) / 100);
                        //         return (amount).format();
                        //     }, align: 'center', sort: true
                        // }
                        , {
                            field: 'realCharge', title: '服务费(元)', width: 120, templet: function (d) {
                                var amount = (parseFloat(d.realCharge) / 100);
                                return (amount).format();
                            }, align: 'center', sort: true
                        }
                        , {field: 'total', title: '总数(笔)', width: 120, align: 'center', sort: true}
                        , {field: 'totalSuccess', title: '总成功数(笔)', width: 120, align: 'center', sort: true}
                        , {
                            field: 'status', title: '状态', width: 100, templet: function (d) {
                                var value = "";
                                if (d.status.value === "PROCESSING") {
                                    value = "处理中";
                                } else if (d.status.value === "SUCCESS") {
                                    value = "成功";
                                } else if (d.status.value === "FAIL") {
                                    value = "失败";
                                }
                                return value;
                            }, minWidth: 50
                        }
                        , {
                            field: 'payeeType', title: '收款方式', width: 120, templet: function (d) {
                                var value = "";
                                if (d.payeeType.value === "BANK") {
                                    value = "银行卡";
                                } else if (d.payeeType.value === "ALIPAY") {
                                    value = "支付宝";
                                }
                                return value;
                            }, minWidth: 50
                        },
                        {
                            field: 'completeTime', title: '完成时间', templet: function (d) {
                                return d.completeTime != null ? (new Date(d.completeTime)).format("yyyy-MM-dd hh:mm:ss") : "";
                            }, sort: true
                        },
                        {
                            field: 'createTime', title: '创建时间', templet: function (d) {
                                return d.createTime != null ? (new Date(d.createTime)).format("yyyy-MM-dd hh:mm:ss") : "";
                            }, sort: true
                        }
                        , {field: 'remark', title: '备注', sort: true}
                        , {fixed: 'right', title: '操作', width: 80, toolbar: '#detailAction'}
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
                            "data": res.result.data
                        }
                    }
                    ,where: {
                        "sorts[0].name": "createTime",
                        "sorts[0].order": "desc"
                    }
                });


                table.on('sort(substitute)',function (obj) {
                   table.reload('substituteReload',{
                       initSort : obj,
                       page:{
                           curr:1
                       },
                       where:{
                           "sorts[0].name": obj.field,
                           "sorts[0].order": obj.type
                       }
                   });
                });
                table.on('tool(substitute)', function (obj) {
                    var data = obj.data;

                    if (obj.event === 'detail') {
                        var loading = layer.open({
                            shadeClose: false
                            , type: 3
                            , content: '...'
                        });

                        layer.close(loading);
                        layer.open({
                            id: 'layerDemo' //防止重复弹出
                            , type: 1
                            , area: ['1000px', '450px']
                            , title: '订单详情'
                            , content: $("#details")
                            , btn: '关闭全部'
                            , btnAlign: 'c' //按钮居中
                            , shade: 0 //不显示遮罩
                            , success: function () {
                                table.render({
                                    elem: '#details'
                                    , url: '/current-merchant/' + data.id + '/details'
                                    , title: '代付详情'
                                    , cols: [[
                                        {field: 'transNo', title: '交易号', sort: true}
                                        , {field: 'paymentId', title: '订单号', sort: true}
                                        , {field: 'payeeName', title: '收款人姓名',align: 'center',}
                                        , {
                                            field: 'amount', title: '金额(元)', templet: function (e) {
                                                var money = parseFloat(e.amount) / 100;
                                                return (money).format();
                                            }, align: 'center',sort: true
                                        }
                                        , {
                                            field: 'status', title: '状态',align: 'center', templet: function (d) {
                                                return d.status != null ? d.status.text : "";
                                            }
                                        }
                                        , {
                                            field: 'chargeAmount', title: '手续费(元)',align: 'center', templet: function (e) {
                                                var money = parseFloat(e.chargeAmount) / 100;
                                                return (money).format();
                                            }, sort: true
                                        }
                                        , {field: 'chargeMemo', title: '手续费说明'}
                                        , {field: 'remark', title: '备注'}
                                    ]]
                                    , response: {
                                        statusCode: 200
                                    },
                                    parseData: function (res) {
                                        return {
                                            "code": res.status,
                                            "msg": '',
                                            "data": res.result
                                        }
                                    }
                                });
                            }
                            , yes: function () {
                                layer.closeAll();
                                $("#details").hide();
                                $("[lay-id=details]").remove();
                            }
                        });

                    }
                });

                var $ = layui.$, active = {
                    reload: function () {
                        var transNo = $("input[name='transNo']");
                        var status = $("select[name='status']");
                        var totalAmountGT = $("input[name='totalAmountGTE']");
                        var totalAmountLT = $("input[name='totalAmountLT']");
                        var createTime = $("input[name='createTime']");

                        //执行重载
                        table.reload('substituteReload', {
                            page: {
                                curr: 0 //重新从第 1 页开始
                            }
                            , where: encodeParams(
                                {
                                    transNo: transNo.val(),
                                    status: status.val(),
                                    totalAmount$GTE: parseFloat(totalAmountGT.val()) * 100,
                                    totalAmount$LT: parseFloat(totalAmountLT.val()) * 100,
                                    createTime$btw: createTime.val().replace(" - ", ",")
                                }
                            )
                        });
                    }
                };

                $(".yesterday-data").on("click", function () {

                    var day = new Date();
                    day.setDate(day.getDate() - 1);
                    var str1 = (day).format("yyyy-MM-dd");
                    var yesterday = str1 + " 00:00:00 - " + str1 + " 23:59:59";
                    laydate.render({
                        elem: '#createTime'
                        , type: 'datetime'
                        , value: yesterday
                        , range: true
                    });
                    active.reload();

                });
                $(".seven-days-data").on("click", function () {
                    //日期时间范围
                    //今天
                    var nowTime = (new Date()).format("yyyy-MM-dd");

                    //七天前
                    var day = new Date();
                    day.setDate(day.getDate() - 6);
                    var sevenDay = day.format("yyyy-MM-dd");
                    var sevensDay = sevenDay + " 00:00:00 - " + nowTime + " 23:59:59";

                    laydate.render({
                        elem: '#createTime'
                        , type: 'datetime'
                        , value: sevensDay
                        , range: true
                    });
                    active.reload();
                });
                $(".month-data").on("click", function () {
                    //日期时间范围
                    //今天
                    var nowTime = (new Date()).format("yyyy-MM-dd");

                    //三十天前
                    var day = new Date();
                    day.setDate(day.getDate() - 29);
                    var thirtyDay = (day).format("yyyy-MM-dd");

                    var thirtyDays = thirtyDay + " 00:00:00 - " + nowTime + " 23:59:59";
                    laydate.render({
                        elem: '#createTime'
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
    }

    return {init: init}
});