define(["request", "hsForm", "hsTable"], function (request, hsForm, hsTable) {


    function init(containerId) {
        var id = "table" + new Date().getTime();
        var defPWD = Math.random();
        var table;
        var roles = [];
        //加载全部角色信息
        request.get("role/no-paging", function (e) {
            if (e.status === 200) {
                roles = e.result;
            }
        });

        //打开编辑窗口
        function openSaveWindow(data, callback) {
            var dataId = data ? data.id : "";
            if (data) {
                data.password = defPWD;
            }
            require(["text!pages/user/save.hf"], function (templateJSON) {
                hsForm.openForm({
                    title:"保存用户信息",
                    area: ["800px", "600px"],
                    template: JSON.parse(templateJSON),
                    data: data,
                    onOpen: function (formEl,ready) {
                        if (dataId) {
                            formEl.find("input[name=username]").attr("disabled", "disabled");
                        }
                        //将角色选择 加载到表单中
                        var roleContainer = $("<div class='layui-col-xs12 layui-col-md12'>");
                        var inputContainer = $("<div class=\"layui-input-inline\">");
                        $(roles).each(function () {
                            var box = $("<input type=\"checkbox\"  title='选项1'>")
                                .attr({
                                    title: this.name,
                                    name: "roles[" + this.id + "]"
                                });
                            if (data && data.roles && data.roles.indexOf(this.id) !== -1) {
                                box.prop("checked", true);
                            }
                            inputContainer.append(box);
                        });
                        roleContainer.append(inputContainer);
                        formEl.append(roleContainer);
                        ready();
                    },
                    onSubmit: function (form) {
                        //提交中遮罩层
                        var index = layer.msg('提交中', {
                            icon: 16
                            , shade: 0.01
                        });
                        //如果没有修改密码
                        if (form.password === "" + defPWD) {
                            delete form.password;
                        }
                        //将选中的角色转为集合
                        form.roles = form.roles ? form.roles.split(",") : undefined;

                        //如果id存在则使用put方法进行修改,否则使用post方法新增
                        var fun = dataId ? request.put : request.post;
                        fun("user/" + dataId, form, function (resp) {
                            layui.layer.close(index);
                            if (resp.status === 200 || resp.status === 201) {
                                if (resp.status === 201) { //201代表新增
                                    dataId = resp.result;
                                }
                                layui.layer.msg("保存成功");
                                if (callback)
                                    callback();
                            } else {
                                layui.layer.alert("保存失败:" + resp.message);
                            }
                        })
                        return false;
                    }
                });
            })
        }

        table = hsTable.init(id, containerId, "user", [[
            {field: 'username', title: "用户名", sort: true,width:"30%"},
            {field: 'name', title: "姓名",width:"30%"},
            {
                field: 'status', title: "状态", sort: true,width:"10%", templet: "<script type='text/html'>" +
            "<input type=\"checkbox\"  lay-skin=\"switch\" value=\"{{d.id}}\" lay-text=\"启用|禁用\" {{d.status===1?'checked':''}} lay-filter=\"user_status\" name=\"status\" title=\"有效\" />" +
            "</script>"
            },
            {
                title: "操作",align:"center", width:"30%",toolbar: "<script type='text/html'>" +
            "<button lay-event=\"edit\" class='layui-btn layui-btn-sm'><i class=\"layui-icon\">&#xe642;</i>编辑</button>" +
            "<button lay-event=\"edit_permission\" class='layui-btn layui-btn-normal layui-btn-sm'>" +
            "<i class=\"layui-icon\">&#xe614;</i> 设置权限</button>" +
            "</script>"
            }
        ]], {
            btns: [{
                name: '新建',
                class: '',
                callback: function () {
                    openSaveWindow(null, function () {
                        table.reload({}, true);
                    });
                }
            }],
            search: [{
                label: '用户名',
                column: 'username',
                type: 'input'
            }, {
                label: '姓名',
                column: 'name',
                type: 'input'
            }
            // , {
            //     label: '性别',
            //     column: 'gender',
            //     type: 'radio',
            //     options: [{
            //         value: 'man',
            //         text: '男',
            //     },{
            //         value: 'woman',
            //         text: '女',
            //     }]
            // }, {
            //     label: '复选框',
            //     column: 'checkcheck',
            //     type: 'checkbox',
            //     options: [{
            //         value: 'man',
            //         text: '男',
            //     },{
            //         value: 'woman',
            //         text: '女',
            //     }]
            // }, {
            //     label: '开关',
            //     column: 'switchswitch',
            //     type: 'switch',
            //     // text: '开启|关闭',
            // }, {
            //     label: '日期',
            //     column: 'switchswitch',
            //     type: 'date',
            //     options: {
            //         type: 'year'
            //     }
            // }, {
            //     label: 'hsSelect',
            //     column: 'hs1',
            //     type: 'hsSelect',
            //     url: 'menu',
            // }, {
            //     label: 'hsSelectTree',
            //     column: 'hs2',
            //     type: 'hsSelectTree',
            //     url: 'menu',
            // }
            ]
        });
        layui.form.on('switch(user_status)', function (obj) {
            var id = this.value;
            var state = obj.elem.checked;
            var api = state ? "user/" + id + "/enable" : "user/" + id + "/disable";
            request.put(api, {}, function (resp) {
                console.log(obj);
                if (resp.status !== 200) {
                    layer.alert("提交失败:" + resp.message);
                } else {
                    layer.tips("用户已" + (state ? "启用" : "禁用"), obj.othis, {time: 1000});
                }
            })
        });

        function edit(data) {
            request.get("user/" + data.id, function (resp) {
                if (resp.status === 200) {
                    openSaveWindow(resp.result, function () {
                        table.reload({}, true);
                    });
                }
            })
        }

        layui.table.on("tool(" + containerId + ")", function (e) {
            var data = e.data;
            var layEvent = e.event;
            if (layEvent === 'edit') {
                edit(data);
            }else if (layEvent === 'edit_permission') {
                require(["pages/autz-setting/autz-setting"], function (setting) {
                    setting("user", data.id)
                });
            }
        })
    }

    return {init: init}
});
