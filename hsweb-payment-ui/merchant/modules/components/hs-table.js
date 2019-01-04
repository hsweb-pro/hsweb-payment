define(["hsForm","request"], function (hsForm,request) {
    var element = layui.element,
        form = layui.form,
        laydate = layui.laydate,
        table = layui.table;

    //渲染搜索框的方法
    var renderForm = {
        input: function (item) {
            var formInput = $('<input type="text" class="layui-input">').attr('name', item.column);
            return formInput;
        },
        select: function (item) {
            var formInput = $('<select></select>').attr('name', item.column);
            formInput.append($('<option></option>'));
            item.options.forEach(function (opt) {
                formInput.append($('<option></option>').text(opt.text).attr('value', opt.value))
            });
            return formInput;
        },
        radio: function (item) {
            var formInput = $('<div>');
            item.options.forEach(function (opt) {
                formInput.append($('<input type="radio">').attr('title',opt.text).attr('value', opt.value).attr('name',item.column))
            });
            return formInput;
        },
        checkbox: function (item) {
            var formInput = $('<div>');
            item.options.forEach(function (opt) {
                formInput.append($('<input type="checkbox">').attr('title',opt.text).attr('name',item.column+'['+opt.value+']'))
            });
            return formInput;
        },
        switch: function (item) {
            var formInput = $('<input type="checkbox" lay-skin="switch">').attr('name',item.column).attr('lay-text',item.text);
            return formInput;
        },
        date: function (item) {
            var formInput = $('<input type="text" class="layui-input">').attr('name',item.column);
            return formInput;
        },
        hsSelect: function (item) {
            var formInput = $('<input hs-type="hsSelect">')
                .attr('hs-value',item.value)
                .attr('hs-text',item.text)
                .attr('hs-data',item.data)
                .attr('hs-name',item.column)
                .attr('hs-url',item.url)
                .attr('hs-selected',item.selected);
            return formInput;
        },
        hsSelectTree: function (item) {
            var formInput = $('<input hs-type="hsSelectTree">')
                .attr('hs-value',item.value)
                .attr('hs-idKey',item.idKey)
                .attr('hs-pIdKey',item.pIdKey)
                .attr('hs-nameKey',item.nameKey)
                .attr('hs-data',item.data)
                .attr('hs-name',item.column)
                .attr('hs-url',item.url)
                .attr('hs-selected',item.selected);
            return formInput;
        },
    }

    function init(id, containerId, url, cols, tools) {
        //查询
        if (tools && tools.search) {
            var wrap = $('<form class="layui-form layui-row custom-table-tool" id="form-' + containerId + '"></form>');
            var btnSearch = $('<a class="layui-btn" lay-submit lay-filter="search-' + containerId + '">查询</a>');
            var btnClear = $('<a class="layui-btn layui-btn-primary" id="clear-' + containerId + '">重置</a>').on('click', function () {
                document.getElementById("form-" + containerId).reset();
                tableInit.reload({
                    where: {},
                    sorts: [],
                    page: {
                        curr: 1
                    }
                });
            });
            tools.search.forEach(function (item) {
                var formItem = $('<div class="layui-col-lg3 custom-form-item"></div>');
                var formLabel = $('<label class="layui-form-label"></label>').text(item.label);
                var formInputWrap = $('<div class="layui-input-block"></div>');

                //渲染搜索框
                var formInput = renderForm[item.type](item);

                formInputWrap.append(formInput);
                formItem.append(formLabel).append(formInputWrap);
                wrap.append(formItem);

                if(item.type == 'date'){
                    var param = $.extend({},{elem:formInput[0]},item.options);
                    laydate.render(param);
                }
            });
            wrap.append($('<div class="layui-col-lg3 custom-form-item" style="text-align: right"></div>').append(btnSearch).append(btnClear));
            $('#tools-' + containerId).append(wrap);

            hsForm.init();
            form.render();
        }
        //工具栏
        if (tools && tools.btns) {
            tools.btns.forEach(function (item) {
                $('#tools-' + containerId).append($('<a class="layui-btn"></a>')
                    .text(item.name)
                    .addClass(item.class)
                    .on('click', function () {
                        item.callback && item.callback();
                    }));
            });
        }

        //表格
        var sorts = [];
        var tableInit = table.render({ //其它参数在此省略
            id: id,
            elem: '#container-' + containerId,
            url: window.API_BASE_PATH + url,
            cols: cols,
            ajaxSort: true,
            ajax: function (param, call) {
                request.get(url, param, function (res) {
                    if (res.status === 200) {
                        call.success(res);
                    }
                })
            },
            // height: 'full-210',
            //where: {token: 'sasasas', id: 123} //如果无需传递额外参数，可不加该参数
            //method: 'post' //如果无需自定义HTTP类型，可不加该参数
            request: {
                pageName: 'pageIndex' //页码的参数名称，默认：page
                , limitName: 'pageSize' //每页数据量的参数名，默认：limit
            },
            page: {
                curr: 1
            },
            limit:10,
            sorts: sorts,
            response: {
                statusName: 'status' //数据状态的字段名称，默认：code
                , statusCode: 200 //成功的状态码，默认：0
                , msgName: 'message' //状态信息的字段名称，默认：msg
                , countName: 'result.total' //数据总数的字段名称，默认：count
                , dataName: 'result.data' //数据列表的字段名称，默认：data
            }
        });

        //表格查询
        form.on('submit(search-' + containerId + ')', function (data) {
            var terms = [];
            $.each(data.field, function (k, i) {
                if (i) {
                    terms.push({
                        column: k,
                        value: '%' + i + '%',
                        type: 'and',
                        termType: 'like'
                    })
                }
            });
            tableInit.reload({
                where: encodeTerms(terms),
                sorts: [],
                page: {
                    curr: 1
                }
            });
            return false; //阻止表单跳转。如果需要表单跳转，去掉这段即可。
        });

        return tableInit;
    }

    //格式化terms
    function encodeTerms(data) {
        var queryParam = {};
        data.forEach(function (item, index) {
            for (var k in item) {
                queryParam["terms[" + (index) + "]." + k] = item[k];
            }
        });
        return queryParam;
    }

    var e = {
        init: init
    };

    return e;
});