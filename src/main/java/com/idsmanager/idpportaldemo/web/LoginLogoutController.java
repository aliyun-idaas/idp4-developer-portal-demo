package com.idsmanager.idpportaldemo.web;

import com.idsmanager.idpportaldemo.config.IDaaSPortalProperty;
import com.idsmanager.idpportaldemo.util.HttpClient;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

import static com.idsmanager.idpportaldemo.Constants.*;
import static com.idsmanager.idpportaldemo.config.IDaaSPortalProperty.*;

/**
 * 2021-05-11
 * <pre>
 * 登录和退出
 * </pre>
 *
 * @author Kuinie Fu
 */
@Controller
public class LoginLogoutController {

    private final static Logger LOG = LoggerFactory.getLogger(LoginLogoutController.class);

    @Autowired
    private IDaaSPortalProperty portalProperty;


    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 调用IDaaS的接口登录
     *
     * @param username 用户名
     * @param pwd      密码
     * @return
     */
    @PostMapping("/login")
    public String login(@RequestParam("user_portal") String username, @RequestParam("pWd_portal") String pwd, HttpServletRequest request) {

        request.getSession().setAttribute(LOGIN_TYPE_SESSION_KEY, LOGIN_TYPE_API);

        String fullLoginApiUrl = tokenUrl() + "?client_id=%s&client_secret=%s&grant_type=password&username=%s&password=%s";
        String apiKey = portalProperty.getApiKey();
        String apiSecret = portalProperty.getApiSecret();
        String url = String.format(fullLoginApiUrl, apiKey, apiSecret, username, pwd);
        HttpClient loginClient = new HttpClient(url).post();

        try {
            String ret = loginClient.execute();
            LOG.debug("Send Request url[{}] with result[{}]", url, ret);

            JSONObject jsonObject = JSONObject.fromObject(ret);
            //若包含用户的access_token,则为登录成功
            if (jsonObject.containsKey("access_token")) {
                request.getSession().setAttribute(USERNAME_SESSION_KEY, username);
                request.getSession().setAttribute(USER_AT_SESSION_KEY, jsonObject.getString("access_token"));
                return "redirect:/index";
            }

        } catch (IOException e) {
            LOG.warn("Send Request url[" + url + "] with exception:", e);
        }


        String error = "Invalid Username Or Password!";
        return "redirect:/login?error=" + error;
    }


    /**
     * step1 > 去idaas页面登录
     *
     * @return idaas oauth2授权码模式下的登录页
     */
    @GetMapping("/go_idaas_login")
    public void goIDaaSPageLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {

        request.getSession().setAttribute(LOGIN_TYPE_SESSION_KEY, LOGIN_TYPE_PAGE);

        String fullAuthorizeUrl = authorizeUrl() + "?response_type=code&scope=read&client_id=%s&redirect_uri=%s&state=%s";
        String clientId = portalProperty.getClientId();
        String redirectUri = URLEncoder.encode(portalProperty.getRedirectUri(), "utf8");
        //state参数可由门户自定义,随机生成等,idaas在用户登录成功后会随code参数一起原封不动的带回给门户
        String state = UUID.randomUUID().toString();
        String url = String.format(fullAuthorizeUrl, clientId, redirectUri, state);

        response.sendRedirect(url);
    }


