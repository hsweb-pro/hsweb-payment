define(["components/data-selector"], function (selector) {

    return function (call) {
        var config = {
            url: "permission",
            columns: [
                {field: 'id', title: "标识", sort: true, width: "20%"},
                {field: 'name', title: "名称", width: "30%"},
                {field: 'describe', title: "备注", width: "30%"}
            ],
            search: [{
                label: '标识',
                column: 'id',
                type: 'input'
            }, {
                label: '名称',
                column: 'name',
                type: 'input'
            }]
        };
        selector(config, call);
    }

});