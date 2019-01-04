define(["request"], function (request) {

    function init(containerId) {
        require(["text!pages/person-info/payee/list.html"], function (html) {
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
            // //日期时间范围
            // laydate.render({
            //     elem: '#applyTime'
            //     , type: 'datetime'
            //     , range: true
            // });

            form.render();
            table.render({
                elem: '#payee-info'
                , url: '/current-agent/payee-config-detail'
                , title: '收款人信息'
                , totalRow: true
                , cols: [[
                    {field: 'payee', title: '收款账号', sort: true}
                    ,
                    {field: 'payeeName', title: '收款人姓名'}
                    , {
                        field: 'payeeType', title: '收款类型', width: 150, templet: function (d) {
                            if (d.payeeType) {
                                return d.payeeType.text;
                            } else {
                                return "";
                            }
                        }, align: 'right'
                    }
                    , {
                        field: 'defaultWithdraw', title: '是否默认收款', templet: function (d) {
                            if (d.defaultWithdraw) {
                                return "是"
                            } else {
                                return "否"
                            }
                        }
                    }
                    , {fixed: 'right', title: '操作', toolbar: '#payInfoAction'}
                ]]
                ,
                id: 'payeeInfo'
                , request: {
                    pageName: "pageIndex",
                    limitName: "pageSize"
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
                        "data": res.result
                    }
                }
            });

            var payeeCache;

            function getPayeeConfig(call) {
                if (payeeCache) {
                    call(payeeCache);
                } else {
                    request.get("current-agent/payee-config", function (e) {
                        call(payeeCache = e.result);
                    });
                }
            }


            table.on('tool(payee-info)', function (obj) {

                var data = obj.data;
                var payeeInfo = data.payeeInfo || {};
                var detail = {
                    payee: data.payee,
                    payeeName: data.payeeName,
                    payeeType: (data.payeeType || {}).text || "",
                    bankId: (payeeInfo.bankId || {}).text || "",
                    accountName: payeeInfo.accountName || "",
                    accountNo: payeeInfo.accountNo || "",
                    idNumber: payeeInfo.idNumber || "",
                    accountType: payeeInfo.accountType || "",
                    branchName: payeeInfo.branchName || "",
                    province: payeeInfo.province || "",
                    city: payeeInfo.city || ""
                };
                //详情
                if (obj.event === 'payee-info-detail') {

                    getPayeeConfig(function (types) {
                        $("#bankPayeeDetail").html("");

                        for (var i = 0; i < types.length; i++) {
                            var option = types[i];
                            if (option.payeeType === data.payeeType.value) {
                                renderDetailForm(option);
                            }
                        }
                        $("#bankPayeeDetail input").attr("readonly", "true");
                        form.val('bank-payee-detail', detail);
                    });


                    layer.open({
                        id: 'bankPayee' //防止重复弹出
                        , type: 1
                        , area: ['600px', '700px']
                        , title: '收款人详情'
                        , content: $("#bankPayeeDetail")
                        , btn: '关闭'
                        , btnAlign: 'c' //按钮居中
                        , shade: 0 //不显示遮罩
                        , yes: function () {
                            // layer.alert("保存成功");
                            layer.closeAll();
                        }
                    });

                }
                else if (obj.event === 'edit-payee-info') {
                    getPayeeConfig(function (types) {
                        $("#bankPayeeDetail").html("");
                        for (var i = 0; i < types.length; i++) {
                            var option = types[i];
                            if (option.payeeType === data.payeeType.value) {
                                renderDetailForm(option);
                            }
                        }
                        console.log(detail);
                        form.val('bank-payee-detail', detail);

                        layer.open({
                            id: 'bankPayee' //防止重复弹出
                            , type: 1
                            , area: ['600px', '400px']
                            , title: '收款人详情'
                            , content: $("#bankPayeeDetail")
                            , btn: '提交'
                            , btnAlign: 'c' //按钮居中
                            , shade: 0 //不显示遮罩
                            , yes: function () {
                                layer.alert("保存成功");
                                // layer.closeAll();
                            }
                        });
                    })

                }
            })
        });

        function renderDetailForm(option) {
            var inputs = option.properties;
            var div = ' <div class="layui-tab-item" action="" lay-filter="payee-info" style="margin-top: 30px"> <form class="layui-form" action="" lay-filter="payee-info">';

            var type = '<input type="text" name="payeeType" value="' + option.payeeType + '" class="layui-input" style="display: none">';

            var div = ' <div class="layui-tab-item layui-show" action="" lay-filter="payee-info" style="margin-top: 30px"><form class="layui-form" action="" lay-filter="payee-info">';
            for (var j = 0; j < (option.properties).length; j++) {
                div += ' <div class="layui-form-item">\n' +
                    '    <label class="layui-form-label">' + inputs[j].name + '</label>\n' +
                    '    <div class="layui-input-block">\n' +
                    '      <input type="text" name="' + inputs[j].property + '" required  lay-verify="required" autocomplete="off" class="layui-input">\n' +
                    '    </div>\n' +
                    ' </div>';
            }
            $("#bankPayeeDetail").append(div + type);
        }

        $(".add-payee-btn").on("click", function () {
            var menu = {
                id: "add-payee",
                name: "收款信息",
                url: "./pages/person-info/payee/save"
            };
            loadMenu(menu);
        });
    }


    return {init: init}
});