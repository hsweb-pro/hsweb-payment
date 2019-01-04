layui.use("table", function () {
    //展示已知数据
    layui.table.render({
        elem: '#configTable'
        , cols: [[ //标题栏
            {field: 'column', align: 'center', edit: "text", title: '字段'}
            , {field: 'alias', align: 'center', edit: "text", title: '别名'}
            , {
                field: 'javaType', align: 'center', edit: function (e) {
                    console.log(e);
                }, title: 'javaType'
            }
            , {field: 'dataType', align: 'center', title: 'jdbcType'}
            , {field: 'comment', align: 'center', title: '备注'}
        ]]
        , data: [{
            "id": "10001"
            , "username": "杜甫"
            , "email": "xianxin@layui.com"
            , "sex": "男"
            , "city": "浙江杭州"
            , "sign": "人生恰似一场修行"
            , "experience": "116"
            , "ip": "192.168.0.8"
            , "logins": "108"
            , "joinTime": "2016-10-14"
        }, {
            "id": "10002"
            , "username": "李白"
            , "email": "xianxin@layui.com"
            , "sex": "男"
            , "city": "浙江杭州"
            , "sign": "人生恰似一场修行"
            , "experience": "12"
            , "ip": "192.168.0.8"
            , "logins": "106"
            , "joinTime": "2016-10-14"
            , "LAY_CHECKED": true
        }]
        //,skin: 'line' //表格风格
        , even: true
    });
});
