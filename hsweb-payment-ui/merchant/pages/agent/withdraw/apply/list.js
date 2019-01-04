define(["request"], function (request) {

    require(["css!pages/agent/withdraw/apply/list"]);
    function init(containerId) {
        require(["text!pages/agent/substitute/apply/list.html"], function (html) {
            $("#container-" + containerId).html($(html));
            layui.element.init();
            layui.use(['form', 'layedit', 'laydate', 'upload'], function () {
                var form = layui.form
                    , layer = layui.layer;

                form.render();

                form.on('submit(apply-withdraw)', function (data) {
                    var param = data.field;
                    var amount = param.transAmount;
                    if (amount){
                        param.transAmount = parseFloat(amount)*100;
                    }
                    layer.prompt({
                        title: '请输入动态口令', closeBtn: false, btn2: function () {
                        }, formType: 0
                    }, function (pass, index) {
                        request.post("merchant-withdraw/apply?verifyCode="+pass,param, function (e){
                            if (e.status === 200) {
                                layer.alert("申请成功");
                                layer.close(index);
                            } else {
                                layer.alert(e.message);
                                layer.close(index);
                            }
                        });
                    });
                    return false;

                });

                request.get("current-agent/balance",function (e) {
                    if (e.status===200){
                        $(".all-balance").html((parseFloat(e.result)/100).toFixed(2));
                        $(".available-balance").html((parseFloat(e.result)/100).toFixed(2));
                    }
                });

            });
        });
    }


    return {init: init}
});