importResource("/admin/css/common.css");

importMiniui(function () {
    mini.parse();

    require(["miniui-tools", "request"], function (tools, request) {
        var province = mini.get("firstCombo");
        // var city = mini.get("cityCombo");
        // var county = mini.get("countyCombo");

        request.createQuery("district/no-paging").where("parentId", "-1").orderByAsc("sortIndex")
            .exec(function (e) {
            province.setData(e.result);
        });


        function appendChild(e,level) {
            level=level||0;
            var comboboxId = randomChar(8);
            var html = $('<input class="mini-combobox" style="width: 150px;" onvaluechanged="dynamicValueChange" textField="name" valueField="id" showNullItem="true">')
                .attr("id",comboboxId)
                .attr("data-options",JSON.stringify({level:level+1}))


             request.createQuery("district/no-paging").where("parentId", e.value).orderByAsc("sortIndex")
                .exec(function (e) {
                    if (e.status===200) {
                        var data = e.result;
                        if(data.length>0){
                            $("#checkDistrict").append(html);

                            mini.parse();
                            mini.get(comboboxId).setData(e.result);
                        }else{
                            //$("#checkDistrict>span:gt("+(level)+")").remove();
                        }
                    }else {

                    }
                });
        }


        window.dynamicValueChange = function (e) {
            var source =e.sender;
            var setting=source["data-options"];
            var level=0;

            if(setting){
                level=JSON.parse(setting).level;
            }
            console.log(level);
            $("#checkDistrict>span:gt("+level+")").remove();
            appendChild(e,level);
        };

        window.valueChange = function (e) {
            //添加下级

            appendChild(e);
        };
    });
    function randomChar(len) {
        var chars = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];

        function generateMixed(n) {
            var res = "";
            for (var i = 0; i < n; i++) {
                var id = Math.ceil(Math.random() * 35);
                res += chars[id];
            }
            return res;
        }

        return generateMixed(len);
    }
});
//
// <input id="cityCombo" class="mini-combobox" style="width: 150px;" textField="name" valueField="id" onvaluechanged="checkCounty" showNullItem="true">
//
//     <input id="countyCombo" class="mini-combobox" style="width: 150px;" textField="name" valueField="id" showNullItem="true">