importResource("/admin/css/common.css");

require(["authorize"], function (authorize) {
    authorize.parse(document.body);
    window.authorize = authorize;
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
            grid.setUrl(API_BASE_PATH + "notice");

            function search() {
                tools.searchGrid("#search-box", grid);
            }

            search();
            $(".add-button").on("click", function () {
                tools.openWindow("admin/merchant/notice/save.html", "新建公告", "80%", "80%", search, function () {
                });
            });
            grid.getColumn('types').renderer = function (e) {
                var names = [];
                $(e.value).each(function () {
                    names.push(this.text);
                })
                return names.join(",");
            };
            grid.getColumn('createTime').renderer = function (e) {
                return mini.formatDate(new Date(e.value), "yyyy-MM-dd HH:mm:ss")
            };

            grid.getColumn("status").renderer = function (e) {
                var status = ((e.row.status)||{}).value;
                if (status === 'OPEN') {
                    return tools.createActionButton("点击禁用", "icon-ok", function () {
                        e.record.status = "CLOSE";
                        e.sender.updateRow(e.row, e.row);
                        updateStatus({
                            status: e.record.status,
                            id: e.record.id
                        });
                    })
                } else if (status === 'CLOSE') {
                    return tools.createActionButton("点击启用", "icon-remove", function () {
                        e.record.status = "OPEN";
                        e.sender.updateRow(e.record, e.record);
                        updateStatus({
                            status: e.record.status,
                            id: e.record.id
                        });
                    })
                }
            };

            function updateStatus(data) {
                request.patch("notice", data, function (e) {
                    if (e.status === 200) {
                        grid.reload();
                    }
                });
            }

            grid.getColumn("action").renderer = function (e) {
                var html = [];
                html.push(
                    tools.createActionButton("编辑", "icon-edit", function () {
                        tools.openWindow("admin/merchant/notice/save.html?id=" + e.record.id, "编辑商户", "80%", "80%", grid.reload, function () {
                        });
                    }),
                );
                return html.join("")
            }
        });
    });
});

