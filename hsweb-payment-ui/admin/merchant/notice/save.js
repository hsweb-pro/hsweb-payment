importResource("/admin/css/common.css");


window.UEDITOR_HOME_URL = window.BASE_PATH + "plugins/ueditor/";
importMiniui(function () {

    mini.parse();

    mini.getbyName("types")
        .setUrl(API_BASE_PATH + "dictionary/define/notice-type/items");

    require(["request", "miniui-tools", "message"], function (request, tools, message) {
        var dataId = request.getParameter("id");

        $(".close").on("click", function () {
            tools.closeWindow();
        });
        var editor;
        require(["ueditor.config.js", "plugin/ueditor/ueditor.all.min"], function () {
            require(["plugin/ueditor/lang/zh-cn/zh-cn"], function () {
                editor = UE.getEditor("container");

            });
        });

        $(".save").on("click", function () {
            var ue = UE.getEditor("container");
            var data = new mini.Form("#notice-form").getData(true);
            if (dataId){
                data.id = dataId;
            }
            data.content = ue.getContent() || "";
            data.types = data.types.split(",");
            if (!data) {
                return;
            }
            var loading = message.loading("保存中...");

            request.patch("notice", data, function (e) {
                loading.hide();
                if (e.status===200){
                    message.showTips("保存成功！");
                } else {
                    message.showTips("保存失败！");
                }
            })
        });

        if (dataId){
            loadData(dataId);
        }
        function loadData() {
            var loading = message.loading("加载中...");
            request.get("notice/" + dataId, function (response) {
                loading.hide();
                if (response.status === 200) {
                    var types = response.result.types;
                    var type=[];
                    for (var i = 0; i < types.length; i++) {
                        type.push(types[i].value);
                    }
                    var ue = UE.getEditor("container");
                    new mini.Form("#notice-form").setData(response.result);
                    mini.get("types").setValue(type.join(","));
                    ue.setContent(response.result.content);
                    mini.parse();
                }
            })
        }

    });

});