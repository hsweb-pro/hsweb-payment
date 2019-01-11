define([], function () {

    var accountType = {
        "PERSON": "个人账户", "BUSINESS": "对公"
    }
    var payeeTypes = {
        ALIPAY: {
            createHtml: function (payee) {
                var html = [
                    "<div style='font-size:16px;margin: auto;width: 400px'>"
                    , "<h2 align='center'>收款到支付宝</h2>"
                    , "<span>户<span style='margin-left: 2em'></span>名: </span>", payee.payeeName
                    , "<br><span>账<span style='margin-left: 2em'></span>号: </span>", payee.payee
                    , "<div>"

                ];
                return html.join("");
            }
        },
        BANK: {
            createHtml: function (payee) {
                var html = [
                    "<div style='font-size:16px;margin: auto;width: 400px'>"
                    , "<h2 align='center'>收款到银行卡</h2>"
                    , "<span>户<span style='margin-left: 2em'></span>名: </span>", payee.accountName
                    , "<br><span>账<span style='margin-left: 2em'></span>号: </span>", payee.accountNo
                    , "<br><span>账户类型: </span>", accountType[payee.accountType] || '个人账户'
                    , "<br><span>银行编码: </span>", (payee.bankId.value?payee.bankId.value:payee.bankId)
                    , "<br><span>支行名称: </span>", payee.branchName
                    , "<br><span>省<span style='margin-left: 2em'></span>份: </span>", payee.province
                    , "<br><span>城<span style='margin-left: 2em'></span>市: </span>", payee.city
                    , "<br><span>支行名称: </span>", payee.branchName
                    , "<div>"

                ];
                return html.join("");

            }
        }
    };

    var payeeWindow;


    function initPayeeWindow() {

        var win = $("<div title='收款人信息' class='mini-window' showModal='false' style='width: 500px;height: 400px'></div>");
        win.append("<div class='mini-fit' id='payee-info-conatiner'></div>");
        win.attr("id", "payee-window");

        $(document.body).append(win);
        mini.parse();
        payeeWindow = mini.get("payee-window");

    }

    initPayeeWindow();

    function show(payeeType, payee) {
        if (!payeeTypes[payeeType]) {
            return;
        }
        var html = payeeTypes[payeeType].createHtml(payee);

        $("#payee-info-conatiner").html("").append(html);
        mini.parse();
        payeeWindow.show();

    }

    return {
        show: show
    };
});