(function () {
    var FormParser = function () {
        this.config = {};
    };

    function openPersonSelector(call) {
        layui.layer.alert("张三");

        call({id: "admin", "name": "张三"});
    }

    FormParser.prototype.renderPersonSelector = function () {
        var elem = $(this.config.elem);
        var isReadonly = this.config.readonly;
        elem.find(".person-selector").each(function () {
            var me = $(this);
            if (isReadonly) {
                me.remove();
                return;
            }
            var input = me.parent().parent().find('input');
            me.unbind("click").on("click", function () {
                openPersonSelector(function (person) {
                    input.val(person.name);
                    input.attr(input.attr("value-property"), person.id);
                });
            });
        });
    };
    FormParser.prototype.render = function () {
        this.renderPersonSelector();

    };

    window.FormParser = FormParser;
})();