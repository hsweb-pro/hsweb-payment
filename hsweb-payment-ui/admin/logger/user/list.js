importResource("/admin/css/common.css");
var districtMap = {};

importMiniui(function () {
    mini.parse();
    require(["request", "miniui-tools", "search-box", 'pages/form/designer-drag/parser'],
        function (request, tools, SearchBox, FormParser) {
            var infoForm;
            require(["text!info.hf", "pages/form/designer-drag/components-default"], function (config) {
                infoForm = new FormParser(JSON.parse(config));
                var formEl = $("#content");
                infoForm.render(formEl);
            });
            new SearchBox({
                container: $("#search-box"),
                onSearch: search,
                initSize: 2
            }).init();
            var grid = window.grid = mini.get("datagrid");
            tools.initGrid(grid);
            grid.setUrl(request.basePath + "logger/user");
            var requestIdParam = request.getParameter("sessionId");
            search();

            function search() {
                tools.searchGrid("#search-box", grid, {}, {sessionId: requestIdParam});
            }

            function showDetail(data) {
                var win = mini.get("detailWin");
                win.show();
                var date = new Date(data.createTime);
                data["createTime"] = mini.formatDate(date, "yyyy-MM-dd HH:mm:ss");
                infoForm.setData(data);
            }

            $(".search-button").click(search);
            tools.bindOnEnter("#search-box", search);

            $(".reload-button").on("click", function () {
                window.location.reload();
            });


            window.renderTime = function (e) {
                var date = new Date(e.value);
                return mini.formatDate(date, "yyyy-MM-dd HH:mm:ss")
            };

            window.renderAction = function (e) {
                var row = e.record;
                var html = [];
                html.push(tools.createActionButton("查看本次会话操作记录", "icon-find", function () {
                    tools.openWindow("admin/logger/access/list.html?sessionId=" + row.sessionId, "访问日志", "80%", "80%", function () {
                        grid.reload();
                    });
                }));

                html.push(tools.createActionButton("查看本次会话系统日志", "icon-search", function () {
                    tools.openWindow("admin/logger/system/list.html?sessionId=" + row.sessionId, "系统日志", "80%", "80%", function () {
                        grid.reload();
                    });
                }));
                return html.join("");
            };

        });
});