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
            var selector = request.getParameter("selector") === 'true';

            window.tools = tools;
            var grid = window.grid = mini.get("data-grid");
            tools.initGrid(grid);
            grid.setUrl(API_BASE_PATH + "channel/settle/info");
            grid.getColumn("balance").renderer = function (e) {
                return mini.formatNumber(e.value / 100, "#0,.00");
            };

            function search() {
                tools.searchGrid("#search-box", grid);
            }

            search();
            $(".add-button").on("click", function () {
                var row ={name: "新建结算账户", balance: 0};
                grid.addRow(row);
                grid.setCurrentCell([row._id-1,0])
                grid.beginEditCell(row);
            });

            grid.on('cellbeginedit', function (e) {
                if (e.field === 'accountNo') {
                    if (e.record.id) {
                        e.cancel = true;
                    }
                }
            })

            grid.getColumn("action").renderer = function (e) {
                var html = [];
                var row = e.record;
                if (selector) {
                    html.push(
                        tools.createActionButton("选择", "icon-ok", function () {
                            tools.closeWindow(e.record);
                        })
                    );
                }

                html.push(
                    tools.createActionButton("保存结算账户", "icon-save", function () {
                        var loading = message.loading("保存中...");
                        request.patch("channel/settle/info", {id: row.id, accountNo: row.accountNo, name: row.name, comment: row.comment}, function (resp) {
                            loading.hide();
                            if (resp.status !== 200) {
                                message.alert(resp.message);
                            } else {
                                message.showTips("保存成功");
                                e.record.id = resp.result;
                                e.sender.updateRow(e.record);
                                e.sender.accept(e.record)
                            }
                        })
                    })
                );

                html.push(
                    tools.createActionButton("查看结算记录", "icon-money", function () {
                        tools.openWindow("admin/channel/log/list.html?accountNo=" + row.accountNo, "结算记录", "80%", "80%", function () {

                        }, function () {
                        });
                    })
                );


                return html.join("")
            }
        });
    });
});

