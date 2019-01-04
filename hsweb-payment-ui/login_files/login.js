var nowCaptchaToken;
var API_BASE_PATH = window.top.API_BASE_PATH || '/';

function reset() {
    LUOCAPTCHA.reset();
    nowCaptchaToken = undefined
    $("#password").val("");
    $("#login-button").text("立即登录").removeAttr("disabled");

}

function getResponse(resp) {
    nowCaptchaToken = resp;
    doLogin();
}

function doLogin() {
    var username = $("#username").val();
    var password = $("#password").val();
    if (!username) {
        $("#username").focus();
        return;
    }
    if (!password) {
        $("#password").focus();
        return;
    }
    if (!nowCaptchaToken) {
        alert("请先完成人机验证");
        return
    }
    $("#login-button").attr("disabled", "disabled").text("登录中...");

    function handleResponse(e) {
        reset();
        if (e.status === 200) {
            if (window.loginCallback) {
                window.loginCallback(e.result);
                return;
            }
            if (e.result.userType && (e.result.userType === 'merchant' || e.result.userType === 'agent')) {
                window.location.href = "merchant/index.html";
            } else {
                window.location.href = "admin/index.html";
            }
        } else {
            alert(e.message);
        }
    }

    $.ajax({
        url: API_BASE_PATH + "authorize/login",
        method: "POST",
        data: JSON.stringify(
            {
                username: username,
                password: password,
                token: nowCaptchaToken
            }
        ),
        contentType: "application/json",
        async: true,
        dataType: "json",
        success: handleResponse,
        error: function (e) {
            if (e.status === 200) {
                msg = {status: 200, result: e.statusText, success: true};
                return msg;
            }
            var msg = {};
            if (e.responseJSON) {
                msg = e.responseJSON;
            } else {
                msg = {status: e.status, message: e.statusText ? e.statusText : "未知错误", success: false};
            }
            handleResponse(msg);
        }
    })
}
