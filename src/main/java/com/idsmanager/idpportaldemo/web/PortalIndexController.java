package com.idsmanager.idpportaldemo.web;

import com.idsmanager.idpportaldemo.config.IDaaSPortalProperty;
import com.idsmanager.idpportaldemo.model.SSOAppModel;
import com.idsmanager.idpportaldemo.util.HttpClient;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import static com.idsmanager.idpportaldemo.Constants.USER_AT_SESSION_KEY;
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
                        List<SSOAppModel> appModelList = SSOAppModel.toList(appListJSONArray, accessToken);
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


}
