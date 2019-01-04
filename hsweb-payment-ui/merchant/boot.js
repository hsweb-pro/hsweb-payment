/**
 * 后台服务接口根路径
 * @type {string}
 */
window.API_BASE_PATH = "/";
/**
 * 资源文件根路径
 * @type {string}
 */
window.RESOURCE_PATH = "/merchant/";


String.prototype.startWith = function (str) {
    var reg = new RegExp("^" + str);
    return reg.test(this);
};

String.prototype.endWith = function (str) {
    var reg = new RegExp(str + "$");
    return reg.test(this);
};

/**
 * 获取cooke
 * @param sName cookie名称
 * @param defaultVal 默认值
 * @returns {*} cookie值
 */
function getCookie(sName, defaultVal) {
    var aCookie = document.cookie.split("; ");
    var lastMatch = null;
    for (var i = 0; i < aCookie.length; i++) {
        var aCrumb = aCookie[i].split("=");
        if (sName == aCrumb[0]) {
            lastMatch = aCrumb;
        }
    }
    if (lastMatch) {
        var v = lastMatch[1];
        if (v === undefined) return v;
        return unescape(v);
    }
    return defaultVal;
}

function importResource(path, callback) {
    if (path.indexOf("http") !== 0 || path.indexOf("//") !== 0) {
        if (!path.startWith("/"))
            path = window.RESOURCE_PATH + path;
    }
    var head = document.getElementsByTagName('head')[0];
    if (path.endWith("js")) {
        var script = document.createElement('script');
        script.type = 'text/javascript';
        script.charset = 'utf-8';
        script.timeout = 120000;
        if (typeof callback !== "undefined")
            script.async = false;
        script.src = path;

        function onload() {
            if (callback) callback();
        }

        script.onreadystatechange = function () {
            var r = script.readyState;
            if (r === 'loaded' || r === 'complete') {
                script.onreadystatechange = null;
                onload();
            }
        };
        script.onload = onload;
        script.onerror = onload;
        head.appendChild(script);
    } else if (path.endWith("css")) {
        var style = document.createElement('link');
        style.rel = "stylesheet";
        style.href = path;
        style.type = "text/css";
        head.appendChild(style);
        if (callback) callback();
    }
}

function initRequireJs() {
    require.config({
        waitSeconds: 0,
        map: {
            '*': {
                'css': RESOURCE_PATH + 'plugins/require/css/css.js',
                'text': RESOURCE_PATH + 'plugins/require/text/text.js'
            }
        },
        shim: {
            'jquery': {exports: "$"},
            'layui': {exports: "layui"}
        },
        paths: {
            "components":[RESOURCE_PATH + "modules/components"],
            "jquery": [RESOURCE_PATH + "plugins/jquery-ui/jquery"],
            "authorize": [RESOURCE_PATH + "admin/commons/authorize"], //权限管理
            "plugin": [RESOURCE_PATH + "plugins"],
            "module": [RESOURCE_PATH + "modules"],
            "request": [RESOURCE_PATH + "modules/request"],
            "pages": [RESOURCE_PATH + "pages"],
            "hsForm": [RESOURCE_PATH + "modules/components/hs-form"],
            "hsTable": [RESOURCE_PATH + "modules/components/hs-table"],
            "echarts":[RESOURCE_PATH + "../plugins/echarts/echarts.min"]
        }
    });
}

function importJquery(callback) {
    require(["jquery"], callback);
}

function importLayui(callback) {
    function doImport() {
        //重复引入
        if (window.layui) {
            callback();
            return;
        }

        function loadLayui() {
            require(["css!plugin/layui/css/layui", "plugin/layui/layui"], function () {
                layui.config({
                    dir: RESOURCE_PATH + 'plugins/layui/'
                    , version: false
                    , debug: false
                    , base: RESOURCE_PATH + 'modules/'
                });
                layui.use(["table", "element", "form", "layer", "laydate"], function () {
                    callback();
                });
            });
        }

        if (!window.jQuery && !window.$) {
            importJquery(loadLayui);
        } else {
            loadLayui();
        }
    }
    doImport();
}

//格式化Term
function encodeParams(params) {

    var terms = [];
    for (var param in params) {
        terms.push({
            column: param,
            value: params[param] || null
        })
    }
    var queryParam = {};
    terms.forEach(function (item, index) {
        for (var k in item) {
            queryParam["terms[" + (index) + "]." + k] = item[k];
        }
    });

    return queryParam;
}


/**
 * 将数值格式化成金额形式
 *
 * @param num 数值(Number或者String)
 * @param precision 精度，默认不变
 * @param separator 分隔符，默认为逗号
 * @return 金额格式的字符串,如'1,234,567'，默认返回NaN
 * @type String
 */

// String.prototype.startWith
Number.prototype.format = function(precision, separator) {
    var parts;
    var num = this;
    // 判断是否为数字
    if (!isNaN(parseFloat(num)) && isFinite(num)) {
        // 把类似 .5, 5. 之类的数据转化成0.5, 5, 为数据精度处理做准, 至于为什么
        // 不在判断中直接写 if (!isNaN(num = parseFloat(num)) && isFinite(num))
        // 是因为parseFloat有一个奇怪的精度问题, 比如 parseFloat(12312312.1234567119)
        // 的值变成了 12312312.123456713
        num = Number(num);
        // 处理小数点位数
        num = (typeof precision !== 'undefined' ? (Math.round(num * Math.pow(10, precision)) / Math.pow(10, precision)).toFixed(precision) : num).toString();
        // 分离数字的小数部分和整数部分
        parts = num.split('.');
        // 整数部分加[separator]分隔, 借用一个著名的正则表达式
        parts[0] = parts[0].toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, '$1' + (separator || ','));

        return parts.join('.');
    }
    return NaN;
};

Date.prototype.format = function(fmt) {
    var date = this;
    var o = {
        "M+": date.getMonth() + 1,                 //月份
        "d+": date.getDate(),                    //日
        "h+": date.getHours(),                   //小时
        "m+": date.getMinutes(),                 //分
        "s+": date.getSeconds(),                 //秒
        "q+": Math.floor((date.getMonth() + 3) / 3), //季度
        "S": date.getMilliseconds()             //毫秒
    };
    if (/(y+)/.test(fmt))
        fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}

function loadMenu(menu) {
    if ($("[lay-id=" + menu.id + "]").length === 0) {
        layui.element.tabAdd('tabs', {
            title: menu.name
            , content: '<div id="tools-' + menu.id + '"></div>' +
                '<div lay-filter="' + menu.id + '" id="container-' + menu.id + '">' +
                '</div>'
            , id: menu.id
        });
        require([menu.url], function (page) {
            page.init(menu.id);
        });
    }
    layui.element.tabChange('tabs', menu.id);
}


importResource(RESOURCE_PATH + "plugins/require/js/require.min.js", initRequireJs);
