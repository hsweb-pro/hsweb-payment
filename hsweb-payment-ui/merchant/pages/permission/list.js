define(["request", "hsForm", "hsTable"], function (request, hsForm, hsTable) {

    function init(containerId) {
        var id = "table" + new Date().getTime();
        var table;

        //打开编辑窗口
        function openSaveWindow(data, callback) {
            var dataId = data ? data.id : "";
            require(["text!pages/permission/save.html"], function (html) {
                hsForm.openForm({
                    title: "保存权限信息",
                    area: ["850px", "600px"],
                    template: {html: html, components: []},
                    data: data,
                    onOpen: function (formEl, ready) {
                        //表单被打开后调用
                        if (dataId) {
                            formEl.find("input[name=id]").attr("disabled", "disabled");
                        }
                        require(["pages/permission/save"], function (savePage) {
                            savePage(formEl, data);
                            ready();
                        });

                    },
                    onSubmit: function (formData, formEl) {
                        //数据权限
                        formData.supportDataAccessTypes = typeof formData.supportDataAccessTypes === 'string' ? formData.supportDataAccessTypes.split(",") : formData.supportDataAccessTypes;
                        //按钮
                        var actions = [];
                        var buttons = formEl.find(".permission-actions button");
                        $(buttons).each(function () {
                            var text = $(this).text();
                            var arr = [text, text];
                            if (text.indexOf(":") !== -1) {
                                arr = text.split(":");
                            }
                            actions.push({action: arr[0], describe: arr[1]});
                        });
                        formData.actions = actions;
                        //提交中遮罩层
                        var index = layer.msg('提交中', {
                            icon: 16
                            , shade: 0.01
                        });
                        //如果id存在则使用put方法进行修改,否则使用post方法新增
                        var fun = dataId ? request.put : request.post;
                        fun("permission/" + dataId, formData, function (resp) {
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
                        });
                        return false;
                    }
                });
            })
        }

        table = hsTable.init(id, containerId, "permission", [[
            {field: 'id', title: "标识", sort: true, width: "20%"},
            {field: 'name', title: "名称", width: "30%"},
            {field: 'describe', title: "备注", width: "30%"},
            // {
            //     field: 'status', title: "状态", sort: true,width:"10%", templet: "<script type='text/html'>" +
            // "<input type=\"checkbox\"  lay-skin=\"switch\" value=\"{{d.id}}\" lay-text=\"启用|禁用\" {{d.status===1?'checked':''}} lay-filter=\"role_status\" name=\"status\" title=\"有效\" />" +
            // "</script>"
            // },
            {
                title: "操作", align: "center", width: "20%", toolbar: "<script type='text/html'>" +
            "<button lay-event=\"edit\" class='layui-btn layui-btn-sm'><i class=\"layui-icon\">&#xe642;</i>编辑</button>" +
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
                label: '标识',
                column: 'id',
                type: 'input'
            }, {
                label: '名称',
                column: 'name',
                type: 'input'
            }]
        });
        layui.form.on('switch(role_status)', function (obj) {
            var id = this.value;
            var state = obj.elem.checked;
            var api = state ? "permission/" + id + "/enable" : "permission/" + id + "/disable";
            request.put(api, {}, function (resp) {
                if (resp.status !== 200) {
                    layer.alert("提交失败:" + resp.message);
                } else {
                    layer.tips("角色已" + (state ? "启用" : "禁用"), obj.othis, {time: 1000});
                }
            })
        });

        function edit(data) {
            openSaveWindow(data, function () {
                table.reload({}, true);
            });
        }

        layui.table.on("tool(" + containerId + ")", function (e) {
            var data = e.data;
            var layEvent = e.event;
            if (layEvent === 'edit') {
                edit(data);
            }
        })
    }

    return {init: init}
});
