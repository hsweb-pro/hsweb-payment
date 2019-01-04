define(["request"], function (request) {
    require(["css!pages/permission/save"]);
    var allSupportDataAccessTypes = [
        // {id: "DENY_FIELDS", text: "禁止访问字段"},
        {id: "ONLY_SELF", text: "仅限本人"},
        {id: "POSITION_SCOPE", text: "仅限本人及下属"},
        {id: "DEPARTMENT_SCOPE", text: "所在部门"},
        {id: "ORG_SCOPE", text: "所在机构"},
        {id: "CUSTOM_SCOPE_ORG_SCOPE_", text: "自定义设置-机构"},
        {id: "CUSTOM_SCOPE_DEPARTMENT_SCOPE_", text: "自定义设置-部门"},
        {id: "CUSTOM_SCOPE_POSITION_SCOPE_", text: "自定义设置-岗位"}
    ];
    var defaultActionData = [
        {"action": "query", "describe": "查询列表", defaultCheck: true},
        {"action": "get", "describe": "查询明细", defaultCheck: true},
        {"action": "add", "describe": "新增", defaultCheck: true},
        {"action": "update", "describe": "修改", defaultCheck: true},
        {"action": "delete", "describe": "删除", defaultCheck: false}
    ];

    /*action 相关*/
    function initAction(actionButton) {
        actionButton.unbind("click")
            .on("click", function () {
                var editor = $("<div class='layui-input-inline'>");
                var input = $("<input style='width: 100px;margin-left: 10px;margin-right: 10px;' class='layui-input layui-input-inline'>");
                input.val(actionButton.text());
                input.css("width", actionButton.width() + 35);
                editor.append(input);
                // editor.append($("<button class='layui-btn layui-btn-danger'>")
                //     .text("X").on("click",function () {
                //         editor.remove();
                //         actionButton.remove();
                //     }));
                actionButton.replaceWith(editor);
                input.focus();
                input.on("blur", function () {
                    if (!input.val()) {
                        input.remove();
                    } else {
                        actionButton.text(input.val());
                        editor.replaceWith(actionButton);
                        initAction(actionButton);
                    }
                })
            });
    }

    function addActionButton(container, action) {
        var actionButton = $("<button class=\"layui-btn layui-btn-normal\"></button>");
        actionButton.text(action.action + ":" + action.describe);
        initAction(actionButton);
        container.append(actionButton);
        return actionButton;
    }


    /*数据权限相关*/
    function initDataAccessSupport() {
        $(allSupportDataAccessTypes).each(function () {
            var checkbox = $(" <input type=\"checkbox\" >");
            checkbox.attr({
                "name": "supportDataAccessTypes[" + this.id + "]",
                "title": this.text
            });
            $(".support-data-access").append(checkbox);
        })
    }

    /*初始化*/
    function init(el, data) {
        initDataAccessSupport();
        var buttonsContainer = el.find(".permission-actions");
        $(".add-permission-action").on("click", function () {
            addActionButton(buttonsContainer, {action: "new-action", describe: "新建操作"})
                .click();
        });
        if (data) {
            $(data.actions).each(function () {
                addActionButton(buttonsContainer, this);
            })
        }else{
            $(defaultActionData).each(function () {
                addActionButton(buttonsContainer, this);
            })
        }
        layui.form.render()
    }

    return init
});