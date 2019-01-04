importResource("/admin/css/common.css");

require(["authorize"], function (authorize) {
    authorize.parse(document.body);
    window.authorize = authorize;
    importMiniui(function () {
        mini.parse();
        require(["miniui-tools", "request", "search-box"], function (tools, request, SearchBox) {
            var params = {
                accountNo: request.getParameter("accountNo"),
                merchantId: request.getParameter("merchantId"),
                paymentId:request.getParameter("paymentId")
            };
            new SearchBox({
                container: $("#search-box"),
                onSearch: search,
                initSize: 2
            }).init();
            window.tools = tools;
            var grid = window.grid = mini.get("data-grid");
            tools.initGrid(grid);
            grid.setUrl(API_BASE_PATH + "channel/settle/log");
            grid.getColumn("amount").renderer =
                grid.getColumn("balance").renderer = function (e) {
                    return mini.formatNumber(e.value / 100, '#0.00')
                };

            grid.getColumn("createTime").renderer = function (e) {
                return mini.formatDate(new Date(e.value), "yyyy-MM-dd HH:mm:ss")
            };
            grid.getColumn('memo').renderer = function (e) {
                return e.value ? e.value.split(";").join("<br/>") : "";
            }
            grid.getColumn('beforeBalance').renderer = function (e) {
                var row = e.record;

                if (row.fundDirection.value === "IN") {
                    return mini.formatNumber((row.balance - row.amount) / 100, '#0.00');
                }
                if (row.fundDirection.value === "OUT") {
                    return mini.formatNumber((row.balance + row.amount) / 100, '#0.00');
                }
            }
            if (params.paymentId) {
                grid.hideColumn(grid.getColumn("paymentId"));
                grid.hideColumn(grid.getColumn("createTime"));
            }
            if (params.merchantId) {
                grid.hideColumn(grid.getColumn("accountName"));
            }
            grid.on('load', function (e) {
                var inAmount = 0;
                var outAmount = 0;
                $(e.data).each(function () {
                    if (this.fundDirection.value === 'IN') {
                        inAmount += this.amount;
                    }
                    if (this.fundDirection.value === 'OUT') {
                        outAmount += this.amount;
                    }

                });
                $(".in-amount").text((inAmount / 100).toFixed(2));
                $(".out-amount").text((outAmount / 100).toFixed(2));
                grid.mergeColumns(["paymentId"]);
            });

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

