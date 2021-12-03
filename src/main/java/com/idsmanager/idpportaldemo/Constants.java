package com.idsmanager.idpportaldemo;

/**
 * 2021-05-11
 * <pre>
 * 静态变量
 * </pre>
 *
 * @author Kuinie Fu
 */
public interface Constants {

    String OAUTH_TOKEN_URL = "/oauth/token";

    String AUTHORIZE_URL = "/oauth/authorize";

    String USER_INFO_URL = "/api/bff/v1.2/oauth2/userinfo";

    String LOGOUT_URL = "/public/sp/slo/";

    String USER_AUTHORIZE_APP_LIST_URL = "/api/bff/v1.2/enduser/portal/sso/app_list";

    String USERNAME_SESSION_KEY = "username";

    String USER_AT_SESSION_KEY = "user_access_token";

    /**
     * 登录方式Session Key
     */
    String LOGIN_TYPE_SESSION_KEY = "loginType";

    /**
     * 调用IDaaS-API登录
     */
    String LOGIN_TYPE_API = "idaas-api";

    /**
     * 去IDaaS登录页登录
     */
    String LOGIN_TYPE_PAGE = "idaas-page";
}
