layui.use(['element'],function (element) {
    var generator = new function () {

        this.build = function (data,dom) {
            this.buildToolBar(data.toolbar,dom)
        }

        this.buildToolBar = function (toolbar,dom) {
            var body = $('<ul class="layui-nav" lay-filter="toolbar"></ul>')
            $(toolbar).each(function () {
                var line = $('<li class="layui-nav-item"></li>')
                line.append('<a href="javascript:void(0);">'+((this.iconCls && this.iconCls != '') ? '<i class="layui-icon">'+this.iconCls+'</i>&nbsp;':'')+this.text+'</a>')
                if(this.type == 'menuButton'){
                    var dl = $('<dl class="layui-nav-child"></dl>')
                    for(var j = 0; j < this.children.length; j++){
                        dl.append('<dd><a href="javascript:void(0);">'+((this.children[j].iconCls && this.children[j].iconCls != '') ? '<i class="layui-icon">'+this.children[j].iconCls+'</i>&nbsp;':'')+this.children[j].text+'</a></dd>')
                    }
                    line.append(dl)
                }
                body.append(line)
            })
            $(dom ? dom : document.body).append(body)
            element.render('nav')
        }


    }


    window.setTimeout(function () {
        if(window.ready){
            window.ready(generator)
        }
    },500)
})