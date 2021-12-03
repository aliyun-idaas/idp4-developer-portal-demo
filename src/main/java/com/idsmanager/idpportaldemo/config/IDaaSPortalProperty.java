package com.idsmanager.idpportaldemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import static com.idsmanager.idpportaldemo.Constants.*;

/**
 * 2021-05-11
 * <pre>
 * Portal 对 IDaaS 配置相关
 * </pre>
 *
 * @author Kuinie Fu
 */
@Configuration
public class IDaaSPortalProperty {

    private static String idaasHost;

    private static String appId;

    private static String apiKey;

    private static String apiSecret;

    private static String clientId;

    private static String clientSecret;

    private static String redirectUri;

    private static String portalLoginUrl;

    public IDaaSPortalProperty() {

    }

    public String getIdaasHost() {
        return idaasHost;
    }

    @Value("${idaas.host:}")
    public void setIdaasHost(String idaasHost) {
        IDaaSPortalProperty.idaasHost = idaasHost;
    }

    public String getAppId() {
        return appId;
    }

    @Value("${idaas.portal.oauth2.appId:}")
    public void setAppId(String appId) {
        IDaaSPortalProperty.appId = appId;
    }

    public String getApiKey() {
        return apiKey;
    }

    @Value("${idaas.portal.oauth2.apiKey:}")
    public void setApiKey(String apiKey) {
        IDaaSPortalProperty.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    @Value("${idaas.portal.oauth2.apiSecret:}")
    public void setApiSecret(String apiSecret) {
        IDaaSPortalProperty.apiSecret = apiSecret;
    }

    public String getClientId() {
        return clientId;
    }


    @Value("${idaas.portal.oauth2.clientId:}")
    public void setClientId(String clientId) {
        IDaaSPortalProperty.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    @Value("${idaas.portal.oauth2.clientSecret:}")
    public void setClientSecret(String clientSecret) {
        IDaaSPortalProperty.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    @Value("${idaas.portal.oauth2.redirect.uri:}")
    public void setRedirectUri(String redirectUri) {
        IDaaSPortalProperty.redirectUri = redirectUri;
    }

    @Value("${portal.login.url:}")
    public void setPortalLoginUrl(String portalLoginUrl) {
        IDaaSPortalProperty.portalLoginUrl = portalLoginUrl;
    }

    public String getPortalLoginUrl() {
        return portalLoginUrl;
    }

    public static String tokenUrl() {
        return idaasHost.endsWith("/") ? idaasHost.substring(0, idaasHost.length() - 1) + OAUTH_TOKEN_URL : idaasHost + OAUTH_TOKEN_URL;
    }

    public static String authorizeUrl() {
        return idaasHost.endsWith("/") ? idaasHost.substring(0, idaasHost.length() - 1) + AUTHORIZE_URL : idaasHost + AUTHORIZE_URL;
    }

    public static String userInfoUrl() {
        return idaasHost.endsWith("/") ? idaasHost.substring(0, idaasHost.length() - 1) + USER_INFO_URL : idaasHost + USER_INFO_URL;
    }

    public static String logoutUrl() {
        return idaasHost.endsWith("/") ? idaasHost.substring(0, idaasHost.length() - 1) + LOGOUT_URL : idaasHost + LOGOUT_URL;
    }

    public static String userSSOAppListUrl() {
        return idaasHost.endsWith("/") ? idaasHost.substring(0, idaasHost.length() - 1) + USER_AUTHORIZE_APP_LIST_URL : idaasHost + USER_AUTHORIZE_APP_LIST_URL;
    }
}
