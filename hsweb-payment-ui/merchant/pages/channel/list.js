define([], function () {

    function init(containerId) {
        require(["text!pages/channel/list.html"], function (html) {
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
                elem: '#channel-list'
                , url: '/manager/merchant/channel-configs'
                , cols: [[
                    {field: 'channelName', title: '通道标志'}
                    , {field: 'channel', title: '通道名称'}
                    , {field: 'transType', title: '交易类型', templet: function (d) {
                            return d.transType.text;
                        }}
                ]]
                , response:
                    {
                        statusCode: 200
                    }
                ,
                parseData: function (res) {
                    console.log(res);
                    return {
                        "code": res.status,
                        "msg": '',
                        "data": res.result
                    }
                }
            });
        });
    }

    return {init: init}
})