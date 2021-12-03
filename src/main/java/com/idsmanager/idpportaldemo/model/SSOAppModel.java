package com.idsmanager.idpportaldemo.model;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 2021-05-12
 * <pre>
 * 单点登录的应用信息
 * </pre>
 *
 * @author Kuinie Fu
 */
public class SSOAppModel implements Serializable {

    private static final long serialVersionUID = -1689854121219910304L;

    private String name;

    private String applicationId;

    private String applicationUuid;

    private String idpApplicationId;

    private String logoUuid;

    private String startUrl;

    private String createTime;

    private String description;

    private boolean enabled;

    private String supportDeviceTypes;

    private boolean existAccountLinking;

    private boolean enableTwoFactor;

    private boolean display;

    private boolean defaultLinking;

    private boolean autoLogin;

    private int orderId;


    public SSOAppModel() {
    }

    public SSOAppModel(JSONObject obj, String accessToken) {
        this.name = obj.optString("name");
        this.applicationId = obj.optString("applicationId");
        this.applicationUuid = obj.optString("applicationUuid");
        this.idpApplicationId = obj.optString("idpApplicationId");
        this.logoUuid = obj.optString("logoUuid");


        this.startUrl = obj.optString("startUrl");
        //此处处理下access_token,有可能IDaaS返回startUrl的时候就已经带上了
        this.startUrl = startUrl.contains("access_token") ? startUrl : startUrl + "?access_token=" + accessToken;

        this.createTime = obj.optString("createTime");
        this.description = obj.optString("description");
        this.enabled = obj.optBoolean("enabled");
        this.supportDeviceTypes = obj.getJSONArray("supportDeviceTypes").toString();
        this.existAccountLinking = obj.optBoolean("existAccountLinking");
        this.enableTwoFactor = obj.optBoolean("enableTwoFactor");
        this.display = obj.optBoolean("display");
        this.defaultLinking = obj.optBoolean("defaultLinking");
        this.autoLogin = obj.optBoolean("autoLogin");
        this.orderId = obj.optInt("orderId");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationUuid() {
        return applicationUuid;
    }

    public void setApplicationUuid(String applicationUuid) {
        this.applicationUuid = applicationUuid;
    }

    public String getIdpApplicationId() {
        return idpApplicationId;
    }

    public void setIdpApplicationId(String idpApplicationId) {
        this.idpApplicationId = idpApplicationId;
    }

    public String getLogoUuid() {
        return logoUuid;
    }

    public void setLogoUuid(String logoUuid) {
        this.logoUuid = logoUuid;
    }

    public String getStartUrl() {
        return startUrl;
    }

    public void setStartUrl(String startUrl) {
        this.startUrl = startUrl;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSupportDeviceTypes() {
        return supportDeviceTypes;
    }

    public void setSupportDeviceTypes(String supportDeviceTypes) {
        this.supportDeviceTypes = supportDeviceTypes;
    }

    public boolean isExistAccountLinking() {
        return existAccountLinking;
    }

    public void setExistAccountLinking(boolean existAccountLinking) {
        this.existAccountLinking = existAccountLinking;
    }

    public boolean isEnableTwoFactor() {
        return enableTwoFactor;
    }

    public void setEnableTwoFactor(boolean enableTwoFactor) {
        this.enableTwoFactor = enableTwoFactor;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public boolean isDefaultLinking() {
        return defaultLinking;
    }

    public void setDefaultLinking(boolean defaultLinking) {
        this.defaultLinking = defaultLinking;
    }

    public boolean isAutoLogin() {
        return autoLogin;
    }

    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public static List<SSOAppModel> toList(JSONArray appListJSONArray, String accessToken) {
        Iterator it = appListJSONArray.iterator();
        List<SSOAppModel> modelList = new ArrayList<>(appListJSONArray.size());
        while (it.hasNext()) {
            JSONObject obj = (JSONObject) it.next();
            modelList.add(new SSOAppModel(obj, accessToken));
        }
        return modelList;
    }
}
