define(["request"], function (request) {

    function init(containerId) {
        require(["text!pages/agent/merchant/list.html"], function (html) {
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
                elem: '#myMerchant'
                , url: '/current-agent/merchants'
                , cols: [[
                    {field: 'id', title: '商户ID'}
                    , {field: 'name', title: '商户名称', sort: true}
                    , {field: 'phone', title: '手机号', sort: true}
                    , {
                        field: 'status', title: '账户状态', templet: function (d) {
                            return d.status.text;
                        }
                    }
                    , {field: 'legalPersonName', title: '商户姓名'}
                    , {field: 'companyName', title: '公司名称'}
                    , {fixed: 'right', title: '操作', toolbar: '#merchantAction'}
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
                , where: {
                    "sorts[0].name": "createTime",
                    "sorts[0].order": "desc"
                }
            });


            table.on('tool(myMerchant)', function (obj) {
                var event = obj.event;
                var data = obj.data;

                if (event === 'merchant-detail') {
                    var status = data.status.text;
                    data.status = status;

                    layer.open({
                        id: 'merchantDetail' //防止重复弹出
                        , type: 1
                        , title: '商户详情'
                        , content: $("#merchantDetailForm")
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
                else if (event === 'setting-channel') {

                    var agentConfig;

                    function getAgentChannel(call) {
                        if (agentConfig) {
                            call(agentConfig);
                        } else {
                            request.get("current-merchant/channel-configs", function (e) {
                                call(agentConfig = e.result);
                            });
                        }
                    }

                    request.get("current-agent/channel-configs/" + data.id, function (e) {

                        var agentSupport = [];
                        getAgentChannel(function (agentChannel) {
                            for (var i = 0; i < agentChannel.length; i++) {
                                agentSupport.push(agentChannel[i].channel + agentChannel[i].transType.value);
                            }

                            layer.open({
                                id: 'merchantDetail' //防止重复弹出
                                , type: 1
                                , area: ['600px', '500px']
                                , title: '商户渠道'
                                , content: $("#merchantChannel")
                                , btn: ['保存配置', '关闭']
                                , btnAlign: 'c' //按钮居中
                                , shade: 0 //不显示遮罩
                                // , closeBtn: 1
                                , success: function () {
                                    renderChannel(data.id);
                                }
                                , yes: function () {
                                    $("#save-channel").click();
                                }
                                , btn2: function () {
                                }
                            });

                            function renderChannel(id) {
                                $("#merchantChannel").html("");
                                var data = e.result;
                                var html = "<input type='text' hidden='hidden' name='id' value='" + id + "'>";

                                // 商户渠道
                                var merchantChannel = [];
                                for (key in data) {
                                    var title = data[key].channel + data[key].transType.value;
                                    merchantChannel.push(title);
                                }

                                //代理渠道
                                for (key in agentChannel) {
                                    var agent = agentChannel[key].channel + agentChannel[key].transType.value;
                                    var result = merchantChannel.indexOf(agent);
                                    var title = agentChannel[key].channelName;

                                    if (result > -1) {
                                        html += '<div class="layui-inline" style="margin-top: 20px;"><input type="checkbox" name="' + agent + '" title="' + title + '" checked></div>';

                                    } else {
                                        html += '<div class="layui-inline" style="margin-top: 20px;"><input type="checkbox" name="' + agent + '" title="' + title + '"></div>';
                                    }
                                }


                                var btn = '<div hidden="hidden"><button class="layui-btn" id="save-channel" lay-submit=""\n' +
                                    '                                lay-filter="save-channel" type="button">保存配置\n' +
                                    '                        </button></div>\n';
                                $("#merchantChannel").append(html + btn);
                                form.render();
                            }

                        });


                    });
                }
                else if (event === 'setting-charge') {
                    // if (data.status.value === "ACTIVE") {
                    layer.open({
                        id: 'merchantDetail' //防止重复弹出
                        , type: 1
                        , area: ['600px', '500px']
                        , title: '设置费率'
                        , content: $("#chargeForm")
                        , btn: ['保存费率', '关闭']
                        , btnAlign: 'c' //按钮居中
                        , shade: 0 //不显示遮罩
                        , closeBtn: 0
                        , success: function () {
                            renderChargeForm(data.id);
                        }
                        , yes: function () {
                            $("#create-merchant-btn").click();
                            // $("#chargeForm").hide();
                            // $("#render-charge-form").remove()
                        }
                        , btn2: function () {
                            // layer.closeAll();
                            $("#chargeForm").hide();
                            $("#chargeForm").html("");
                        }
                    });
                }
                // else {
                //     layer.alert("账户状态异常，不可设置费率");
                // }
                // }
            });

            $(".add-merchant").on('click', function () {
                var menu = {
                    id: "add-merchant",
                    name: "新增商户",
                    url: "./pages/agent/merchant/save"
                };
                loadMenu(menu);
            });


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

            function getAgentChannel(call) {
                if (agentConfig) {
                    call(agentConfig);
                } else {
                    request.get("current-merchant/channel-configs", function (e) {
                        call(agentConfig = e.result);
                    });
                }
            }


            function renderChargeForm(e) {
                $("#chargeForm").html("");
                var id = '<input type="text" hidden="hidden" name="id" value="' + e + '">';
                $("#chargeForm").append(id);

                var supportConfig;

                function getSupportChannel(call) {
                    if (supportConfig) {
                        call(supportConfig);
                    } else {
                        request.get("current-agent/channel-configs/" + e, function (e) {
                            call(supportConfig = e.result);
                        });
                    }
                }

                //渲染渠道费率
                request.get('current-agent/merchant/rate-configs/' + e, function (e) {

                    getSupportChannel(function (support) {


                        var end = '<div class="layui-form-item" hidden="hidden" >\n' +
                            '                    <div class="layui-input-block" style="align-content: center">\n' +
                            '                        <button class="layui-btn" id="create-merchant-btn" lay-submit=""\n' +
                            '                                lay-filter="create-merchant-btn" type="button">保存配置\n' +
                            '                        </button>\n' +
                            '                    </div>\n' +
                            '                </div></div>';

                        var content = '<div id="render-charge-form">';
                        var data = e.result;


                        //商户支持的渠道
                        var channels = {};
                        for (var j = 0; j < support.length; j++) {
                            var supportChannel = support[j].channel;
                            channels[supportChannel] = support[j];
                        }


                        for (var i = 0; i < data.length; i++) {

                            var rate = data[i].channelName + data[i].transType.value;

                            if (data[i].enable) {
                                content += '<div class="layui-form-item"><div class="layui-inline">\n' +
                                    '                    <label class="layui-form-label"  style="width: 250px">' + data[i].channelName + '(' + data[i].rateType.text + ')' + '：</label>\n' +
                                    '                    <div class="layui-input-inline" style="width: 150px">\n' +
                                    '                        <input style="" required type="text" name="' + data[i].channelName + '" lay-verify="title"\n' +
                                    '                               class="layui-input" value="' + data[i].rate + '" placeholder="不低于' + (data[i].rateType.value === 'FIXED' ? ((data[i].memo) / 100).format() : data[i].memo) + '">\n' +
                                    '                    </div>' +
                                    '<div class="layui-input-inline" id="' + rate + '" style="width: 65px">' +
                                    '<input type="checkbox" name="' + data[i].transType.value+data[i].channel + "-status" + '" value="' + data[i].transType.value+"-"+data[i].channel + '"  checked="" lay-skin="switch" lay-filter="switchTest" lay-text="开启|关闭"></div>' +
                                    '</div></div>\n' +
                                    '                </div>';
                            }
                            else {
                                content += '<div class="layui-form-item"><div class="layui-inline">\n' +
                                    '                    <label class="layui-form-label"  style="width: 250px">' + data[i].channelName + '(' + data[i].rateType.text + ')' + '：</label>\n' +
                                    '                    <div class="layui-input-inline" style="width: 150px">\n' +
                                    '                        <input style="" required type="text" name="' + data[i].channelName + '" lay-verify="title"\n' +
                                    '                               class="layui-input" value="' + data[i].rate + '" placeholder="不低于' + (data[i].rateType.value === 'FIXED' ? ((data[i].memo) / 100).format() : data[i].memo) + '">\n' +
                                    '                    </div>' +
                                    '<div class="layui-input-inline" id="' + rate + '" style="width: 65px">' +
                                    '<input type="checkbox" name="' + data[i].transType.value+data[i].channel + "-status" + '" value="' + data[i].transType.value+"-"+data[i].channel + '"  lay-skin="switch" lay-filter="switchTest" lay-text="开启|关闭"></div>' +
                                    '</div></div>\n' +
                                    '                </div>';

                            }
                        }

                        $("#chargeForm").append(content + end);

                        form.render();

                    });

                    form.render();
                });

            }


            form.on('submit(save-channel)', function (data) {
                var checkChannel = [];
                for (var key in data.field) {
                    checkChannel.push(key);
                }
                getAgentChannel(function (channels) {
                    //data 是开启的渠道。从channels里面筛选出开启的渠道。
                    var map = [];
                    for (i in channels) {
                        var key = channels[i].channel + channels[i].transType.value;
                        var channel = {
                            key: key,
                            value: channels[i]
                        };

                        map.push(channel);
                    }

                    //选中的渠道
                    var confirmChannel = [];
                    for (var i = 0; i < map.length; i++) {
                        var r = checkChannel.indexOf(map[i].key);
                        if (r > -1) {
                            confirmChannel.push(map[i].value);
                        }
                    }

                    request.patch("current-agent/merchant/config/SUPPORTED_CHANNEL/" + data.field.id, confirmChannel, function (e) {
                        if (e.status === 200) {
                            layer.alert("保存成功！");
                        } else {
                            layer.alert("保存失败！");
                        }
                    });
                })
            });

            form.on('submit(create-merchant-btn)', function (data) {

                var checkChannel = [];
                for (var key in data.field) {
                    if (key.endsWith("-status")) {
                        checkChannel.push(data.field[key]);
                    }
                }

                getAgentConfig(function (configs) {
                    var config = configs.data;
                    var merchantConfigs = [];
                    for (var key in config) {
                        var name = config[key].channelName;
                        if (data.field[name]) {
                            var conf = $.extend(true, {}, config[key]);
                            conf.rate = data.field[name];
                            conf.transType = (config[key].transType || {}).value;
                            conf.rateType = (config[key].rateType || {}).value;


                            if (conf.channel===undefined){
                                conf.channel="undefined";
                            }
                            if (checkChannel.indexOf(conf.transType+"-"+conf.channel)>-1){
                                conf.enable=true;
                            } else {
                                conf.enable=false;
                            }
                            merchantConfigs.push(conf);
                        }
                    }
                    request.patch("current-agent/merchant/config/RATE_CONFIG/" + data.field.id, JSON.stringify(merchantConfigs), function (e) {
                        if (e.status === 200) {
                            layer.alert("保存成功!");
                        } else {
                            layer.alert("保存失败!" + e.message);
                        }
                    });
                });
            });
        });
    }


    return {init: init}
});