importResource("/admin/css/common.css");
importResource("/admin/form/designer-drag/defaults.css");

importMiniui(function () {

    mini.parse();

    require(['message', 'request', 'miniui-tools'], function (message, request, tools) {

        function selectSettle(e) {
            tools.openWindow("admin/channel/settle/list.html?selector=true", "选择结算账户", "80%", "80%", function (channel) {

                if (channel && channel !== 'close' && channel !== 'cancel') {
                    e.source.setValue(channel.accountNo);
                    e.source.setText(channel.name);
                    // e.source.setReadOnly(true)
                }
            });
        }

        var id = request.getParameter("id");

        var sourceAccountNo = mini.getbyName("sourceAccountNo");
        sourceAccountNo.on("buttonclick", selectSettle);
        var targetAccountNo = mini.getbyName("targetAccountNo");
        targetAccountNo.on("buttonclick", selectSettle);

        function getData() {
            var data = tools.getFormData("#form", true);
            if (!data) {
                return;
            }
            data.sourceAccountName = sourceAccountNo.getText();
            data.targetAccountName = targetAccountNo.getText();
            data.sourceAmount = parseFloat(data.sourceAmount) * 100;
            data.targetAmount = parseFloat(data.targetAmount) * 100;

            if (sourceAccountNo.getValue() === sourceAccountNo.getText()) {
                delete data.sourceAccountNo;
            }
            if (targetAccountNo.getValue() === targetAccountNo.getText()) {
                delete data.targetAccountNo;
            }

            if (!data.sourceAccountNo && !data.targetAccountNo) {
                mini.alert("必须选择源账户或目标账户")
                return;
            }

            return data;
        }

        $(".save-button").on("click", function () {
            var data = getData();

            if (data) {
                request.post("supplement/create", data, function (response) {
                    if (response.status === 200) {
                        tools.closeWindow();
                    } else {
                        message.alert(response.message);
                    }
                })
            }
        });
        $('.close')
            .on('click', function () {
                tools.closeWindow();
            })
    })


});