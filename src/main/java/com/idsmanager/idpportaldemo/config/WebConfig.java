package com.idsmanager.idpportaldemo.config;

import com.idsmanager.idpportaldemo.SessionLoginFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

/**
 * 2021-05-11
 * <pre>
 * Web config 配置
 * </pre>
 *
 * @author Kuinie Fu
 */
@Configuration
public class WebConfig {

    /**
     * 字符编码 Filter,
     */
    @Bean
    public FilterRegistrationBean characterFilter() {
        final SessionLoginFilter loginFilter = new SessionLoginFilter();
        loginFilter.setForceEncoding(true);
        loginFilter.setEncoding("utf8");

        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(loginFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("loginFilter");
        registrationBean.setOrder(1);
        return registrationBean;
    }


}
