# ${api.name}

${api.description}

#  公共参数

## 请求地址
|   环境  | 请求地址 |
| :------:| :------: |<% for(env in api.envs) {%>
|  ${env.name} | ${env.url}  |<%}%>

## 公共请求参数

| 参数 | 参数名称 | 类型 | 必填 | 加密 | 参数说明 | 示例 |
| :------:| ------: | :------: | :------: |:------: |:------: |:------: |<% for(commonParam in api.commonRequestParams) {%>
| ${commonParam.id} | ${commonParam.name} | ${commonParam.type} | ${commonParam.required} | ${commonParam.enc} | ${commonParam.description} | ${commonParam.example} |<%}%>

## 请求参数
| 参数 | 参数名称 | 类型 | 必填 | 加密 | 参数说明 | 示例 |
| :------:| ------: | :------: | :------: |:------: |:------: |:------: |<% for(param in api.requestParams) {%>
| ${param.id} | ${param.name} | ${param.type} | ${param.required} | ${param.enc} | ${param.description} | ${param.example} |<% }%>

## 公共响应参数
| 参数 | 参数名称 | 类型  | 加密 | 参数说明 | 示例 |
| :------:| ------: | :------: | :------: |:------: |:------: |:------: |<% for(commonParam in api.commonResponseParams) {%>
| ${commonParam.id} | ${commonParam.name} | ${commonParam.type} |  ${commonParam.enc} | ${commonParam.description} | ${commonParam.example} |<% } %>

## 响应参数
| 参数 | 参数名称 | 类型 |  加密 | 参数说明 | 示例 |
| :------:| ------: | :------: | :------: |:------: |:------: |:------: |<% for(param in api.responseParams) {%>
| ${param.id} | ${param.name} | ${param.type} |  ${param.enc} | ${param.description} | ${param.example} |<% }%>

# 请求示例
