define([], function () {

    function init(containerId) {
        require(["css!pages/api/list"]);
        require(["text!pages/api/list.html"], function (html) {
            console.log(containerId)
            $("#container-" + containerId).html($(html));
            layui.element.init();
            initData();

            $(".channel-sign").on("click",function () {
                layer.open({
                    id: 'channelSign' //防止重复弹出
                    , type: 1
                    , title: '渠道标志'
                    , content: $(".channel-content")
                    , btn: '关闭全部'
                    , btnAlign: 'c' //按钮居中
                    , shade: 0 //不显示遮罩
                    , area: ['600px', '500px']
                    , success: function () {
                    }
                    , yes: function () {
                        layer.closeAll();
                    }
                })
            });

            $(".api-url").append(window.location.protocol+"//"+window.location.host);
        });
    }

    function initData() {

        $(".view-error").on("click",function () {
            layer.open({
                id: 'viewError' //防止重复弹出
                , type: 1
                , title: '错误响应'
                , content: $(".pay-error-response")
                , btn: '关闭全部'
                , btnAlign: 'c' //按钮居中
                , shade: 0 //不显示遮罩
                , area: ['800px', '600px']
                , success: function () {
                }
                , yes: function () {
                    layer.closeAll();
                }
            });
        });

        $(".view-wx-native").on("click",function () {
            layer.open({
                id: 'wxNative' //防止重复弹出
                , type: 1
                , title: '微信扫码支付'
                , content: $(".pay-wx-native")
                , btn: '关闭全部'
                , btnAlign: 'c' //按钮居中
                , shade: 0 //不显示遮罩
                , area: ['800px', '600px']
                , success: function () {
                }
                , yes: function () {
                    layer.closeAll();
                }
            });
        });

        $(".view-wx-h5").on("click",function () {
            layer.open({
                id: 'wxH5' //防止重复弹出
                , type: 1
                , title: '微信H5支付'
                , content: $(".pay-wx-h5")
                , btn: '关闭全部'
                , btnAlign: 'c' //按钮居中
                , shade: 0 //不显示遮罩
                , area: ['800px', '600px']
                , success: function () {
                }
                , yes: function () {
                    layer.closeAll();
                }
            });
        });

        $(".view-ali-web").on("click",function () {
            layer.open({
                id: 'aliWeb' //防止重复弹出
                , type: 1
                , title: '支付宝电脑网页支付'
                , content: $(".pay-ali-web")
                , btn: '关闭全部'
                , btnAlign: 'c' //按钮居中
                , shade: 0 //不显示遮罩
                , area: ['800px', '600px']
                , success: function () {
                }
                , yes: function () {
                    layer.closeAll();
                }
            });
        });

        $(".view-ali-h5").on("click",function () {
            layer.open({
                id: 'aliH5' //防止重复弹出
                , type: 1
                , title: '支付宝H5支付'
                , content: $(".pay-ali-h5")
                , btn: '关闭全部'
                , btnAlign: 'c' //按钮居中
                , shade: 0 //不显示遮罩
                , area: ['800px', '600px']
                , success: function () {
                }
                , yes: function () {
                    layer.closeAll();
                }
            });
        });

        $(".substitute-details").on("click",function () {
            console.log("批量代付");
            layer.open({
                id: 'batch-substitute' //防止重复弹出
                , type: 1
                , title: '代付详情'
                , content: $(".details")
                , btn: '关闭全部'
                , btnAlign: 'c' //按钮居中
                , shade: 0 //不显示遮罩
                , area: ['600px', '500px']
                , success: function () {
                }
                , yes: function () {
                    layer.closeAll();
                }
            })
        });
    }

    return {init: init}
})