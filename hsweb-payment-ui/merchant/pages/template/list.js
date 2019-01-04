define(["request", "hsForm", "hsTable"], function (request, hsForm, hsTable) {

    var table
    var designer
    var designerWindow

    var listOption
    var listWindow

    //初始化
    function init(containerId) {
        var id = "table" + new Date().getTime();

        //生成模板列表
        table = hsTable.init(id, containerId, "template", [[
            {field: 'id', title: "模板标识", sort: true},
            {field: 'name', title: "模板名称"},
            {field: 'classified', title: "分类"},
            {
                title: "操作", align: "center", width: "30%", toolbar: "<script type='text/html'>" +
                "<button lay-event=\"edit\" class='layui-btn layui-btn-sm'><i class=\"layui-icon\">&#xe642;</i></button>" +
                '<button lay-event="delete" class="layui-btn layui-btn-sm layui-btn-danger"><i class="layui-icon">&#xe640;</i></button>'+
                "</script>"
            }
        ]], {
            btns: [{
                name: '新建模板',
                class: '',
                callback: openCreateModel}],
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

        layui.table.on("tool(" + containerId + ")", function (e) {
            var data = e.data;
            var layEvent = e.event;
            if (layEvent === 'edit') {
                openEditModel(data);
            }else if(layEvent === 'delete'){
                layer.open({
                    content: '删除后不可恢复，是否确认删除?',
                    yes: function(index, layero){
                        request.delete(window.API_BASE_PATH + 'template/' + data.id,function (r) {
                            if(r.status && r.status == 200){
                                layui.layer.closeAll()
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
    
    function openModel(title,openCallBack,submitCallBack,data) {
        data = data ? data : {}
        require(["text!pages/template/create.html"], function (html) {
            
            function baseInit(ready,formEl,data,first) {
                if(first){
                    ready()
                    layui.form.on('select(typeSelect)', function(d){
                        baseInit(ready,formEl,{type:d.value},false)
                    });
                }
                if(data.type == 'hf'){
                    frameBuild(ready,formEl,data)
                }else if(data.type == 'hl'){
                    listBuild(ready,formEl,data)
                }
            }
            
            function frameBuild(ready,formEl,data) {
                var frame = $('<iframe src="modules/form/designer.html" id="template-designer-iframe" class="template-designer-iframe"></iframe>')
                $(formEl).find('[data-select-id="show"]').empty().append(frame)
                designerWindow = frame[0].contentWindow
                designerWindow.ready = function (e) {
                    designer = e;
                    window.setTimeout(function () {
                        designer.init()
                        $(formEl).parent().on('click','[data-select-id="look"]',function () {
                            layui.layer.open({
                                area: ['800px', '500px'],
                                title:'预览',
                                content: designer.getHtml()
                            })
                        })
                        if(data && data.config){
                            var config = JSON.parse(data.config)
                            e.loadConfig(config)
                        }
                    },100)
                }
            }

            function formSubmit(formData,formEl,submitCallBack) {
                formData.config = JSON.stringify(designer.getConfig())
                request.patch(window.API_BASE_PATH + 'template',formData,function (r) {
                    if(r.status && r.status == 200){
                        if(submitCallBack){
                            submitCallBack(r)
                        }
                        layui.layer.closeAll()
                        table.reload()
                    }else{
                        layui.layer.msg(r.message)
                    }
                })
            }
            
            function listBuild(ready,formEl,data) {
                var frame = $('<iframe src="pages/template/dataList.html" id="data-list-iframe" class="template-designer-iframe"></iframe>')
                $(formEl).find('[data-select-id="show"]').empty().append(frame)
                listWindow = frame[0].contentWindow
                listWindow.ready = function (obj) {
                    listOption = obj
                    listOption.loadConfig(JSON.parse(data.config))
                    $(document).on('click','[data-select-id="look"]',function () {
                        layer.open({
                            type: 2,
                            area:['800px','600px'],
                            content: ['modules/template/generator.html','no'],
                            success: function (index, layero) {
                                var contentWindow = $('iframe[src="modules/template/generator.html"]')[0].contentWindow
                                contentWindow.ready = function (generator) {
                                    generator.build(listOption.getConfig())
                                }
                            }
                        });
                    })
                }
            }
            
            function listSubmit(formData,formEl,submitCallBack) {
                formData.config = JSON.stringify(listOption.getConfig())
                request.patch(window.API_BASE_PATH + 'template',formData,function (r) {
                    if(r.status && r.status == 200){
                        if(submitCallBack){
                            submitCallBack(r)
                        }
                        layui.layer.closeAll()
                        table.reload()
                    }else{
                        layui.layer.msg(r.message)
                    }
                })
            }
            
            hsForm.openForm({
                title: title,
                area: ["1400px", "800px"],
                template: {html: html, components: []},
                btn:{
                    templates:['<button class="layui-btn layui-btn-normal" data-select-id="look">预览</button>'],
                    width:300
                },
                data: data,
                onOpen: function (formEl,ready) {
                    baseInit(ready,formEl,data,true)
                },
                onSubmit:function (formData,formEl) {
                    if(formData.type == 'hf'){
                        formSubmit(formData,formEl,submitCallBack)
                    }else if(formData.type == 'hl'){
                        listSubmit(formData,formEl,submitCallBack)
                    }
                }
            })
        })
    }
    
    //打开创建模板窗口
    function openCreateModel() {
        openModel('新建模板')
    }

    //打开模板编辑窗口
    function openEditModel(data) {
        openModel('编辑模板',null,null,data)
    }

    return {init:init}
})