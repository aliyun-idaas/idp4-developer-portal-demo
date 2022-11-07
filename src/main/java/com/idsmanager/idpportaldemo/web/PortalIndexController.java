package com.idsmanager.idpportaldemo.web;

import com.idsmanager.idpportaldemo.config.IDaaSPortalProperty;
import com.idsmanager.idpportaldemo.model.SSOAppModel;
import com.idsmanager.idpportaldemo.util.HttpClient;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.idsmanager.idpportaldemo.Constants.*;
import static com.idsmanager.idpportaldemo.config.IDaaSPortalProperty.userSSOAppListUrl;

/**
 * 2021-05-11
 * <pre>
 * Portal Index  Controller
 * </pre>
 *
 * @author Kuinie Fu
 */
@Controller
public class PortalIndexController {

    private final static Logger LOG = LoggerFactory.getLogger(PortalIndexController.class);

    @Autowired
    private IDaaSPortalProperty portalProperty;

    @RequestMapping({"/", "/index"})
    public String login(HttpServletRequest request, Model model) {

        String accessToken = (String) request.getSession().getAttribute(USER_AT_SESSION_KEY);
        if (StringUtils.isNotBlank(accessToken)) {

            //获取用户可单点登录的应用列表
            String url = userSSOAppListUrl();
            HttpClient getClient = new HttpClient(url).get();
            //此处展示access_token可放在请求头中,这样更安全
            getClient.addHeader("Authorization", "Bearer " + accessToken);

            try {
                String appListRet = getClient.execute();
                LOG.debug("Send Request url[{}] with result[{}]", url, appListRet);

                JSONObject jsonObject = JSONObject.fromObject(appListRet);
                //code为"200"视为成功
                if (jsonObject.containsKey("code") && "200".equals(jsonObject.getString("code"))) {
                    JSONObject appNodeData = jsonObject.containsKey("data") ? jsonObject.getJSONObject("data") : null;
                    if (null != appNodeData) {
                        JSONArray appListJSONArray = appNodeData.containsKey("authorizationApplications") ? appNodeData.getJSONArray("authorizationApplications") : new JSONArray();
                        List<SSOAppModel> appModelList = SSOAppModel.toList(appListJSONArray);
                        model.addAttribute("appModelList", appModelList);
                        model.addAttribute("idaasHost", portalProperty.getIdaasHost());
                    }
                }
            } catch (IOException e) {
                LOG.warn("Send Request url[" + url + "] with exception:", e);
            }

        }
        return "index";
    }


    @RequestMapping({"/startSso"})
    public void startSso(@RequestParam("ssoUrl") String ssoUrl,
                         HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String loginType = request.getSession().getAttribute(LOGIN_TYPE_SESSION_KEY).toString();
        if (LOGIN_TYPE_API.equals(loginType)) {
            //使用API登录
            //IDP签发的用户令牌access_token
            String accessToken = (String) request.getSession().getAttribute(USER_AT_SESSION_KEY);
            response.sendRedirect(getSsoUrl(ssoUrl, accessToken));
        } else {
            //使用IDP登录
            response.sendRedirect(ssoUrl);
        }
    }

    /**
     * SSO发起地址签名：
     * 步骤：
     * 1. 将所有URL参数通过key升序排序，并去掉空值，拼接成url参数形式（key+value&key=value）
     * 2. 将拼接后的字符串进行sha256加密，apiSecret当盐值
     * 3. 将签名参数放入参数中，并进行跳转
     */
    private String getSsoUrl(String ssoUrl, String accessToken) {
        Map<String, String> params = new HashMap<>(3);
        //前提： 准备参数
        //回调地址，非必传
        params.put("redirectUrl", "");
        //oauth token
        params.put("access_token", accessToken);
        //当前时间戳，注意SSO会校验，5分钟内有效
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));

        //step one : 将所有URL参数通过key升序排序，并去掉空值，拼接成url参数形式（key+value&key=value）
        final String paramsString = params.entrySet().stream()
                .filter(e -> StringUtils.isNotBlank(e.getValue()))
                .sorted(Map.Entry.comparingByKey(String::compareTo))
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        //step two : 将拼接后的字符串进行sha256加密，clientSecret当盐值
        //accessToken=123&timestamp=1667465505282 签名后值为 d827c016f336676e23affa583e985bcbcc4772ec4d370afd7b2dc98acaa85c9d

        String apiSecret = portalProperty.getApiSecret();
        //示例2 ,这里用commons-codec开源库
        final String sign = DigestUtils.sha256Hex(paramsString + apiSecret);
        //示例2，用java原生方法
//        final String sign2 = sha256Hex(paramsString, clientSecret);

        //step three : 将签名参数放入参数中，进行跳转
        //门户SSO发起地址，同原有发起地址，注意后面的UUID需要替换
        return ssoUrl + "?" + paramsString + "&sign=" + sign;
    }

    public static String sha256Hex(String password, String salt) {

        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());
            md.update(salt.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }
}