    /**
     * step2 > idaas回调至门户此地址，并带上用户登录后临时授权码,门户可通过该code换取用户的access_token
     *
     * @param code     用户登录后授权码code
     * @param state    step1 的随机值
     * @param response
     * @return
     */
    @GetMapping("/public/code_callback")
    public String codeCallBack(@RequestParam String code, @RequestParam(required = false) String state, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        LOG.debug("Received IDaaS callback request:[code={},state={}]", code, state);

        //通过code换取用户access_token
        String codeGetTokenUrl = tokenUrl() + "?grant_type=authorization_code&code=%s&client_id=%s&client_secret=%s&redirect_uri=%s";

        String clientId = portalProperty.getClientId();
        String clientSecret = portalProperty.getClientSecret();
        String redirectUri = URLEncoder.encode(portalProperty.getRedirectUri(), "utf8");

        String url = String.format(codeGetTokenUrl, code, clientId, clientSecret, redirectUri);
        HttpClient tokenClient = new HttpClient(url).post();


        try {
            String ret = tokenClient.execute();
            LOG.debug("Send Request url[{}] with result[{}]", ret);


            JSONObject jsonObject = JSONObject.fromObject(ret);
            //若包含用户的access_token,则为登录成功
            if (jsonObject.containsKey("access_token")) {

                //step3 (可选) 此处可根据access_token去换取用户信息
                JSONObject userInfo = getUserInfo(jsonObject.getString("access_token"));
                if (null != userInfo && userInfo.containsKey("data")) {
                    JSONObject dataNode = userInfo.getJSONObject("data");
                    if (dataNode.containsKey("username")) {
                        request.getSession().setAttribute(USERNAME_SESSION_KEY, dataNode.getString("username"));
                        request.getSession().setAttribute(USER_AT_SESSION_KEY, jsonObject.getString("access_token"));
                        return "redirect:/index";
                    }
                }
            }

        } catch (IOException e) {
            LOG.warn("Send Request url[" + url + "] with exception:", e);
        }

        return "redirect:/login?error=Get user info failed, please try again!";
    }

    /**
     * 通过用户访问令牌access_token获取用户在IDaaS上的详情，返回数据格式示例如下
     * <pre>
     * {
     * "success":true,
     * "code":"200",
     * "message":null,
     * "requestId":"1620809903547$bb49ee90-7ba4-476e-a369-a13e781a5b48",
     * "data":{
     * "sub":"2528738322870",
     * "ou_id":"5394213545557",
     * "nickname":"默认管理员",
     * "phone_number":"86 13811111111",
     * "ou_name":"内网测试环境",
     * "email":"wangli@q.com",
     * "username":"wangli"
     * }
     * }
     * </pre>
     *
     * @param accessToken 用户访问令牌
     * @return 用户详细信息
     */
    private JSONObject getUserInfo(String accessToken) {
        String userInfoUrl = userInfoUrl() + "?access_token=%s";
        String url = String.format(userInfoUrl, accessToken);
        HttpClient userInfoClient = new HttpClient(url).get();
        try {
            String ret = userInfoClient.execute();
            return JSONObject.fromObject(ret);
        } catch (IOException e) {
            LOG.warn("Send Request url[" + url + "] with exception:", e);
        }
        return null;
    }


    /**
     * 仅退出门户自己的会话（此种方式对应为门户调用IDaaS接口登录的，因此无需退出IDaaS和浏览器的会话）
     *
     * @param request
     * @return
     */
    @PostMapping("/signout_self")
    public String signoutSelf(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/login";
    }

    /**
     * 退出退出门户自己的会话,并退出IDaaS的会话,IDaaS会重定向至当前门户的登录页
     * （此种方式对应为门户使用IDaaS登录页进行登录的，因此需要退出IDaaS和浏览器的会话）
     * 注意该地址必须通过浏览器地址栏访问或form表单提交
     *
     * @param request
     * @return
     */
    @PostMapping("/signout_self_with_idaas")
    public void signoutSelfWithIDaaS(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String appId = portalProperty.getAppId();

        //IDaaS退出成功后跳转的URL地址，可选（若不传或为空则跳转回IDP登录页），此处传递门户登录地址
        String redirectUrl = URLEncoder.encode(portalProperty.getPortalLoginUrl(), "utf8");

        //IDP签发的用户令牌access_token，可选，若有值则将access_token置为无效状态
        String accessToken = (String) request.getSession().getAttribute(USER_AT_SESSION_KEY);

        String logoutIDaaSUrl = logoutUrl() + appId + "?redirect_url=%s&access_token=%s";

        String url = String.format(logoutIDaaSUrl, redirectUrl, accessToken);

        //清除自身会话
        request.getSession().invalidate();

        //调用IDaaS退出url地址
        response.sendRedirect(url);
    }


}
