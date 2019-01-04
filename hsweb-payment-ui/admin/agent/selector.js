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
            grid.setUrl(API_BASE_PATH + "manager/agent");

            function search() {
                tools.searchGrid("#search-box", grid);
            }

            search();


            grid.getColumn("action").renderer = function (e) {
                var html = [];
                var row = e.record;

                html.push(
                    tools.createActionButton("选择", "icon-ok", function () {
                        tools.closeWindow(mini.clone(row));
                    })
                );

                html.push(
                    tools.createActionButton("查看交易记录", "icon-money", function () {
                        tools.openWindow("admin/account/log/list.html?merchantId=" + row.id, "交易记录", "80%", "80%", function () {

                        });
                    })
                );

                return html.join("")
            }
        });
    });
});

