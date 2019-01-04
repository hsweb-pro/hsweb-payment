define(["request"], function (request) {

    function init(containerId) {
        require(["text!pages/order/list.html"], function (html) {
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
                elem: '#orderDate'
                , type: 'datetime'
                , range: true
            });
            form.render();

            table.render({
                elem: '#order'
                , url: '/current-merchant/order'
                , autoSort: false
                , totalRow: true
                , where: {
                    "sorts[0].name": "createTime",
                    "sorts[0].order": "desc",
                    "terms[0].column": "transType$not",
                    "terms[0].value": "WITHDRAW"
                }
                , cols: [[
                    {field: 'id', title: '订单号', width: 200, sort: true}

                    , {
                        field: 'amount', title: '交易金额(元)', width: 150, templet: function (e) {
                            var money = parseFloat(e.amount) / 100;
                            return (money).format();
                        }, align: "right", sort: true
                    }
                    , {field: 'productName', title: '产品名称'}
                    , {field: 'channelName', title: '渠道名称', width: 200}
                    , {
                        field: 'status', title: '状态', templet: function (d) {
                            return d.status != null ? d.status.text : "";
                        }, width: 200
                    }
                    , {
                        field: 'createTime', title: '创建时间', templet: function (d) {
                            return (new Date(d.createTime)).format("yyyy-MM-dd hh:mm:ss");
                        }, width: 200, sort: true
                    }
                    ,
                    {fixed: 'right', title: '操作', width: 100, toolbar: '#orderAction'}

                ]]
                , request: {
                    pageName: "pageIndex",
                    limitName: "pageSize"
                }
                , page: {
                    curr: 0 //重新从第 1 页开始

                }
                , id: 'orderReload'
                , response: {
                    statusCode: 200
                }
                , parseData: function (res) {
                    return {
                        "code": res.status,
                        "msg": '',
                        "count": res.result.total,
                        "data": res.result.data
                    }
                }
            });


            table.on('sort(order)', function (obj) {
                table.reload('orderReload', {
                    initSort: obj
                    , page: {
                        curr: 0 //重新从第 1 页开始

                    }
                    , where: {
                        "sorts[0].name": obj.field,
                        "sorts[0].order": obj.type
                    }
                });
            });

            table.on('tool(order)', function (obj) {
                var data = obj.data;

                if (obj.event === 'detail') {
                    var loading = layer.open({
                        shadeClose: false
                        , type: 3
                        , content: '...'
                    });
                    //获取数据
                    request.get("current-merchant/order/" + data.id, function (e) {
                        $(".order-detail-data").html("");

                        var field = {
                            "amount": "金额(元)",
                            "channelName": "渠道名称",
                            "completeTime": "完成时间",
                            "createTime": "创建时间",
                            "currency": "币种",
                            "notified": "是否通知",
                            "notifyTime": "通知时间",
                            "orderId": "订单ID",
                            "productName": "产品名称",
                            "status": "状态",
                            "transType": "交易类型",
                            "serviceAmount": "手续费(元)",
                            "serviceComment": "手续费说明"
                        };

                        for (var key in e.result) {
                            var value = e.result[key];
                            var data = '';
                            if (field[key] !== undefined) {
                                if (key === 'status' || key === 'transType') {
                                    data = '<tr><td>' + field[key] + '</td><td>' + value.text + '</td></tr>';
                                } else if (key === 'completeTime' || key === 'notifyTime' || key === 'createTime') {
                                    if (e.result[key]) {
                                        var date = (new Date(e.result[key])).format("yyyy-MM-dd hh:mm:ss");
                                        data = '<tr><td>' + field[key] + '</td><td>' + date + '</td></tr>';
                                    }
                                }
                                else if (key === 'notified') {
                                    var value = "";
                                    if (e.result[key]) {
                                        value = "是"
                                    } else {
                                        value = "否"
                                    }
                                    data = '<tr><td>' + field[key] + '</td><td>' + value + '</td></tr>';
                                } else if (key === "amount" || key === 'serviceAmount') {
                                    var amount = (parseFloat(e.result[key] / 100)).format();
                                    data = '<tr><td>' + field[key] + '</td><td>' + amount + '</td></tr>';
                                } else {
                                    data = '<tr><td>' + field[key] + '</td><td>' + e.result[key] + '</td></tr>';
                                }
                            }

                            $(".order-detail-data").append(data);
                        }
                        layer.close(loading);
                        layer.open({
                            id: 'layerDemo' //防止重复弹出
                            , type: 1
                            , area: ['600px', '450px']
                            , title: '订单详情'
                            , content: $("#order-detail")
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
                    var paymentId = $("input[name='id']");
                    var status = $("select[name='status']");
                    var transType = $("select[name='transType']");
                    var amountGT = $("input[name='amountGT']");
                    var amountLT = $("input[name='amountLT']");
                    var createTime = $("input[name='createTime']");
                    //执行重载
                    table.reload('orderReload', {
                        page: {
                            curr: 0 //重新从第 1 页开始

                        }

                        , where: encodeParams(
                            {
                                id: paymentId.val(),
                                status: status.val(),
                                amount$GTE: parseFloat(amountGT.val()) * 100,
                                amount$LT: parseFloat(amountLT.val()) * 100,
                                createTime$btw: createTime.val().replace(" - ", ","),
                                transType: transType.val(),
                                transType$not: "WITHDRAW"
                            }
                        )
                    });
                }
            };


            $('.search-order .layui-btn').on('click', function () {
                var type = $(this).data('type');
                active[type] ? active[type].call(this) : '';
            });

            $(".download-order").on('click', function () {
                var paymentId = $("input[name='id']").val();
                var status = $("select[name='status']").val();
                var transType = $("select[name='transType']").val();
                var amountGT = $("input[name='amountGT']").val();
                var amountLT = $("input[name='amountLT']").val();
                var createTime = $("input[name='createTime']").val();

                var params = request.createQuery()
                    .where("paymentId", paymentId)
                    .and("amountGT", amountGT)
                    .and("status", status)
                    .and("transType", transType)
                    .and("amountLT", amountLT)
                    .and("createTime", createTime)
                    .noPaging()
                    .getParams();
                var form = $("<form style='display: none' target='_blank' action='" + API_BASE_PATH + "current-merchant/download'>");
                for (var key in params) {
                    form.append($("<input type='hidden'>").attr("name", key).val(params[key]))
                }
                form.appendTo($(document.body))
                    .submit()
                    .remove();
            });


            $(".yesterday-order").on("click", function () {

                var day = new Date();
                day.setDate(day.getDate() - 1);
                var str1 = day.format("yyyy-MM-dd");
                var yesterday = str1 + " 00:00:00 - " + str1 + " 23:59:59";
                laydate.render({
                    elem: '#orderDate'
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
                var sevenDay = day.format("yyyy-MM-dd");
                var sevensDay = sevenDay + " 00:00:00 - " + nowTime + " 23:59:59";

                laydate.render({
                    elem: '#orderDate'
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
                var thirtyDay = day.format("yyyy-MM-dd");

                var thirtyDays = thirtyDay + " 00:00:00 - " + nowTime + " 23:59:59";
                laydate.render({
                    elem: '#orderDate'
                    , type: 'datetime'
                    , value: thirtyDays
                    , range: true
                });
                active.reload();
            });
        });
    }



    return {init: init}
});