define(["request"], function (request) {

    function init(containerId) {
        require(["text!pages/person-info/payee/save.html"], function (html) {
            $("#container-" + containerId).html($(html));
            layui.element.init();
            loadData();
        });
    }

    function loadData() {
        layui.use(['form', 'layedit', 'laydate', 'upload','element'], function () {
            var form = layui.form;
            form.render();

            request.get("current-agent/payee-config",function (data) {
                if (data.status===200){
                    var types = data.result;
                    for (var i = 0; i < types.length; i++) {
                        var option = types[i];
                        var inputs = option.properties;
                        var type = '<input type="text" name="payeeType" value="'+option.payeeType+'" class="layui-input" style="display: none">';

                        var end = ' <div class="layui-form-item">\n' +
                            '                            <div class="layui-input-block">\n' +
                            '                                <button class="layui-btn" lay-submit="" lay-filter="submit-payee-info">立即提交' +
                            '                                </button>\n' +
                            '                            </div>\n' +
                            '                        </div></div></form>';
                        if (i===0){
                            $(".payee-type").append("<li class='layui-this'>" + option.name + "</li>");
                            var div = ' <div class="layui-tab-item layui-show" action="" lay-filter="payee-info" style="margin-top: 30px"><form class="layui-form" action="" lay-filter="payee-info">';
                            for (var j = 0; j <(option.properties).length ; j++) {
                                div += ' <div class="layui-form-item">\n' +
                                    '    <label class="layui-form-label">'+inputs[j].name+'</label>\n' +
                                    '    <div class="layui-input-block">\n' +
                                    '      <input type="text" name="'+inputs[j].property+'" required  lay-verify="required" autocomplete="off" class="layui-input">\n' +
                                    '    </div>\n' +
                                    ' </div>';

                            }

                            $(".payee-form").append(div+type+end);
                        }
                        else {
                            $(".payee-type").append("<li>" + option.name + "</li>");
                            var div = ' <div class="layui-tab-item" action="" lay-filter="payee-info" style="margin-top: 30px"> <form class="layui-form" action="" lay-filter="payee-info">';
                            for (var j = 0; j <(option.properties).length ; j++) {
                                div += ' <div class="layui-form-item">\n' +
                                    '    <label class="layui-form-label">'+inputs[j].name+'</label>\n' +
                                    '    <div class="layui-input-block">\n' +
                                    '      <input type="text" name="'+inputs[j].property+'" required  lay-verify="required" autocomplete="off" class="layui-input">\n' +
                                    '    </div>\n' +
                                    ' </div>';
                            }
                            $(".payee-form").append(div+type+end);
                        }
                    }


                }
            });

            //监听收款信息
            form.on('submit(submit-payee-info)', function (data) {
                var param = {
                    payeeType:data.field.payeeType,
                    payeeInfoJson:JSON.stringify(data.field)
                };
                request.post("current-agent/payee-config",param,function (e) {
                    if (e.status ===200){
                        console.log("保存成功");
                    } else {
                        console.log("保存失败！");
                    }
                });
                return false;
            });

        });
    }


    return {init: init}
});