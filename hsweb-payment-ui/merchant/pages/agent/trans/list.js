define(["request"], function (request) {

    function init(containerId) {
        require(["text!pages/agent/trans/list.html"], function (html) {
            $("#container-" + containerId).html($(html));
            layui.element.init();
            initTable();

        });

    }

    function initTable() {
        layui.use(['table', 'form', 'laydate', 'layer'], function () {
            var form = layui.form;
            var table = layui.table;
            var laydate = layui.laydate;
            var layer = layui.layer;
            //日期时间范围
            laydate.render({
                elem: '#transDate'
                , type: 'datetime'
                , range: true
            });
            form.render();

            table.render({
                elem: '#trans'
                , url: '/account-trans-log/me'
                , totalRow: true
                , cols: [[
                    {field: 'id', title: 'ID', width: 200, sort: true}
                    , {field: 'paymentId', title: '订单号' ,width: 200,}
                    , {
                        field: 'transType', title: '交易类型', templet: function (e) {
                            if (e.transType) {
                                return e.transType.text;
                            }
                            return "";
                        },width:150, sort: true
                    }
                    , {
                        field: 'transAmount', title: '交易金额(元)', templet: '#transLogAmount'
                        ,align:'right',width:150, sort: true
                    }
                    , {
                        field: 'balance', title: '交易后余额(元)', templet: function (e) {
                            var amount = (parseFloat(e.balance) / 100);
                            return (amount).format();
                        },align:'right',width:150, sort: true
                    }
                    , {
                        field: 'createTime', title: '创建时间', templet: function (d) {
                            return (new Date(d.createTime)).format("yyyy-MM-dd hh:mm:ss");
                        }, width: 200, sort: true
                    }, {field: 'comment', title: '备注'}
                ]]
                , request: {
                    pageName: "pageIndex",
                    limitName: "pageSize"
                }
                , page: {
                    curr: 0
                }
                , id: 'transReload'
                , response: {
                    statusCode: 200
                }
                , autoSort: false
                , initSort: {
                    field: 'createTime' //排序字段，对应 cols 设定的各字段名
                    , type: 'desc' //排序方式  asc: 升序、desc: 降序、null: 默认排序
                }
                , parseData: function (res) {
                    return {
                        "code": res.status,
                        "msg": '',
                        "count": res.result.total,
                        "data": res.result.data
                    }
                }
                ,where:{
                    "sorts[0].name": "createTime",
                    "sorts[0].order": "desc"
                }
            });


            table.on('sort(trans)', function (obj) {
                table.reload('transReload', {
                    initSort: obj,
                    page: {
                        curr: 0
                    }
                    , where: {
                        "sorts[0].name": obj.field,
                        "sorts[0].order": obj.type
                    }
                });
            });
            table.on('tool(trans)', function (obj) {
                var data = obj.data;
                if (obj.event === 'detail') {
                    //获取数据
                    request.get("/payment/trans/" + data.id, function (e) {
                        $(".trans-detail-data").html("");
                        for (var key in e.result) {
                            var value = e.result[key];
                            var data = '';
                            if (key == 'status' || key == 'transType') {
                                data = '<tr><td>' + key + '</td><td>' + value.text + '</td></tr>';
                            } else {
                                data = '<tr><td>' + key + '</td><td>' + e.result[key] + '</td></tr>';
                            }
                            $(".trans-detail-data").append(data);
                        }
                        layer.open({
                            id: 'layerDemo' //防止重复弹出
                            , type: 1
                            , title: '订单详情'
                            , content: $("#trans-detail")
                            , btn: '关闭全部'
                            , btnAlign: 'c' //按钮居中
                            , shade: 0 //不显示遮罩
                            , yes: function () {
                                layer.closeAll();
                            }
                        });
                    });
                }
            });


            var $ = layui.$, active = {
                reload: function () {
                    var id = $("input[name='id']");
                    var paymentId = $("input[name='paymentId']");
                    var transType = $("select[name='transType']");
                    var amountGT = $("input[name='transAmountGT']");
                    var amountLT = $("input[name='transAmountLT']");
                    var createTime = $("input[name='createTime']");
                    var accountTransType = $("select[name='accountTransType']");
                    //执行重载
                    table.reload('transReload', {
                        page: {
                            curr: 0 //重新从第 1 页开始

                        }

                        , where: encodeParams(
                            {
                                id: id.val(),
                                paymentId: paymentId.val(),
                                transAmount$GTE: parseFloat(amountGT.val()) * 100,
                                transAmount$LT: parseFloat(amountLT.val()) * 100,
                                createTime$btw: createTime.val().replace(" - ", ","),
                                transType: transType.val(),
                                accountTransType: accountTransType.val()
                            }
                        )
                    });
                }
            };

            $(".yesterday-order").on("click", function () {
                var day = new Date();
                day.setDate(day.getDate() - 1);
                var str1 = day.format("yyyy-MM-dd");
                var yesterday = str1 + " 00:00:00 - " + str1 + " 23:59:59";
                laydate.render({
                    elem: '#transDate'
                    , type: 'datetime'
                    , value: yesterday
                    , range: true
                });
                active.reload();
            });
            $(".seven-days-order").on("click", function () {
                //日期时间范围
                //今天
                var nowTime = (new Date()).format("yyyy-MM-dd");

                //七天前
                var day = new Date();
                day.setDate(day.getDate() - 6);
                var sevenDay =day.format("yyyy-MM-dd");
                var sevensDay = sevenDay + " 00:00:00 - " + nowTime + " 23:59:59";
                laydate.render({
                    elem: '#transDate'
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
                var thirtyDay =(day).format("yyyy-MM-dd");
                var thirtyDays = thirtyDay + " 00:00:00 - " + nowTime + " 23:59:59";
                laydate.render({
                    elem: '#transDate'
                    , type: 'datetime'
                    , value: thirtyDays
                    , range: true
                });
                active.reload();
            });


            $('.search-trans .layui-btn').on('click', function () {
                var type = $(this).data('type');
                active[type] ? active[type].call(this) : '';
            });
        });
    }

    return {init: init}
});