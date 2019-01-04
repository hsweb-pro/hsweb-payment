importResource("/admin/css/common.css");
importResource("/admin/form/designer-drag/defaults.css");
var timeUnitData = [
    {id: "SINGLE", text: '笔'},
    {id: "DAY", text: '天'},
    {id: "WEEK", text: '周'},
    {id: "MONTH", text: '月'},
    {id: "YEAR", text: '年'}
];
var timeUnitDataMap = {};

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
                rate: parseData(mini.clone(mini.get("value-ladder").getData()))
            }
        },
        clean: function (disable) {
            mini.get("value-ladder").setData([]);

            if (disable) {
                mini.get("value-ladder").setEnabled(false);
            }
        },
        setConfig: function (rate) {
            var conf = JSON.parse(rate);
            if (conf) {
                $(conf).each(function () {
                    this.values = this.origValues;
                    if (this.rateType === 'FIXED') {
                        this.rate = (this.rate / 100).toFixed(2);
                    }
                })
            }
            mini.get("value-ladder").setData(conf);
        }
    }
}


importMiniui(function () {
    $(timeUnitData).each(function () {
        timeUnitDataMap[this.id] = this;
    });
    $(timeUnitData).each(function () {
        timeUnitDataMap[this.id] = this;
    });
    $(ladder_type_data).each(function () {
        ladder_type_map[this.id] = this.text;
    });
    $(rate_type_data).each(function () {
        rate_type_map[this.id] = this.text;
    });
    mini.parse();

    var allChannelTree = mini.get("all-channel-list");
    var configGrid = mini.get("config-grid");
    var nowSelectedChannel;
    var allChannels = {};

    require(['request', 'message', 'miniui-tools'], function (request, message, tools) {

        var selector = request.getParameter("selector") === 'true';

        window.actionClick = function () {
            if (!nowSelectedChannel) {
                message.showTips("请先选择要配置的渠道", "danger");
                return;
            }
            configGrid.addNode({
                channelName: nowSelectedChannel.channelName,
                channel: nowSelectedChannel.channel,
                transType: nowSelectedChannel.transType,
                channelProvider: nowSelectedChannel.channelProvider,
                channelProviderName: nowSelectedChannel.channelProviderName
            })
        };
        loadAllChannel();


        configGrid.getColumn('accountNo').renderer = function (e) {
            return (e.value || '') + tools.createActionButton("修改", "icon-edit", function () {
                tools.openWindow("admin/channel/settle/list.html?selector=true", "选择结算账户", 800, 600, function (acc) {
                    if (acc && acc.accountNo) {
                        e.record.accountNo = acc.accountNo;
                        e.sender.updateRow(e.record);
                    }
                })
            })
        }

        function loadAllChannel() {
            request.get("payment/channels", function (response) {
                if (response.status === 200) {
                    var tmp = {};
                    var list = [];
                    var providerTmp = {};
                    $(response.result).each(function () {
                        var newProvider = typeof(providerTmp[this.channel]) === 'undefined';

                        var providers = providerTmp[this.channel] || {
                            id: this.transType + "-" + this.channel,
                            transType: this.transType,
                            channel: this.channel,
                            channelName: this.channelName,
                            text: this.channelName,
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
                        providers.children.push(this);
                        allChannels[this.transType + '-' + this.channelProvider + "-" + this.channel] = this;
                    });
                    for (var transType in tmp) {
                        list.push(tmp[transType]);
                    }
                    allChannelTree.setData([{id: "all", text: "全部渠道", children: list}]);
                    loadConfig({})
                }
            })
        }


        allChannelTree.on("nodeclick", function (e) {
            var node = e.node;
            if (node.properties) {
                nowSelectedChannel = node;
            } else {
                nowSelectedChannel = undefined;
            }
            loadConfig(node);
        });

        function loadConfig(node) {
            configGrid.loading("加载中");
            request.createQuery("channel/config/no-paging")
                .where("channel", node.channel)
                .and("transType", node.transType)
                .and("channelProvider", node.channelProvider)
                .exec(function (response) {
                    configGrid.unmask();
                    if (response.status === 200) {
                        configGrid.setData(response.result);
                    } else {
                        message.showTips(response.message);
                    }
                })
        }

        function doSave(row) {
            configGrid.loading("保存中...");
            if (nowSelectedChannel) {
                row.transType = nowSelectedChannel.transType;
                row.channel = nowSelectedChannel.channel;
                row.channelName = nowSelectedChannel.channelName;
                row.channelProvider = nowSelectedChannel.channelProvider;
                row.channelProviderName = nowSelectedChannel.channelProviderName;
            }
            request.patch("channel/config", row, function (e) {
                configGrid.unmask();
                if (e.status === 200) {
                    row.id = e.result;
                    configGrid.updateRow(row, row);
                } else {
                    message.showTips(e.message, "danger");
                }
            });
        }

        if (selector) {
            $(["config", "rateConfig", "limitConfig", "status"]).each(function () {
                configGrid.hideColumn(this + "")
            })

        }
        var bindGrid = mini.get("bind-grid");
        var loadBindConfig;

        bindGrid.getColumn("action").renderer = function (e) {
            return tools.createActionButton("删除", "icon-remove", function () {
                message.confirm("确认删除?", function () {
                    var loading = message.loading("删除中");
                    request['delete']('channel/bind/' + e.record.id, function (resp) {
                        loading.hide();
                        loadBindConfig();
                        if (resp.status !== 200) {
                            message.showTips("删除失败:" + resp.message)
                        }
                    });
                })
            })
        };
        //表格相关
        configGrid.getColumn("action").renderer = function (e) {
            var list = [];
            if (selector) {
                list.push(tools.createActionButton("选择", "icon-ok", function () {
                    tools.closeWindow(e.record);
                }));
            } else {
                list.push(tools.createActionButton("保存", "icon-save", function () {
                    doSave(e.record);
                }));
                if (e.record.id) {
                    list.push(tools.createActionButton("商户独占", "icon-user", function () {
                        mini.get('bindWindow').show();
                        loadBindConfig = function () {
                            bindGrid.setData([]);
                            var loading = message.loading("加载中...");
                            request.createQuery("channel/bind/no-paging")
                                .where("configId", e.record.id)
                                .exec(function (resp) {
                                    loading.hide();
                                    if (resp.status === 200) {
                                        bindGrid.setData(resp.result);
                                    }
                                })
                        }
                        window.onDoBindMerchant = function () {
                            tools.openWindow("admin/merchant/selector.html?selector=true", "选择商户", "80%", "80%", function (merchant) {
                                if (merchant && merchant.id && merchant.name) {
                                    var data = {
                                        merchantId: merchant.id,
                                        merchantName: merchant.name,
                                        configId: e.record.id,
                                        configName: e.record.configName
                                    };
                                    request.post("channel/bind", data, function (respse) {
                                        if (respse.status === 200) {
                                            loadBindConfig();
                                        }
                                    })
                                }
                            })
                        };
                        loadBindConfig();
                    }));
                }
                list.push(tools.createActionButton("删除", "icon-remove", function () {
                    if (!e.record.id) {
                        e.sender.removeNode(e.record);
                    } else {
                        message.confirm("确定删除此配置,删除后无法恢复!", function () {
                            request['delete']('channel/config/' + e.record.id, function (resp) {
                                if (resp.status === 200) {
                                    e.sender.removeNode(e.record);
                                } else {
                                    message.showTips("删除失败:" + resp.message, "danger");
                                }
                            });
                        });
                    }
                }))
            }
            return list.join("");
        };

        configGrid.getColumn("status").renderer = function (e) {
            var status = (e.value ? e.value : 0) + "";
            if (status === '1') {
                return tools.createActionButton("点击禁用", "icon-ok", function () {
                    e.record.status = 0;
                    e.sender.updateRow(e.record, e.record);
                })
            } else {
                return tools.createActionButton("点击启用", "icon-remove", function () {
                    e.record.status = 1;
                    e.sender.updateRow(e.record, e.record);
                })
            }
        };
        var rateGrid = mini.get("value-ladder");

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
                rateConfigGetter[rateType].setConfig(conf.rate, false)
            }
        }

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

        function getRateConfig() {
            var rateType;
            $(rates).each(function () {
                if ((mini.getbyName("rate-" + this).getValue() + "") === 'true') {
                    rateType = (this + "");
                }
            });
            var rate = rateType ? rateConfigGetter[rateType].getConfig() : {};
            return {rate: rate.rate, rateType: rateType};
        }

        configGrid.getColumn("rateConfig").renderer = function (e) {
            var row = e.record;
            return tools.createActionButton("编辑", "icon-edit", function () {
                var win = mini.get('rateWindow');
                loadRate({rateType: row.rateType ? row.rateType.value ? row.rateType.value : row.rateType : null, rate: row.rate});
                win.show();
                $(".save-rate").unbind("click")
                    .on("click", function () {
                        var conf = getRateConfig();
                        console.log(conf)
                        e.sender.updateNode(row, conf);
                        win.hide();
                    })
            })
        }

        function getPropertiesConfig() {
            var propertiesGrid = mini.get("properties-grid");
            var properties = {};
            $(propertiesGrid.getData()).each(function () {
                properties[this.property] = this.value;
            });
            return properties;
        }

        function initPropertiesEditor(row) {
            var propertiesGrid = mini.get("properties-grid");
            propertiesGrid.setData([]);
            console.log(allChannels, row);
            var channel = allChannels[(row.transType.value ? row.transType.value : row.transType)
            + '-' + (row.channelProvider) + "-" + (row.channel || '_none')];
            if (channel && channel.properties) {
                var properties = mini.clone(channel.properties);
                $(properties).each(function () {
                    if (row.properties) {
                        this.value = row.properties[this.property];
                    }
                });
                propertiesGrid.setData(properties);
                $('.save-properties')
                    .unbind('click')
                    .on('click', function () {
                        row.properties = getPropertiesConfig();
                        mini.get('propertiesWindow').hide();
                    })
            }
            mini.get('propertiesWindow').show();
        }

        var limitGrid = mini.get("limit-grid");
        limitGrid.getColumn("timeUnit").renderer = function (e) {
            if (!e.value) {
                return "";
            }
            return (timeUnitDataMap[e.value.value ? e.value.value : e.value] || {text: e.value}).text;
        };
        limitGrid.getColumn("limit").renderer =
            limitGrid.getColumn("warnLimit").renderer = function (e) {
                return mini.formatNumber(parseFloat(e.value||0), "#,0.00元");
            };

        limitGrid.getColumn("action").renderer = function (e) {
            var html = [];
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
                    limitGrid.updateRow(row, row);
                    mini.get('limitWindow').hide();
                });
            mini.get('limitWindow').show();
        }

        configGrid.getColumn("config").renderer = function (e) {
            return tools.createActionButton("编辑配置", "icon-edit", function () {
                initPropertiesEditor(e.record);
            });
        }

        configGrid.getColumn("limitConfig").renderer = function (e) {
            return tools.createActionButton("编辑配置", "icon-edit", function () {
                initLimitEditor(e.record);
            });
        }
    })

});