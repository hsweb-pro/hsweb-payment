importResource("/admin/css/common.css");
require(["css!pages/form/designer-drag/defaults"]);

var ladder_type_data = [{text: '范围', id: 'range'}, {text: '大于', id: 'gt'}];
var rate_type_data = [{text: '固定收取', id: 'FIXED'}, {text: '百分比', id: 'PERCENT'}];
var ladder_type_map = {};
var rate_type_map = {};
var rates = ["fixed", "percent", "ladder"];
var rateConfigGetter = {
    "fixed": {
        getConfig: function () {
            var rate = mini.getbyName("value-fixed").getValue();

            return {
                rate: Math.floor(parseFloat(rate) * 100) + ""
            }
        },
        setConfig: function (rate) {
            mini.getbyName("value-fixed").setValue((parseFloat(rate) / 100).toFixed(2))
        }, clean: function (disable) {
            mini.getbyName("value-fixed").setValue("");
            if (disable) {
                mini.getbyName("value-fixed").setEnabled(false);
            }
        }
    },
    "percent": {
        getConfig: function () {
            return {
                rate: mini.getbyName("value-percent").getValue() + ""
            }
        },
        setConfig: function (rate) {
            mini.getbyName("value-percent").setValue(rate)
        }, clean: function (disable) {
            mini.getbyName("value-percent").setValue("");
            if (disable) {
                mini.getbyName("value-percent").setEnabled(false);
            }
        }
    },
    "ladder": {
        getConfig: function () {
            function parseData(data) {
                var newData = [];
                $(data).each(function () {
                    var values = this.values.split("-");
                    for (var i = 0; i < values.length; i++) {
                        values[i] = Math.floor(parseFloat(values[i]) * 100);
                    }
                    var rate = this.rate;
                    if (this.rateType === 'FIXED') {
                        rate = Math.floor(parseFloat(rate) * 100)
                    }
                    var conf = {
                        type: this.type,
                        origValues: this.values,
                        rate: rate,
                        rateType: this.rateType,
                        values: values
                    };
                    newData.push(conf);
                });
                return JSON.stringify(newData);
            }

            return {
                rate: parseData(mini.clone(mini.get("value-ladder").getData())),
                chargeTimeUnit: mini.getbyName('chargeTimeUnit').getValue(),
                chargeInterval: mini.getbyName("chargeInterval").getValue()
            }
        },
        clean: function (disable) {
            mini.get("value-ladder").setData([]);

            if (disable) {
                mini.get("value-ladder").setEnabled(false);
            }
        },
        setConfig: function (rate, all) {
            var conf = JSON.parse(rate);
            if (conf) {
                $(conf).each(function () {
                    this.values = this.origValues;
                    if (this.rateType === 'FIXED') {
                        this.rate = (this.rate / 100).toFixed(2);
                    }
                })
            }
            mini.getbyName('chargeTimeUnit').setValue(all.chargeTimeUnit);
            mini.getbyName('chargeInterval').setValue(all.chargeInterval);
            mini.get("value-ladder").setData(conf);
        }
    }
}

var timeUnitData = [
    {id: "DAY", text: '天'},
    {id: "WEEK", text: '周'},
    {id: "MONTH", text: '月'},
    {id: "YEAR", text: '年'}
];
var timeUnitDataMap = {};


