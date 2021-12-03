package com.idsmanager.idpportaldemo;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.thymeleaf.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.idsmanager.idpportaldemo.Constants.USERNAME_SESSION_KEY;

/**
 * 2021-05-11
 * <pre>
 * session 会话 filter
 * </pre>
 *
 * @author Kuinie Fu
 */
public class SessionLoginFilter extends CharacterEncodingFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI().replaceFirst(request.getContextPath(), "");
        if (ignored(uri)) {
            super.doFilterInternal(request, response, filterChain);
            return;
        }
        String username = (String) request.getSession().getAttribute(USERNAME_SESSION_KEY);
        if (StringUtils.isEmpty(username)) {
            response.sendRedirect("/login");
            return;
        }
        super.doFilterInternal(request, response, filterChain);
    }

    private boolean ignored(String uri) {
        return uri.startsWith("/login") || uri.startsWith("/go_idaas_login") || uri.startsWith("/static") || uri.startsWith("/public");
    }
}
