define([], function () {

    function init(containerId) {
        require(["text!pages/charge/list.html"], function (html) {
            console.log(containerId)
            $("#container-" + containerId).html($(html));
            layui.element.init();
            initTable();
        });

    }

    function initTable() {
        layui.use('table', function () {
            var table = layui.table;
            table.render({
                elem: '#charge'
                , url: '/current-merchant/rate-configs'
                , cols: [[
                    {
                        field: 'channelName', title: '渠道',templet: function (d){
                            // console.log(d.transType);
                            // if (d.transType.text==="快捷支付"||d.transType.text==="代付"){
                            //     return d.transType.text;
                            // }
                            // else {
                                return d.channelName;
                            // }
                        }
                    }
                    // ,{field: 'transType', title: '交易类型',templet:function (d) {
                    //          return (d.transType||{}).text;
                    //      }}
                    , {field: 'rate', title: '费率', sort: true,templet: function (d){
                                if (d.rateType.value==="FIXED"){
                                    return (parseFloat(d.rate)/100).toFixed(2);
                                }
                                else {
                                    return d.rate;
                                }
                            }}
                    , {field: 'rateType', title: '费率类型',templet: function (d){
                            if (d.rateType.value==="FIXED"){
                                return "固定费率(元)";
                            }
                            else {
                                return (d.rateType||{}).text;
                            }
                        }}

                    // , {field: 'agentRate', title: '代理费率', sort: true}
                    // , {field: 'agentRateType', title: '代理费率类型'}
                    , {field: 'memo', title: '备注'}
                ]]
                , page: true
                , response: {
                    statusCode: 200
                }
                , parseData: function (res) {
                    console.log(res);
                    return {
                        "code": res.status,
                        "msg": '',
                        "count": res.result.total,
                        "data": res.result.data
                    }
                }
            });
        });
    }


    return {init: init}
})