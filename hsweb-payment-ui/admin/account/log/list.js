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
            grid.setUrl(API_BASE_PATH + "account-trans-log");
            grid.getColumn("transAmount").renderer =
                grid.getColumn("balance").renderer = function (e) {
                    return mini.formatNumber(e.value / 100, '#0.00')
                };

            grid.getColumn("createTime").renderer = function (e) {
                return mini.formatDate(new Date(e.value), "yyyy-MM-dd HH:mm:ss")
            };
            grid.getColumn('beforeBalance').renderer = function (e) {
                var row = e.record;

                if (row.accountTransType.value === "IN") {
                    return mini.formatNumber((row.balance - row.transAmount) / 100, '#0.00');
                }if (row.accountTransType.value === "OUT") {
                    return mini.formatNumber((row.balance + row.transAmount) / 100, '#0.00');
                }
            }
            if (params.paymentId) {
                grid.hideColumn(grid.getColumn("paymentId"));
                grid.hideColumn(grid.getColumn("createTime"));
            }
            if (params.merchantId) {
                grid.hideColumn(grid.getColumn("accountName"));
            }
            grid.getColumn('comment').renderer = function (e) {
                return e.value ? e.value.split(";").join("<br/>") : "";
            }
            grid.on('load', function (e) {
                var inAmount = 0;
                var outAmount = 0;
                $(e.data).each(function () {
                    if (this.accountTransType.value === 'IN') {
                        inAmount += this.transAmount;
                    }
                    if (this.accountTransType.value === 'OUT') {
                        outAmount += this.transAmount;
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

