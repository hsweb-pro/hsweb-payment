define(["request"], function (request) {

    var tmp = {};
    var merchantInfo = {
        idCardFront: null,
        idCardBack: null,
        businessLicense: null
    };


    Object.defineProperties(merchantInfo, {

        idCardFront: {
            get: function () {
                return tmp.idCardFront;
            },
            set: function (url) {
                tmp.idCardFront = url;
                $(".card-front-btn").show();
            }
        },
        idCardBack: {
            get: function () {
                return tmp.idCardBack;
            },
            set: function (url) {
                tmp.idCardBack = url;
                $(".card-back-btn").show();
            }
        },
        businessLicense: {
            get: function () {
                return tmp.businessLicense;
            },
            set: function (url) {
                tmp.businessLicense = url;
                $(".business-license-btn").show();
            }
        },

    });


    function init(containerId) {
        require(["text!pages/person-info/base/list.html"], function (html) {
            $("#container-" + containerId).html($(html));
            layui.element.init();
            loadData()
        });
    }


    function loadData() {
        var id = "";
        layui.use(['form', 'layedit', 'laydate', 'upload'], function () {
            var form = layui.form
                , layer = layui.layer;

            request.get("current-merchant/base-info", function (data) {
                form.val('merchant-info', data.result);
                form.val('merchant-contact', data.result);

                var datas = data.result;
                datas.idCardFront =  datas.idCardFront || "./default.png";
                datas.idCardBack  = datas.idCardBack || "./default.png";
                datas.businessLicense = datas.businessLicense ||"./default.png";

                $('#idCardFrontView').attr('src', merchantInfo.idCardFront);
                $('#idCardBackView').attr('src', merchantInfo.idCardFront);
                $('#businessLicenseView').attr('src', merchantInfo.idCardFront);
                id = data.result.id;
            });

            //监听提交备案信息
            form.on('submit(submit-merchant-info)', function (data) {
                data.field.id = id;
                var param = data.field;
                param.idCardFront=merchantInfo.idCardFront;
                param.idCardBack = merchantInfo.idCardBack;
                param.businessLicense = merchantInfo.businessLicense;
                request.put("current-merchant/main-info", param, function (data) {
                    if (data.status === 200) {
                        layer.alert("更新成功");
                    } else {
                        layer.alert("更新失败");
                    }
                });
                return false;
            });

            var upload = layui.upload;

            $(".card-front-view").on("click", function () {
                layer.open({
                    type: 1,
                    content: '<img src="' + merchantInfo.idCardFront + '">',
                })
            });
            $(".card-back-view").on("click", function () {
                layer.open({
                    type: 1,
                    content: '<img src="' + merchantInfo.idCardBack + '">',
                })
            });
            $(".business-license-view").on("click", function () {
                layer.open({
                    type: 1,
                    content: '<img src="' + merchantInfo.businessLicense + '">',
                })
            });


            request.get("current-merchant/status",function (e) {
                if (e.status ===200){
                    if (e.result==="REVIEW_COMPLETED"){
                        // style="background-color: #F8F8F8" disabled="disabled"
                        $("#merchant-base-info input")
                            .attr("readOnly","readOnly")
                            .css("background-color","#F8F8F8");
                        $("#base-info-btn").hide();
                    }else {
                        //上传身份证正面
                        var uploadIdCardFront = upload.render({
                            elem: '#idCardFront',
                            url: '/file/upload-static',
                            accept: 'images',
                            before: function (obj) {//文件上传前的回调
                                //预读本地文件示例，不支持ie8
                                obj.preview(function (index, file, result) {
                                    $('#idCardFrontView').attr('src', result); //图片链接（base64）直接将图片地址赋值给img的src属性
                                });
                            },
                            done: function (res) {
                                var idCardFront = res.result;
                                merchantInfo.idCardFront = idCardFront;
                            },
                            error: function () {
                                console.log("上传失败！");
                            }
                        });

                        //上传身份证背面
                        var uploadIdCardBack = upload.render({
                            elem: '#idCardBack',
                            url: '/file/upload-static',
                            accept: 'images',
                            before: function (obj) {//文件上传前的回调
                                //预读本地文件示例，不支持ie8
                                obj.preview(function (index, file, result) {
                                    $('#idCardBackView').attr('src', result); //图片链接（base64）直接将图片地址赋值给img的src属性
                                });
                            },
                            done: function (res) {
                                var idCardBack = res.result;
                                merchantInfo.idCardBack = idCardBack;
                            },
                            error: function () {
                                console.log("上传失败！");
                            }
                        });

                        //上传营业执照
                        var uploadBusinessLicense = upload.render({
                            elem: '#businessLicense',
                            url: '/file/upload-static',
                            accept: 'images',
                            before: function (obj) {//文件上传前的回调
                                //预读本地文件示例，不支持ie8
                                obj.preview(function (index, file, result) {
                                    $('#businessLicenseView').attr('src', result); //图片链接（base64）直接将图片地址赋值给img的src属性
                                });
                            },
                            done: function (res) {
                                var license = res.result;
                                merchantInfo.businessLicense = license;
                            },
                            error: function () {
                                console.log("上传失败！");
                            }
                        });
                    }
                }
            });
        });

    }


    return {init: init}
});