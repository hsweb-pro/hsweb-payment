function setColumnsValue(update) {
    var value = $('input[name="columns"]').val()
    value = JSON.parse(value == '' ? '{}':value)
    var boo = update(value)
    if(boo != false){
        var tmp = []
        for (var name in value){
            if(value[name]){
                tmp.push(value[name])
            }
        }
        $('input[name="columns"]').val(JSON.stringify(tmp))
    }
    return boo != false
}

function setColumnsTableBody(data) {
    var tr = $('<tr data-value="'+data.id+'"></tr>')
    tr.append('<td>'+data.name+'</td>')
    tr.append('<td>'+data.columnName+'</td>')
    tr.append('<td>'+data.alias+'</td>')
    tr.append('<td>'+data.describe+'</td>')
    tr.append('<td>'+data.jdbcType+'</td>')
    tr.append('<td>'+data.javaType+'</td>')
    tr.append('<td>'+data.length+'</td>')
    tr.append('<td><button class="layui-btn layui-btn-danger layui-btn-xs" data-value="'+data.name+'" ' +
        'data-select-id="deleteColumnsBtn">' +
        '<i class="layui-icon">&#x1006;</i>' +
        '</button></td>')
    $('[data-select-id="field-table-body"]').append(tr)
}

define(["request", "hsForm", "hsTable"], function (request, hsForm, hsTable) {

    function init(containerId) {
        var id = "table" + new Date().getTime();
        var table;
        //打开编辑窗口
        function openSaveWindow(data,openCallback,submitCallback){
            var dataId = data ? data.id : "";
            require(["text!pages/form/save.html"], function (html) {
                hsForm.openForm({
                    title: "编辑表单",
                    area: ["1000px", "400px"],
                    template: {html: html, components: []},
                    data: data,
                    onOpen: function (formEl, ready) {
                        request.get(window.API_BASE_PATH + '/datasource',function (data) {
                            var select = $('select[name="dataSourceId"]')
                            $(data.result).each(function () {
                                select.append('<option value="'+this.id+'">'+this.name+'</option>')
                            })
                            ready()
                            if(openCallback) openCallback(formEl);
                        })

                        $(formEl).on('click','[data-select-id="add-column-btn"]',function () {
                            require(["text!pages/form/saveColumns.html"],function (html) {
                                hsForm.openForm({
                                    title: "添加字段",
                                    area: ["600px", "400px"],
                                    template: {html: html, components: []},
                                    data: null,
                                    onOpen: null,
                                    onSubmit: function (data) {
                                        if(setColumnsValue(function (value) {
                                            if(value[data.name]){
                                                layui.layer.msg('字段名称重复')
                                                return false
                                            }
                                            value[data.name] = data
                                        })){
                                            setColumnsTableBody(data)
                                            console.info($('input[name="columns"]'))
                                            return true
                                        }
                                    }
                                });
                            })
                        })
                    },
                    onSubmit: submitCallback
                });
            })
        }

        function edit(data) {
            openSaveWindow(data,function (formEl) {
                $(formEl).append('<input type="hidden" name="deleteColumns" value="{}" />')
                request.get(window.API_BASE_PATH + 'dynamic/form/column/' + data.id,function (r) {
                    $(r.result).each(function () {
                        setColumnsTableBody(this)
                    })
                })

                $(formEl).on('click','[data-select-id="deleteColumnsBtn"]',function () {
                    var tr = $(this).parent().parent()
                    var id = tr.attr('data-value')

                    var value = JSON.parse($('[name="deleteColumns"]').val())
                    value[id] = id
                    $('[name="deleteColumns"]').val(JSON.stringify(value))

                    tr.remove()
                })
            },function (formData,formEl) {
                request.patch(window.API_BASE_PATH + '/dynamic/form',formData,function (r) {
                    if(r.status && r.status == 200){
                        var columnIds = ''
                        var value = JSON.parse($('[name="deleteColumns"]').val())
                        for (var id in value){
                            columnIds = columnIds + id + ','
                        }
                        request.delete(window.API_BASE_PATH + 'dynamic/form/column?ids=' + columnIds,function (r) {
                            if(r.status && r.status == 200){
                                if(formData.columns){
                                    var columns = JSON.parse(formData.columns)
                                    formData.columns = []
                                    for (var name in columns){
                                        if(columns[name] != null){
                                            columns[name].formId = formData.id
                                            formData.columns.push(columns[name])
                                        }
                                    }
                                    request.patch(window.API_BASE_PATH + 'dynamic/form/column/batch',formData.columns,function (r) {
                                        if(r.status && r.status == 200){
                                            layui.layer.closeAll()
                                            table.reload()
                                            return true
                                        }else{
                                            layui.layer.msg(r.message)
                                        }
                                    })
                                }else{
                                    layui.layer.closeAll()
                                    table.reload()
                                    return true
                                }
                            }else{
                                layui.layer.msg(r.message)
                            }
                        })
                    }else{
                        layui.layer.msg(r.message)
                    }
                })
            })
        }

        table = hsTable.init(id, containerId, "/dynamic/form", [[
            {field: 'id', title: "", sort: true, width: "10%"},
            {field: 'type ', title: "表单类型", width: "10%"},
            {field: 'databaseTableName', title: "物理表名", width: "20%"},
            {field: 'alias', title: "别名", width: "20%"},
            {field: 'version', title: "版本", width: "10%"},
            {
                field: 'deployed', title: "发布状态", sort: true,width:"20%", templet: "<script type='text/html'>" +
            "<input type=\"checkbox\"  lay-skin=\"switch\" value=\"{{d.id}}\" lay-text=\"有效|无效\" {{d.deployed===true?'checked':''}} lay-filter=\"form_deployed\" name=\"deployed\" />" +
            "</script>"
            },
            {
                title: "操作", align: "center", width: "10%", toolbar: "<script type='text/html'>" +
                "<button lay-event=\"edit\" class='layui-btn layui-btn-sm'><i class=\"layui-icon\">&#xe642;</i></button>" +
                '<button lay-event="delete" class="layui-btn layui-btn-sm layui-btn-danger"><i class="layui-icon">&#xe640;</i></button>'+
                "</script>"
            }
        ]], {
            btns: [{
                name: '新建',
                class: '',
                callback: function () {
                    openSaveWindow(null,function (formEl) {
                        $(formEl).on('click','[data-select-id="deleteColumnsBtn"]',function () {
                            var tr = $(this).parent().parent()
                            var name = $(tr.find('td')[0]).text()
                            setColumnsValue(function (value) {
                                value[name] = null
                                tr.remove()
                            })
                        })
                    },function (formData,formEl) {
                        request.post(window.API_BASE_PATH + '/dynamic/form',formData,function (r) {
                            if(r.status && r.status == 200){
                                var columns = JSON.parse(formData.columns)
                                formData.columns = []
                                for (var name in columns){
                                    if(columns[name] != null){
                                        columns[name].formId = r.result
                                        formData.columns.push(columns[name])
                                    }
                                }
                                request.patch(window.API_BASE_PATH + 'dynamic/form/column/batch',formData.columns,function (r) {
                                    if(r.status && r.status == 200){
                                        layui.layer.closeAll()
                                        table.reload()
                                    }else{
                                        layui.layer.msg(r.message)
                                    }
                                })
                            }else{
                                layui.layer.msg(r.message)
                            }
                        })
                    })
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

        layui.form.on('switch(form_deployed)', function (obj) {
            var id = this.value;
            var state = obj.elem.checked;
            var url = state ? 'dynamic/form/' + id + '/deploy':'dynamic/form/' + id + '/un-deploy'
            request.put(url,{},function (resp) {
                if (resp.status !== 200) {
                    layer.alert("提交失败:" + resp.message);
                } else {
                    layer.tips("动态表单已" + (state ? "发布" : "取消发布"), obj.othis, {time: 1000});
                }
            })
        });

        layui.table.on("tool(" + containerId + ")", function (e) {
            var data = e.data;
            var layEvent = e.event;
            if (layEvent === 'edit') {
                edit(data);
            }else if(layEvent === 'delete'){
                layer.open({
                    content: '删除后不可回复，是否确认删除?',
                    yes: function(index, layero){
                        request.delete(window.API_BASE_PATH + 'dynamic/form/'+e.data.id,function (r) {
                            if(r.status && r.status == 200){
                                layer.close(index)
                                table.reload()
                            }else{
                                layui.layer.msg(r.message)
                            }
                        })
                    }
                })
            }
        })
    }

    return {init:init}
})