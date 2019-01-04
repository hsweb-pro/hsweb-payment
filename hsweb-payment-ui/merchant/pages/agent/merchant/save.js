define(["request"], function (request) {

    function init(containerId) {
        require(["text!pages/agent/merchant/save.html"], function (html) {
            $("#container-" + containerId).html($(html));
            layui.element.init();
            loadData();
        });
    }

    var agentConfig;

    function getAgentConfig(call) {
        if (agentConfig) {
            call(agentConfig);
        } else {
            request.get("current-merchant/rate-configs", function (e) {
                call(agentConfig = e.result);
            });
        }
    }

    function loadData() {


        layui.use(['form', 'layedit', 'laydate', 'upload', 'element'], function () {
            var form = layui.form;
            form.render();


            form.on('submit(create-merchant-btn)', function (data) {

                var loading = layer.open({
                    shadeClose: false
                    , type: 3
                    , content: '...'
                });

                request.post("current-agent/create-merchant", data.field, function (e) {
                    if (e.status === 200) {
                        var merchant = e.result;
                        getAgentConfig(function (configs) {
                            var config = configs.data;
                            var merchantConfigs = [];
                            for (var key in config) {
                                var name = config[key].channelName;
                                if (data.field[name]) {
                                    config[key].rate = data.field[name];
                                    config[key].transType = (config[key].transType || {}).value;
                                    config[key].rateType = (config[key].rateType || {}).value;
                                    merchantConfigs.push(config[key]);
                                }
                            }
                            request.patch("current-agent/merchant/config/RATE_CONFIG/" + merchant.id, JSON.stringify(merchantConfigs), function (e) {
                                if (e.status === 200) {
                                    layer.alert("保存成功!");
                                    $("#create-merchant input").val("");
                                } else {
                                    layer.alert("保存失败!" + e.message);
                                }
                                layer.close(loading);
                            });
                        });

                        //保存费率
                        // layer.alert("保存成功");
                    } else {
                        layer.close(loading);
                        layer.alert("保存失败!" + e.message);
                    }
                });


                return false;

            });

            renderChargeForm();
        });


        function renderChargeForm() {
            $("#initChargeForm").html("");
            request.get('current-merchant/rate-configs', function (e) {
                var content = '<div id="render-charge-form">';
                var data = e.result.data;
                for (var i = 0; i < data.length; i++) {
                    var transType = data[i].transType.value;
                    if (!(transType === 'WITHDRAW' || transType === 'SUBSTITUTE')) {
                        content += '<div class="layui-form-item">\n' +
                            '                    <label class="layui-form-label"  style="width: 250px">' + data[i].channelName + '(' + data[i].rateType.text + ')' + '：</label>\n' +
                            '                    <div class="layui-input-block">\n' +
                            '                        <input style="width: 40%" required type="text" name="' + data[i].channelName + '" lay-verify="title"\n' +
                            '                               class="layui-input" placeholder="不低于' + (data[i].rateType.value === 'FIXED' ? ((data[i].rate) / 100).format() : data[i].rate) + '">\n' +
                            '                    </div>\n' +
                            '                </div>';
                    }
                }
                $("#initChargeForm").append(content);
            });
        }
    }

    return {init: init}
});