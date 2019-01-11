importResource("/admin/css/common.css");

require(["authorize"], function (authorize) {
    authorize.parse(document.body);
    window.authorize = authorize;
    importMiniui(function () {
        mini.parse();
        require(["miniui-tools", "request", "search-box"], function (tools, request, SearchBox) {
            var params = {
                paymentId: request.getParameter("paymentId"),
                merchantId: request.getParameter("merchantId")
            };
            new SearchBox({
                container: $("#search-box"),
                onSearch: search,
                initSize: 2
            }).init();
            window.tools = tools;
            var grid = window.grid = mini.get("data-grid");
            tools.initGrid(grid);
            grid.setUrl(API_BASE_PATH + "account-freeze-log");

            grid.getColumn("amount").renderer = function (e) {
                return mini.formatNumber(e.value / 100, '#0.00')
            };

            grid.getColumn("freezeTime").renderer = function (e) {
                return mini.formatDate(new Date(e.value), "yyyy-MM-dd HH:mm:ss")
            };
            grid.getColumn("unfreezeTime").renderer = function (e) {
                return mini.formatDate(new Date(e.value), "yyyy-MM-dd HH:mm:ss")
            };
            if (params.paymentId) {
                grid.hideColumn(grid.getColumn("paymentId"));
                grid.hideColumn(grid.getColumn("createTime"));
            }
            if (params.merchantId) {
                grid.hideColumn(grid.getColumn("accountName"));
            }


            function search() {

                tools.searchGrid("#search-box", grid, {}, params)
                // if (window.searchParam) {
                //     grid.load(window.searchParam());
                // } else {
                //     grid.load(request.encodeQueryParam(params));
                // }
            }

            search();

        });
    });
});

