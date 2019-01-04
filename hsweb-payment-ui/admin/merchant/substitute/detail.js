importResource("/admin/css/common.css");
importResource("/admin/form/designer-drag/defaults.css");
require(["authorize"], function (authorize) {
    authorize.parse(document.body);
    window.authorize = authorize;
    importMiniui(function () {
        mini.parse();
        mini.getbyName("status$in")
            .setUrl(API_BASE_PATH + "dictionary/define/substitute-detail-status/items");
        require(["miniui-tools", "request", "search-box", "message"], function (tools, request, SearchBox, message) {

            var substituteId = request.getParameter("substituteId");

            new SearchBox({
                container: $("#search-box"),
                onSearch: search,
                initSize: 2
            }).init();
            window.tools = tools;
            var grid = window.grid = mini.get("data-grid");
            tools.initGrid(grid);
            tools.multiSort(grid, search);
            grid.setDataField("result");
            grid.setUrl(API_BASE_PATH + "merchant/substitute/" + substituteId + "/details");

            function search(sort) {
                tools.searchGrid("#search-box", grid, sort, {}, function (param) {
                    for (var key in param) {
                        if (key.indexOf("amount") !== -1) {
                            param[key] = Math.round((parseFloat(param[key]) * 100));
                        }
                    }
                });
            }

            search();

            function renderAmount(e) {
                var amount = parseFloat(e.value || 0);
                return mini.formatNumber(parseFloat((amount / 100).toFixed(2)), "#,0.00");
            }

            // grid.getColumn('payeeInfo').renderer = function (e) {
            //     var row = e.record;
            //     if (row.payeeInfo) {
            //         var str = [
            //             "收款方式:",
            //             row.payeeType.text,
            //             "<br>收款人:",
            //             row.payeeInfo.payeeName,
            //             " ",
            //             row.payeeInfo.payee
            //         ];
            //         return str.join("");
            //     }
            //     return "/";
            // };
            grid.getColumn('amount').renderer = renderAmount;

            grid.getColumn("action").renderer = function (e) {
                var html = [];
                var row = e.record;
                //
                // if (row.paymentId) {
                //     html.push(
                //         tools.createActionButton("查看资金流向", "icon-money", function () {
                //             tools.openWindow("admin/account/log/list.html?paymentId=" + row.paymentId, "资金流向", "80%", "80%", grid.reload, function () {
                //             });
                //         })
                //     )
                // }
                return html.join("")
            }
        });
    });
});