importMiniui(function () {
    $(timeUnitData).each(function () {
        timeUnitDataMap[this.id] = this;
    });
    mini.parse();
    $(ladder_type_data).each(function () {
        ladder_type_map[this.id] = this.text;
    });
    $(rate_type_data).each(function () {
        rate_type_map[this.id] = this.text;
    });
    var mainForm;
    require(["request", "miniui-tools", 'pages/form/designer-drag/parser', "message"], function (request, tools, FormParser, message) {
        var dataId = request.getParameter("id");
        $(".close").on("click", function () {
            tools.closeWindow();
        })
        require(["text!save.hf", "pages/form/designer-drag/components-default"], function (config) {
            mainForm = new FormParser(JSON.parse(config));
            var formEl = $("#basic-info");
            mainForm.render(formEl);

            $("#basic-info .dynamic-form").append($("#tabsTemplate").html());
            mini.parse();
            if (dataId) {
                loadData(dataId);
            }
            var agentId = mini.getbyName("parentId");
            agentId.on("buttonclick", function (e) {
                tools.openWindow("admin/agent/selector.html", "选择代理", "800", "600", function (agent) {
                    if (agent.id) {
                        if (dataId === agent.id) {
                            message.showTips("不能选择自己为上级代理", "danger")
                            return;
                        }
                        agentId.setText(agent.name);
                        agentId.setValue(agent.id);
                    }
                })
            });
            var rateGrid = mini.get("value-ladder");
            var channelGrid = mini.get("channel-grid");
            var tabs = mini.get("config-tabs");
            tabs.setEnabled(false);
            var channelRateList = mini.get("rate-channel-list");


            var limitGrid = mini.get("limit-grid");
            limitGrid.getColumn("timeUnit").renderer = function (e) {
                if (!e.value) {
                    return "";
                }
                return (timeUnitDataMap[e.value.value ? e.value.value : e.value] || {text: e.value}).text;
            };
            limitGrid.getColumn("limit").renderer = function (e) {
                return mini.formatNumber(parseFloat(e.value), "#,0.00元");
            };
            limitGrid.getColumn("action").renderer = function (e) {
                return tools.createActionButton("删除", "icon-remove", function () {
                    e.sender.removeRow(e.record);
                })
            };

            function initLimitEditor(row) {
                var limits = mini.clone(row.tradingLimits);

                $(limits).each(function () {
                    this.limit = (this.limit / 100).toFixed(2);
                });
                limitGrid.setData(limits);
                $('.save-limit')
                    .unbind('click')
                    .on('click', function () {
                        var list = mini.clone(limitGrid.getData());
                        $(list).each(function () {
                            this.limit = (this.limit * 100).toFixed(0);
                        });
                        row.tradingLimits = list;
                        channelGrid.updateRow(row, row);
                        mini.get('limitWindow').hide();
                    })
                mini.get('limitWindow').show();
            }

            channelGrid.getColumn("limit").renderer = function (e) {
                return tools.createActionButton("限额配置", "icon-edit", function () {
                    initLimitEditor(e.record);
                });
            };


            tabs.on("beforeactivechanged", function (e) {
                if (!checkCanSaveConfig()) {
                    e.cancel = true;
                }
            });
            tabs.on("activechanged", function (e) {

                var tab = e.tab;
                if (tab.name === 'rate-config') {
                    initRate();
                }
                $(".dynamic-form").scrollTop($(window.document).height() + 200)
            });

            var channelRateConfig = {};
            var nowSelectedRateChannel;
            channelRateList.on("nodeclick", function (e) {
                saveRate();
                var node = nowSelectedRateChannel = e.node;

                var conf;
                if (node.type === 'channel') {
                    conf = channelRateConfig[node.transType + "-" + (node.id || '')];
                } else {
                    conf = channelRateConfig[node.id + "-"];
                }
                // if (conf) {
                loadRate(conf || {});
                // }
                // } else {
                //     nowSelectedRateChannel = undefined;
                // }
            });

            function loadRate(conf) {
                var rateType = conf.rateType ? conf.rateType.toLowerCase() : null;
                //先清空全部配置
                $(rates).each(function () {
                    var type = this + "";
                    rateConfigGetter[type].clean(true);
                    mini.getbyName("rate-" + type).setChecked(false);
                    mini.getbyName("rate-" + type).setValue("false");

                });
                //加载平台收费
                if (rateType) {
                    mini.getbyName("rate-" + rateType).setChecked(true);
                    mini.getbyName("rate-" + rateType).setValue("true");
                    mini.getbyName("rate-" + rateType).fire("checkedchanged");
                    rateConfigGetter[rateType].setConfig(conf.rate, conf)
                }
            }

            function saveRate() {
                if (nowSelectedRateChannel) {
                    var rateType;
                    $(rates).each(function () {
                        if ((mini.getbyName("rate-" + this).getValue() + "") === 'true') {
                            rateType = (this + "");
                        }
                    });
                    var rate = rateType ? rateConfigGetter[rateType].getConfig() : {};
                    var id = nowSelectedRateChannel.type === 'channel' ? nowSelectedRateChannel.transType + "-" + (nowSelectedRateChannel.id)
                        : nowSelectedRateChannel.id + "-";
                    if (!rateType) {
                        delete  channelRateConfig[id];
                        return;
                    }
                    // console.log(nowSelectedRateChannel)
                    var conf = {
                        channelName: nowSelectedRateChannel.text,
                        channel: nowSelectedRateChannel.type === 'channel' ? nowSelectedRateChannel.id : null,
                        transType: nowSelectedRateChannel.type === 'channel' ? nowSelectedRateChannel.transType : nowSelectedRateChannel.id,
                        rate: rate.rate,
                        chargeInterval: rate.chargeInterval,
                        chargeTimeUnit: rate.chargeTimeUnit,
                        rateType: rateType ? rateType.toUpperCase() : null
                    };

                    channelRateConfig[id] = conf;
                }
            }

            function initRate() {
                var channels = channelGrid.getSelecteds();
                if (!channels || channels.length === 0) {
                    message.showTips("请先进行渠道配置")
                    tabs.activeTab(tabs.getTab('channel-config'));
                }
                var tmp = {};
                var dataList = [];
                var providerTmp = {};
                $(channels).each(function () {
                    var newProvider = typeof(providerTmp[this.channel]) === 'undefined';

                    var providers = providerTmp[this.channel] || {
                        id: this.channel,
                        channelProvider: this.channelProvider,
                        channelProviderName: this.channelProviderName,
                        transType: this.transType,
                        type: "channel",
                        text: this.channelName,
                        channel: this.channel,
                        channelName: this.channelName,
                        children: []
                    };
                    providerTmp[this.channel] = providers;
                    var transType = tmp[this.transType] || {
                        id: this.transType,
                        transType: this.transType,
                        text: this.transTypeName,
                        children: []
                    };
                    if (newProvider) {
                        transType.children.push(providers);
                    }

                    tmp[this.transType] = transType;
                    this.id = this.transType + "_" + this.channelProvider + "_" + this.channel;
                    this.text = this.channelProviderName;
                    //不配置具体供应商费率
                    // providers.children.push(this);
                });
                for (var transType in tmp) {
                    dataList.push(tmp[transType]);
                }
                mini.get("rate-channel-list").setData(dataList);
                mini.get("rate-channel-list").expandAll();
            }

            var dataId = request.getParameter("id");

            tools.initGrid(channelGrid);
            channelGrid.setDataField("result");
            channelGrid.setUrl(API_BASE_PATH + "payment/channels");
            channelGrid.load();
            channelGrid.on("load", function () {
                channelGrid.mergeColumns(["transType", "channel", "channelName", "channelProviderName"]);
                if (dataId) {
                    loadData()
                }
            });
            rateGrid.getColumn("type").renderer = function (e) {
                var value = e.value;
                return ladder_type_map[value] || value;
            };

            rateGrid.getColumn("rateType").renderer = function (e) {
                var value = e.value;
                return rate_type_map[value] || value;
            };
            rateGrid.getColumn("action").renderer = function (e) {
                return tools.createActionButton("删除", "icon-remove", function () {
                    e.sender.removeNode(e.record);
                })
            };

            $(rates).each(function () {
                var rate = this;
                mini.getbyName("rate-" + rate)
                    .on("checkedchanged", function (r) {
                        return function () {
                            $(rates).each(function () {
                                if (r + "" !== this + "") {
                                    mini.getbyName("rate-" + this).setValue("false");
                                    if (mini.getbyName("value-" + this)) {
                                        mini.getbyName("value-" + this).setEnabled(false);
                                    } else if (mini.get("value-" + this)) {
                                        mini.get("value-" + this).setEnabled(false);
                                    }
                                } else {
                                    if (mini.getbyName("value-" + this)) {
                                        mini.getbyName("value-" + this).setEnabled(true);
                                    } else if (mini.get("value-" + this)) {
                                        mini.get("value-" + this).setEnabled(true);
                                    }
                                }

                            });
                        }
                    }(rate))
            });


            var password = Math.round(Math.random() * 100000) + "";

            function loadData() {
                var loading = message.loading("加载中...");
                request.get("manager/agent/" + dataId, function (response) {
                    loading.hide();
                    if (response.status === 200) {
                        response.result.password = password;
                        mainForm.setData(response.result);
                        mini.getbyName("username").setEnabled(false);
                        if (response.result.parentId) {
                            request.createQuery("manager/agent/no-paging")
                                .includes(["name","id"])
                                .where("id", response.result.parentId)
                                .exec(function (agentResponse) {
                                    if (agentResponse.status === 200 && agentResponse.result.length > 0) {
                                        mini.getbyName("parentId").setValue(response.result.parentId);
                                        mini.getbyName("parentId").setText(agentResponse.result[0].name)
                                    }
                                });
                        }
                        initConfig();
                    }
                })
            }

            var configs = {
                // "SECRET_KEY": {
                //     setConfig: function (e) {
                //         mini.getbyName("SECRET_KEY.value").setValue(e);
                //     }
                // },
                "SUPPORTED_CHANNEL": {
                    setConfig: function (e) {
                        var list = JSON.parse(e);
                        if (list) {
                            $(list).each(function () {
                                var conf = this;
                                channelGrid.findRow(function (row) {
                                    // console.log(row)
                                    if (row.channel === conf.channel
                                        &&row.channelProvider === conf.channelProvider
                                        && row.transType === conf.transType) {
                                        row.tradingLimits = conf.tradingLimits;
                                        row.selectorRule = conf.selectorRule;
                                        row.ruleConfig = conf.ruleConfig;
                                        channelGrid.updateRow(row, row);
                                        channelGrid.select(row);
                                    }
                                })
                            })
                        }
                    },
                    getConfig: function () {
                        var channels = channelGrid.getSelecteds();
                        var list = [];
                        $(channels).each(function () {
                            list.push({
                                transType: this.transType,
                                channelName: this.channelName,
                                tradingLimits: this.tradingLimits,
                                selectorRule: this.selectorRule,
                                ruleConfig: this.ruleConfig,
                                channel: this.channel,
                                channelProvider:this.channelProvider
                            })
                        });
                        return JSON.stringify(list);
                    }
                },
                "RATE_CONFIG": {
                    setConfig: function (e) {
                        var conf = JSON.parse(e);
                        $(conf).each(function () {
                            channelRateConfig[this.transType + "-" + (this.channel || '')] = this;
                        });
                    },
                    getConfig: function () {
                        saveRate();
                        var list = [];
                        for (var key in channelRateConfig) {
                            list.push(channelRateConfig[key]);
                        }
                        return JSON.stringify(list);
                    }
                }
            };

            $(".save-channel-config").on("click", function () {
                if (checkCanSaveConfig()) {
                    var config = configs.SUPPORTED_CHANNEL.getConfig();
                    request.patch("manager/merchant/" + dataId + "/config/SUPPORTED_CHANNEL", config, function (response) {
                        if (response.status === 200) {
                            message.showTips("保存成功");
                        } else {
                            message.showTips(response.message, "danger")
                        }
                    });
                }
            });

            $(".save-rate-config").on("click", function () {
                if (checkCanSaveConfig()) {
                    var config = configs.RATE_CONFIG.getConfig();
                    request.patch("manager/merchant/" + dataId + "/config/RATE_CONFIG", config, function (response) {
                        if (response.status === 200) {
                            message.showTips("保存成功");
                        } else {
                            message.showTips(response.message, "danger")
                        }
                    });
                }
            });

            function checkCanSaveConfig() {
                if (!dataId) {
                    message.showTips("请先保存", "danger");
                    return false;
                }
                return true;
            }

            function initConfig() {
                tabs.setEnabled(true);
                request.get("manager/merchant/" + dataId + "/configs", function (response) {
                    if (response.status === 200) {
                        for (var key in response.result) {
                            var config = configs[key];
                            if (config) {
                                config.setConfig(response.result[key]);
                            }
                        }
                    }
                })
            }

            $(".save").on("click", function () {
                var data = mainForm.getData(true);
                if (!data) {
                    return;
                }
                var loading = message.loading("保存中...");
                if (data.password === password) {
                    delete data.password;
                    delete data.username;
                }
                var api = "manager/agent/" + (dataId || "");
                var method = dataId ? request.put : request.post;

                method(api, data, function (response) {
                    loading.hide();
                    if (response.status === 200) {
                        message.showTips("保存成功");
                        dataId = response.result.id;
                        initConfig();
                    } else {
                        message.showTips(response.message);
                    }
                });

            })
        });


    });

});