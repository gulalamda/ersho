package org.example.ersho.filter;


import io.jsonwebtoken.Claims;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.example.ersho.util.JwtUtil;

import java.io.IOException;

@WebFilter(urlPatterns ="/*")
public class tokenFilter implements Filter {


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request1=(HttpServletRequest) request;
        HttpServletResponse response1=(HttpServletResponse) response;

        String requestURI =request1.getRequestURI();
        //login放过
        if (requestURI.contains("/login")){
            chain.doFilter(request1,response1);
            return;
        }
        //注册放行
        if (requestURI.contains("/denlu")){
            chain.doFilter(request1,response1);
            return;

        }
        //登录失败
        String token=request1.getHeader("token");
        if (token==null){
            response1.setStatus(401);
            return;
        }
        //解析userId找买家/卖家
        Claims claims = JwtUtil.parseToken(token);
        Integer userId = (Integer) claims.get("id");
        request1.setAttribute("userId", userId);




        JwtUtil.parseToken(token);
        chain.doFilter(request1,response1);




    }
}