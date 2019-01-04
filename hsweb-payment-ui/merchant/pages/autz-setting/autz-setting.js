define(["request", "hsForm"], function (request, hsForm) {

    require(['plugin/ztree/jquery.ztree.all', 'css!plugin/font-awesome/css/font-awesome', 'css!plugin/ztree/awesomeStyle/awesome']);
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

    function openSettingWindow(settingForType, settingFor) {
        var menuTree;
        var permissionTree;
        var permissionCache = {};
        var autzSetting = {
            settingFor: settingFor + "",
            type: settingForType + "",
            details: [],
            menus: []
        };
        var noSelectPermission;

        request.get("autz-setting/" + settingForType + "/" + settingFor, function (resp) {
            if (resp.status === 200) {
                if (resp.result) {
                    autzSetting = resp.result;
                }
            }
            doInit();
        });

        request.get("permission/no-paging", function (resp) {
            if (resp.status === 200) {
                $(resp.result).each(function () {
                    permissionCache[this.id] = this;
                });
            }
        });

        function getAutzSettingDetail(id, create) {
            for (var i = 0; i < autzSetting.details.length; i++) {
                if (autzSetting.details[i].permissionId === id) {
                    return autzSetting.details[i];
                }
            }
            if (create) {
                var detail = create();
                autzSetting.details.push(detail);

                return detail;
            }
            return null;
        }

        function getDataAccessConfig(permissionId, type, action, auto) {
            if (!permissionId) {
                permissionId = noSelectPermission.type === 'action' ? noSelectPermission.parentId : noSelectPermission.id;
            }
            var setting = getAutzSettingDetail(permissionId);
            if (setting && setting.dataAccesses) {
                for (var i = 0; i < setting.dataAccesses.length; i++) {
                    if (setting.dataAccesses[i].type === type && setting.dataAccesses[i].action === action) {
                        var c = setting.dataAccesses[i];
                        if (typeof  c.config === 'string') {
                            c.config = JSON.parse(c.config);
                        }
                        return c;
                    }
                }
                if (auto) {
                    var dataAccess = {type: type, action: action};
                    setting.dataAccesses.push(dataAccess);
                    return dataAccess;
                }
            }
            return null;
        }

        function initDataAccess(action) {
            var filter = $(".dataAccessFieldFilter");
            var dataAccess = $(".dataAccess");

            filter.parent().hide();
            dataAccess.parent().hide();


            if (action.type === 'action') {
                //初始化 数据权限->字段过滤
                var permission = permissionCache[action.parentId];
                if (permission) {
                    filter.children().remove();
                    dataAccess.children().remove();
                    var conf = getDataAccessConfig(permission.id, "DENY_FIELDS", action.id, true);
                    if (permission.optionalFields && permission.optionalFields.length > 0) {
                        filter.parent().show();
                    }
                    $(permission.optionalFields)
                        .each(function () {
                            var checkbox = $("<input lay-filter='optional_field' checked type='checkbox'>")
                                .attr({
                                    title: this.describe,
                                    name: "optionalFields[" + this.name + "]",
                                    value: this.name
                                });
                            if (!conf.config) {
                                conf.config = {};
                            }
                            if (conf.config.fields && !conf.config.fields.indexOf(this.name)) {
                                checkbox.prop("checked", false);
                            }
                            filter.append(checkbox);
                        });
                    if (permission.supportDataAccessTypes) {
                        $(allSupportDataAccessTypes).each(function () {
                            if (permission.supportDataAccessTypes.indexOf(this.id) !== -1) {
                                var radio = $("<input lay-filter='data_access' type='radio'>")
                                    .attr({
                                        title: this.text,
                                        name: "dataAccess",
                                        value: this.id
                                    });
                                var conf = getDataAccessConfig(permission.id, this.id, action.id, false);
                                if (conf) {
                                    radio.prop("checked", true);
                                }
                                dataAccess.append(radio).append("<br>");
                            }
                        });
                        dataAccess.parent().show();
                    }
                }
            }
            layui.form.render();
            layui.form.on('checkbox(optional_field)', function (data) {
                var conf = getDataAccessConfig(noSelectPermission.parentId, "DENY_FIELDS", noSelectPermission.id, true);
                if (!conf.config) {
                    conf.config = {};
                }
                if (!conf.config.fields) {
                    conf.config.fields = [];
                }
                if (!data.elem.checked) {
                    conf.config.fields.push(data.value);
                } else {
                    var index = conf.config.fields.indexOf(data.value);
                    if (index !== -1) {
                        conf.config.fields.splice(index, 1);
                    }
                }
            });

            layui.form.on('radio(data_access)', function (data) {
                var type = data.value;
                if (data.elem.checked) {
                    var conf = getDataAccessConfig(noSelectPermission.parentId, type, noSelectPermission.id, true);

                }
            });
        }

        function initPermission(formEl) {
            $(".dataAccessFieldFilter").parent().hide();
            var setting = {
                view: {
                    selectedMulti: false,
                    dblClickExpand: true
                },
                check: {
                    enable: true,
                    chkboxType: {"Y": "ps", "N": "ps"}
                },
                data: {
                    simpleData: {
                        enable: true,
                        idKey: "id",
                        pIdKey: "parentId",
                        rootPid: "-1"
                    },
                    key: {
                        url: "click_url"
                    }
                },
                callback: {
                    onClick: function (event, treeId, treeNode) {
                        //  if (treeNode.type === 'action') {
                        noSelectPermission = treeNode;
                        initDataAccess(treeNode);
                        //  }
                    },
                    onCheck: function (event, treeId, treeNode) {
                        if (treeNode.type === 'action') {
                            var setting = getAutzSettingDetail(treeNode.parentId, function () {
                                return {permissionId: treeNode.parentId, actions: []};
                            });
                            var index = setting.actions.indexOf(treeNode.id);
                            if (treeNode.checked) {
                                if (index === -1) {
                                    setting.actions.push(treeNode.id);
                                }
                            } else {
                                if (index !== -1) {
                                    setting.actions.splice(index, 1);
                                }
                            }
                            console.log(setting);
                        }

                    }
                }
            };
            var checked = menuTree.getCheckedNodes();
            var permissions = {};
            $(checked).each(function () {
                var permissionId = this.permissionId;
                if (permissionId) {
                    var permissionIdList = permissionId.split(",");
                    $(permissionIdList).each(function () {
                        if (permissionCache[this]) {
                            permissions[this] = permissionCache[this];
                        }
                    })
                }
            });
            var permissionList = [];
            for (var id in permissions) {
                var permission = permissions[id];
                permission.type = "permission";
                permissionList.push(permission);
                var actionsArr = [];
                $(permission.actions).each(function () {
                    actionsArr.push(this.action);
                });
                var settings = getAutzSettingDetail(id, function () {
                    return {permissionId: id, actions: actionsArr, dataAccesses: []};
                });
                permission.checked = true;
                $(permission.actions).each(function () {
                    permissionList.push({
                        id: this.action,
                        type: "action",
                        name: this.describe,
                        parentId: id,
                        checked: settings.actions.indexOf(this.action) !== -1
                    });
                });
            }
            console.log(permissionList);
            permissionTree = $.fn.zTree.init(formEl.find("#permissionTree"), setting, permissionList);
            permissionTree.expandAll(true);
            //permissionTree.checkAllNodes(true);
            console.log(permissions);
        }

        function getMenu(id) {
            for (var i = 0; i < autzSetting.menus.length; i++) {
                if (autzSetting.menus[i].menuId === id) {
                    return autzSetting.menus[i];
                }
            }
            return null;
        }

        function doInit() {
            var setting = {
                view: {
                    selectedMulti: false,
                    dblClickExpand: true
                },
                check: {
                    enable: true,
                    chkboxType: {"Y": "ps", "N": "ps"}
                },
                data: {
                    simpleData: {
                        enable: true,
                        idKey: "id",
                        pIdKey: "parentId",
                        rootPid: "-1",
                        url: ""
                    },
                    key: {
                        url: "click_url"
                    }
                },
                callback: {
                    onCheck: function (event, treeId, treeNode) {
                        var menu = getMenu(treeNode.id);
                        if (treeNode.checked) {
                            if (!menu) {
                                autzSetting.menus.push({id: treeNode.id, menuId: treeNode.id, parentId: treeNode.parentId})
                            }
                        } else {
                            if (menu) {
                                var index = autzSetting.menus.indexOf(menu);
                                if (index !== -1) {
                                    autzSetting.menus.splice(index, 1);
                                }
                            }
                        }
                    }
                }
            };
            request.get("menu/no-paging", function (resp) {
                if (resp.status === 200) {
                    require(["text!pages/autz-setting/autz-setting.html"], function (html) {
                        hsForm.openForm({
                            template: {html: html, components: []},
                            onOpen: function (formEl, ready) {

                                $(resp.result).each(function () {
                                    if (getMenu(this.id)) {
                                        this.checked = true;
                                    }
                                });
                                menuTree = $.fn.zTree.init(formEl.find("#menuTree"), setting, resp.result);
                                ready();
                                menuTree.expandAll(true);
                                layui.element.on('tab(permission_tabs)', function (e) {
                                    if (e.index === 1) {
                                        initPermission(formEl);
                                    }
                                });
                            }, onSubmit: function (data) {
                                var menu = menuTree.getCheckedNodes();
                                autzSetting.menus = [];

                                $(menu).each(function () {
                                    autzSetting.menus.push({id: this.id, menuId: this.id, parentId: this.parentId});
                                });

                                request.patch("autz-setting", autzSetting, function (resp) {
                                    if (resp.status === 200) {
                                        autzSetting.id = resp.result;
                                        layer.alert("保存成功");
                                    } else {
                                        layer.alert("保存失败:" + resp.message);
                                    }
                                });
                                return false;
                            }
                        });
                    });
                }
            });
        }
    }

    return openSettingWindow;
});