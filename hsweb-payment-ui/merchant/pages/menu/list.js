layui.define(["hsTable"],function (exports) {
    var hsTable=layui.hsTable;
    var $ = layui.jquery;
    exports("menuManage",{
        init:function (containerId) {
            hsTable.init("user-table"+new Date().getTime(),containerId,"menu",[[
                {field:'name',edit:"text",title:"菜单名称",sort:true},
                {field:'id',title:"ID",sort:true},
                {field:'url',title:"URL",sort:true},
                {field:'permissionId',title:"权限ID",sort:true}
            ]],{
                btns: [{
                    name: '新建',
                    class: 'layui-btn-primary',
                    callback: function() {
                        console.log(123);
                    }
                },{
                    name: '删除',
                    class: 'layui-btn-primary'
                }],
                search: [{
                    label: '菜单名称',
                    column: 'name',
                    type: 'input'
                },{
                    label: '菜单ID',
                    column: 'id',
                    type: 'select',
                    options: [{
                        value: 0,
                        text: '选项1'
                    },{
                        value: 1,
                        text: '选项2'
                    },{
                        value: 2,
                        text: '选项3'
                    }]
                },{
                    label: 'URL',
                    column: 'url',
                    type: 'input'
                }]
            });
        }
    });
});