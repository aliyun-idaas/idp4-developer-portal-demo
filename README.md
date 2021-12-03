# idp4-developer-portal-demo
IDP4门户(Portal)集成demo

# Read Me First
请先在IDaaS创建一个oauth2应用，然后
找到项目配置文件application.properties,
补充完善下面对应的idaas相关配置参数

<pre>
#此处为idaas host地址和平台上oauth2应用的信息
idaas.host=
idaas.portal.oauth2.appId=ceshiplugin_oauth2
</pre>

<pre>
#API Key和 API Secret为调用IDaaS的接口登录用
idaas.portal.oauth2.apiKey=
idaas.portal.oauth2.apiSecret=
</pre>

<pre>
#Client Id 和 Client Secret ,Redirect URI 为拼接OAuth2应用授权地址（第一步）以及使用code获取用户access_token使用（第二步）
idaas.portal.oauth2.clientId=
idaas.portal.oauth2.clientSecret=
idaas.portal.oauth2.redirect.uri=http://127.0.0.1:8080/public/code_callback
</pre>

<pre>
#此处为门户登录地址
portal.login.url=http://127.0.0.1:8080/login
</pre>
