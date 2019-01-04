# 开源聚合支付平台

基于hsweb,spring-boot的开源聚合支付平台, 支持多渠道,多配置. 默认已接入: `微信扫码`,`微信H5`,`支付宝H5`,`支付宝网页支付`

# 环境

最新的java8

数据库支持: h2,mysql,oracle

# 模块
```bash
------hsweb-payment
----------hsweb-payment-account     #资金账户模块
----------hsweb-payment-api         #公共api接口模块
----------hsweb-payment-assemble    #启动模块
----------hsweb-payment-logging     #日志模块
----------hsweb-payment-merchant    #商户模块
----------hsweb-payment-openapi     #开放平台
----------hsweb-payment-pay         #支付模块
----------hsweb-payment-ui          #前端UI模块
```


# 启动

运行`hsweb-payment-assemble`模块下`PaymentApplication`

# 配置

TODO

# 二次开发

TODO

# 参与项目
TODO

# ⚠️ 注意

本项目仅供开源学习,请勿用于商业用途,否者后果自负.