define(["echarts","request"], function (echarts,request) {

    require(["css!pages/agent/dashboard/public"]);
    require(["css!plugins/font-awesome/css/font-awesome"])
    function init(containerId) {
        require(["text!pages/dashboard/list.html"], function (html) {
            $("#container-" + containerId).html($(html));
            layui.element.init();
            initAnalysis();
        });
    }

    function initAnalysis() {

        var tradeChart = echarts.init(document.getElementById("trade"));

        var channelChart = echarts.init(document.getElementById("channel"));
        request.get("current-merchant/sum/trading/DAY/1/7",function (e) {
            if (e.status===200){
                var key = [];
                var value = [];
                var data = e.result;
                for (var k in data) {
                    key.push(data[k].comment.split("-")[0]);
                    value.push((parseFloat(data[k].total)/100).toFixed(2));
                }

                var option = {
                    tooltip : {},
                    legend: {
                    },
                    xAxis: {
                        data: key
                    },
                    yAxis: {},
                    series: [{
                        name:"交易额",
                        data: value,
                        type: 'line'
                    }]
                };
                tradeChart.setOption(option);
            }
        })


        request.get("current-merchant/trading/sum-by-channel/MONTH/1/1",function (e) {
            var key = [];
            var value = [];
            var data = e.result;
            var result = [];
            for (var k in data) {
                key.push(data[k].comment);
                value.push((parseFloat(data[k].total)/100).toFixed(2));
                var countResult = {
                    name:data[k].comment,
                    value:(parseFloat(data[k].total)/100).toFixed(2)
                };
                result.push(countResult);
            }

            var option = {
                title : {
                    text: '各渠道支付金额统计',
                    subtext: '',
                    x:'center'
                },
                tooltip : {
                    trigger: 'item',
                    formatter: "{a} <br/>{b} : {c} ({d}%)"
                },
                legend: {
                    orient: 'vertical',
                    left: 'left',
                    data: key
                },
                series : [
                    {
                        name: '支付金额',
                        type: 'pie',
                        radius : '55%',
                        center: ['50%', '60%'],
                        data: result,
                        itemStyle: {
                            emphasis: {
                                shadowBlur: 10,
                                shadowOffsetX: 0,
                                shadowColor: 'rgba(0, 0, 0, 0.5)'
                            }
                        }
                    }
                ]
            };

            channelChart.setOption(option);
        });


        request.get("current-merchant/balance",function (e) {
           if (e.status===200){
               $("#balance").html((parseFloat(e.result)/100).format());
           }
        });

        request.get("current-merchant/count/order/success/DAY/1/1",function (e) {
            if (e.status===200) {
                $("#today-success-order").html(e.result[0].total);
            }
        });
        request.get("current-merchant/count/order/DAY/1/1",function (e) {
            if (e.status===200) {
                $("#today-all-order").html(e.result[0].total);
            }
        });
        request.get("current-merchant/count/order/requestFail/DAY/1/1",function (e) {
            if (e.status===200) {
                $("#today-error-order").html(e.result[0].total);
            }
        });

        request.get("current-merchant/sum/amount/DAY/1",function (e) {
            if (e.status===200) {
                $("#today-all-amount").html((parseFloat(e.result) / 100).format());
            }
        });

        request.get("current-merchant/sum/amount/success/DAY/1",function (e) {
            if (e.status===200) {
                $("#today-success-amount").html((parseFloat(e.result)/100).format());
            }
        });

        request.get("current-merchant/freeze-balance",function (e) {
            if (e.status===200) {
                $("#current-freeze-amount").html((parseFloat(e.result)/100).format());
            }
        });

        request.get("notice/merchant",function (e) {
            var datas = e.result;
            var html = "";
            for (var i = 0; i < datas.total; i++) {
                var notice = datas.data[i];
                html += '<div class="layui-colla-item">' +
                    '<h2 class="layui-colla-title">'+notice.title+'</h2>' +
                    '<div class="layui-colla-content'+(i===0?" layui-show":"")+'">'+notice.content+'</div>' +
                    '</div>'
            }
            $("#agent-notice").append(html);
            layui.element.init();
        });

        request.get("current-merchant/channel/result/MONTH/1",function (e) {
            if (e.status ===200){
                var datas = e.result;
                var html = "";
                for (var i = 0; i < datas.length; i++) {
                    html+='<tr>\n' +
                        '            <td>'+datas[i].comment+'</td>\n' +
                        '            <td>'+datas[i].total+'</td>\n' +
                        '            <td>'+((datas[i].amount)/100).format()+'</td>\n' +
                        '            </tr>';
                }
            }
            $(".channel-result").append(html);
        });
    }

    return {init: init}
});