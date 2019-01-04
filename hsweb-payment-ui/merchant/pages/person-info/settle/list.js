define(["request"], function (request) {

    function init(containerId) {
        require(["text!pages/person-info/settle/list.html"], function (html) {
            $("#container-" + containerId).html($(html));
            layui.element.init();
            loadData();
        });
    }

    function loadData() {
        var id = "";
        layui.use(['form', 'layedit', 'laydate', 'upload'], function () {
            var form = layui.form
                , layer = layui.layer;
            form.render();

            request.get("dictionary/define/bank-code/items", function (data) {
                if (data.status === 200) {
                    var options = data.result;
                    for (var i = 0, l = options.length; i < l; i++) {
                        var option = options[i];
                        $("#selectBankCode").append("<option value='" + option.value + "'>" + option.text + "</option>")
                    }
                    form.render();
                    getSettleInfo();
                } else {
                    layer.alert("数据加载失败！");
                }

            });

            function getSettleInfo(verifyCode) {
                request.get("current-merchant/settle-configs?verifyCode=" + verifyCode, function (data) {
                    if (data.status === 200) {
                        if (data.result) {
                            var bankId = data.result.bankId;
                            bankId != null ? data.result.bankId = bankId.value : null;
                            form.val('substitute-info', data.result);
                        }

                    } else if (data.status === 403 && data.code === "need_tow_factor") {
                        layer.prompt({
                            title: '请输入动态口令', closeBtn: false, btn2: function () {
                                console.log(123);
                                layui.element.tabDelete("tabs", "settle");
                            }, formType: 0
                        }, function (pass, index) {
                            getSettleInfo(pass);
                            layer.close(index);

                        });
                    }
                });
            }


            //监听结算信息
            form.on('submit(submit-substitute-info)', function (data) {
                data.field.id = id;
                request.post("current-merchant/settle-configs", data.field, function (data) {
                    if (data.status === 200) {
                        layer.alert("更新成功");
                    } else {
                        layer.alert("更新失败");
                    }
                });
                return false;
            });

        });

    }


    return {init: init}
})