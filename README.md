# 开源聚合支付平台

基于hsweb,spring-boot的开源聚合支付平台, 支持多渠道,多配置. 默认已接入: `微信扫码`,`微信H5`,`支付宝H5`,`支付宝网页支付`

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
----------open-api-demo             #OpenApi 例子
```

# 环境

最新版本的Java8

数据库支持: H2,Mysql,Oracle,PostgreSQL。

注意: 数据库表使用[hsweb-starter](https://docs.hsweb.io/framework/zeng-shan-gai-cha/shu-ju-ku-ban-ben-kong-zhi)
自动创建和版本维护，启动项目会自动创建表结构.


# 启动

## 使用docker启动
最新版本已经上传到docker公共仓库:

```bash
$ docker run -d -p 8080:8080 -v data:/data hsweb/hsweb-payment
```

自己构建镜像:

```bash
$ ./build-docker.sh
```

## 使用maven命令启动

```bash
$ ./mvnw install -DskipTests
$ ./mvnw -pl hsweb-payment-assemble spring-boot:run

```

## 使用IDE启动

直接运行模块:`hsweb-payment-assemble`下的类:`org.hswebframework.payment.PaymentApplication`

注意: 启动配置中的`working dir` 必须为项目的根目录(`hsweb-payment`),而不是`hsweb-payment-assemble`目录


# 用户手册

TODO

# ⚠️ 注意

本项目仅供开源学习,请勿用于商业用途,否者后果自负.