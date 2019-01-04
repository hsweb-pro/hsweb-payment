importResource("/admin/css/common.css");

importMiniui(function () {
    mini.parse();
    require(["miniui-tools", "request", "search-box", "message"], function (tools, request, SearchBox, message) {
        new SearchBox({
            container: $("#search-box"),
            onSearch: search,
            initSize: 2
        }).init();
        window.tools = tools;
        var grid = window.grid = mini.get("data-grid");
        tools.initGrid(grid);
        grid.setUrl(API_BASE_PATH + "supplement");

        function search() {
            tools.searchGrid("#search-box", grid);
        }

        mini.getbyName('status$in').setUrl(API_BASE_PATH+"dictionary/define/supplement-status/items")

        search();
        $(".add-button").on("click", function () {
            tools.openWindow("admin/channel/supplement/save.html", "新建补登", "700", "400", search, function () {
            });
        });

        grid.getColumn("sourceAmount").renderer =
            grid.getColumn("targetAmount").renderer=function (e) {
            return mini.formatNumber(e.value / 100, "#,0.00元");
        };
        grid.getColumn("supplementTime").renderer = function (e) {
            return e.value ? mini.formatDate(new Date(e.value), 'yyyy-MM-dd HH:ss:dd') : "";
        };
        grid.getColumn("createTime").renderer = function (e) {
            return e.value ? mini.formatDate(new Date(e.value), 'yyyy-MM-dd HH:ss:dd') : "";
        };
        grid.getColumn("action").renderer = function (e) {
            var html = [];
            var row = e.record;


            if (row.status.value !== 'REQUEST') {
                html.push(
                    tools.createActionButton("查看记录", "icon-find", function () {
                        tools.openWindow("admin/channel/log/list.html?paymentId="+row.id,"查看记录","80%","80%",function () {

                        })
                    })
                );
            }
            if (row.status.value === 'REQUEST') {
                html.push(
                    tools.createActionButton("完成", "icon-ok", function () {

                        message.confirm("确认完成此补登.", function () {
                            grid.loading("完成中...");
                            request.put("supplement/complete/", {supplementId: row.id}, function (response) {
                                if (response.status === 200) {
                                    grid.reload();
                                } else {
                                    grid.unmask();
                                    message.alert(response.message);
                                }
                            })
                        })
                    })
                );
                html.push(
                    tools.createActionButton("关闭", "icon-remove", function () {
                        message.prompt("请输入补登原因", "关闭补登", function (text) {
                            grid.loading("关闭中...");
                            request.put("supplement/close/", {supplementId: row.id, remark: text}, function (response) {
                                if (response.status === 200) {
                                    grid.reload();
                                } else {
                                    grid.unmask();
                                    message.alert(response.message);
                                }
                            })
                        },true)
                    })
                );
            }
            if(row.status.value==='SUCCESS'){
                html.push(
                    tools.createActionButton("关闭", "icon-undo", function () {
                        message.prompt("请输入回退原因", "回退补登", function (text) {
                            grid.loading("回退中...");
                            request.put("supplement/rollback/", {supplementId: row.id, remark: text}, function (response) {
                                if (response.status === 200) {
                                    grid.reload();
                                } else {
                                    grid.unmask();
                                    message.alert(response.message);
                                }
                            })
                        },true)
                    })
                );
            }

            return html.join("")
        }
    });
});

