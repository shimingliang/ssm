package com.sml.websockt.util;

import com.sml.websockt.pojo.User;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 用户权限过滤器
 */
public class PermissionFilter implements Filter {

    private String[] ignorePages;

    public void init(FilterConfig config) throws ServletException {
        String ignorePage = config.getInitParameter("ignore_page");
        if (ignorePage != null)
            this.ignorePages = ignorePage.split(",");
    }

    public void doFilter(ServletRequest req, ServletResponse resp,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        HttpSession session = request.getSession();

        String servletPath = request.getServletPath();

        User user = (User) session.getAttribute(Constants.USER_INFO);
        // 校验是否验证会话
        if (null == user) {
            if (servletPath.contains(".css") ||
                    servletPath.contains(".js") ||
                    servletPath.contains(".jpg") ||
                    servletPath.contains(".png")) {

                chain.doFilter(req, resp);
                return;
            }

            if (isIgnorePage(request)) {
                chain.doFilter(req, resp);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/user/turnToLogin");
        } else {
            chain.doFilter(req, resp);
        }
    }

    private boolean isIgnorePage(HttpServletRequest request) {
        if (this.ignorePages == null) {
            return false;
        }

        String servletPath = request.getServletPath();
        for (int i = 0; i < this.ignorePages.length; i++) {

            if (this.ignorePages[i].endsWith("*")) {
                int lastIndex = servletPath.lastIndexOf("/");
                String compareUrl = null;
                if (lastIndex == 0)
                    compareUrl = "/*";
                else {
                    compareUrl = servletPath.substring(0, lastIndex) + "/*";
                }
                if (this.ignorePages[i].endsWith(compareUrl)) {
                    return true;
                }
            } else if (this.ignorePages[i].equals(servletPath)) {
                return true;
            }
        }
        return false;
    }

    public void destroy() {
    }
}