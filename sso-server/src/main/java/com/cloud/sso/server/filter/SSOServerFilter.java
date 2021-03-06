package com.cloud.sso.server.filter;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cloud.sso.server.JVMCache;

public class SSOServerFilter implements Filter {

    public void destroy() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String service = request.getParameter("service");
        String ticket = request.getParameter("ticket");
        Cookie[] cookies = request.getCookies();
        String username = "";
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                if ("sso".equals(cookie.getName())) {
                    username = cookie.getValue();
                    break;
                }
            }
        }

        if (null == service && null != ticket) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (null != username && !"".equals(username)) {
            long time = System.currentTimeMillis();
            String timeString = username + time;
            JVMCache.TICKET_AND_NAME.put(timeString, username);
            StringBuilder url = new StringBuilder();
            if(service != null){
            url.append(service);
            if (0 <= service.indexOf("?")) {
                url.append("&");
            } else {
                url.append("?");
            }
            url.append("ticket=").append(timeString);
            response.sendRedirect(url.toString());
            }
            else
            {
               request.setAttribute("username", username);
               RequestDispatcher requestDispatcher = request.getRequestDispatcher("/jsp/welcome.jsp");
               requestDispatcher.forward(request, response);
            }
           
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    public void init(FilterConfig arg0) throws ServletException {
    }

}