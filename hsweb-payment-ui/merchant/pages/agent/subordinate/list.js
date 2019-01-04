define(["request"], function (request) {

    function init(containerId) {
        require(["text!pages/agent/subordinate/list.html"], function (html) {
            $("#container-" + containerId).html($(html));
            layui.element.init();
            initTable();
        });
    }

    function initTable() {
        layui.use(['table', 'form'], function () {
            var table = layui.table;
            var form = layui.form;
            form.render();
            table.render({
                elem: '#subordinate'
                , url: '/current-agent/agent-merchant'
                , cols: [[
                    {field: 'id', title: 'ID'}
                    , {field: 'name', title: '商户名称', sort: true}
                    , {field: 'phone', title: '手机号', sort: true}
                    , {
                        field: 'status', title: '账户状态', templet: function (d) {
                            return d.status.text;
                        }
                    }
                    , {field: 'email', title: '邮箱'}
                    , {fixed: 'right', title: '操作', toolbar: '#agentAction'}
                ]]
                , response: {
                    statusCode: 200
                }
                , parseData: function (res) {
                    return {
                        "code": res.status,
                        "msg": '',
                        "data": res.result
                    }
                }
                ,id:"allSubordinate"
                ,where:{
                    "sorts[0].name": "createTime",
                    "sorts[0].order": "desc"
                }, page: {
                    curr: 0
                }
            });

            table.on('tool(subordinate)', function (obj) {
                var event = obj.event;
                var data = obj.data;

                if (event === 'agent-detail') {
                    var status = data.status.text;
                    data.status = status;

                    layer.open({
                        id: 'agentDetail' //防止重复弹出
                        , type: 1
                        , title: '商户详情'
                        , content: $("#agentDetailForm")
                        , btn: '关闭全部'
                        , btnAlign: 'c' //按钮居中
                        , shade: 0 //不显示遮罩
                        , area: ['800px', '600px']
                        , success: function () {
                            $('#merchantCardFrontImg').attr('src', data.idCardFront);
                            $('#merchantCardBackImg').attr('src', data.idCardBack);
                            $('#businessLicenseImg').attr('src', data.businessLicense);
                            form.val('merchant-detail-form', data);
                        }
                        , yes: function () {
                            layer.closeAll();
                        }
                    });
                }
                else if (event === 'agent-charge') {
                    layer.open({
                        id: 'agentDetail' //防止重复弹出
                        , type: 1
                        , area: ['500px', '400px']
                        , title: '代理费率'
                        , content: $("#agentCharge")
                        , btn: '关闭全部'
                        , btnAlign: 'c' //按钮居中
                        , shade: 0 //不显示遮罩
                        , closeBtn: 0
                        , success: function () {
                            table.render({
                                elem: '#agentCharge'
                                , url: '/current-agent/agent/rate-configs/' + data.id
                                , cols: [[
                                    {field: 'channelName', title: '渠道名称', sort: true}
                                    , {field: 'rate', title: '手续费'}
                                    , {field: 'rateType', title: '手续费类型',templet:function (e) {
                                            return e.rateType.text;
                                        }}
                                    , {
                                        field: 'transType', title: '交易类型',templet:function (e) {
                                            return e.transType.text;
                                        }
                                    }
                                ]]
                                , response: {
                                    statusCode: 200
                                }
                                , parseData: function (res) {
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
                            $("#agentCharge").hide();
                            $("[lay-id=agentCharge]").remove()
                        }
                    });
                }
            });

        });
    }


    return {init: init}
});