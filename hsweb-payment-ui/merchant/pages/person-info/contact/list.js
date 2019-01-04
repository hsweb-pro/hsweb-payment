define(["request"], function (request) {

    function init(containerId) {
        require(["text!pages/person-info/contact/list.html"], function (html) {
            $("#container-" + containerId).html($(html));
            layui.element.init();
            loadData()
        });
    }


    function loadData() {
        var id = "";
        layui.use(['form', 'layedit', 'laydate', 'upload'], function () {
            var form = layui.form
                , layer = layui.layer;

            request.get("current-merchant/base-info", function (data) {
                form.val('merchant-info', data.result);
                form.val('merchant-contact', data.result);

                id = data.result.id;
            });


            //监听提交联系信息
            form.on('submit(submit-merchant-contact)', function (data) {
                data.field.id = id;
                request.put("current-merchant/base-info", data.field, function (data) {
                    if (data.status === 200) {
                        layer.alert("更新成功");
                    } else {
                        layer.alert("更新失败");
                    }
                });
                return false;
            });


            //商户修改密码
            form.on('submit(update-password-btn)', function (data) {
                var param = data.field;
                layer.prompt({
                    title: '请输入动态口令', closeBtn: false, btn2: function () {
                    }, formType: 0
                }, function (pass, index) {
                    request.post("current-merchant/update-password?verifyCode=" + pass, param, function (e) {
                        if (e.status === 200) {
                            layer.alert("修改成功");
                            layer.close(index);
                        } else {
                            layer.alert(e.message);
                            layer.close(index);
                        }
                    });
                });
                return false;
            });

        });
    }


    return {init: init}
});