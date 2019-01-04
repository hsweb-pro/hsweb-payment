define(["hsTable", "hsForm"], function (hsTable, hsForm) {

    /**
     * 开启数据选择器
     * <code>
     *     {
     *          url:"permission",
     *          columns:[
     *              {field:"id",title:"ID"},
     *              {field:"name",title:"名称"}
     *          ]
     *          search:[
     *              {field:"id"}
     *          ],
     *          multi:false //多选
     *     }
     * </code>
     * @param config
     */
    function init(config, call) {
        var id = "selector_" + new Date().getTime();

        config.columns.push({
            title: "操作", align: "center", width: "20%", toolbar: "<script type='text/html'>" +
            "<button lay-event=\"select\" class='layui-btn layui-btn-sm'><i class=\"layui-icon\">&#xe618;</i>选择</button>" +
            "</script>"
        });
        var columns = [config.columns];
        var layerIndex = open(id, config.title, config.area, "");

        var table = hsTable.init(id, id, config.url, columns, {
            search: config.search
        });
        layui.table.on("tool(" + id + ")", function (e) {
            var data = e.data;
            var layEvent = e.event;
            if (layEvent === 'select') {
                if (call(data)) {
                    layer.close(layerIndex);
                }
            }
        })
    }

    function open(formId, title, area, template) {
        var html = [
            '<div id="tools-' + formId + '"></div>' + "<div id='container-" + formId + "'  >",
            template,
            "</div>"
        ];
        return layer.open({
            type: 1,
            title: title || "选择",
            // skin: 'layui-layer-rim', //加上边框
            area: area || ['800px', '650px'], //宽高
            // btn: ["确定","重置"],
            content: html.join("")
        });
    }

    return init;
});