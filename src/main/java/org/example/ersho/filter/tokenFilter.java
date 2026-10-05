package org.example.ersho.filter;


import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.ersho.util.JwtUtil;

import java.io.IOException;

@WebFilter(urlPatterns ="/*")
public class tokenFilter implements Filter {


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request1=(HttpServletRequest) request;
        HttpServletResponse response1=(HttpServletResponse) response;

        String requestURI =request1.getRequestURI();

        if (requestURI.contains("/login")){
            chain.doFilter(request1,response1);
            return;
        }
        String token=request1.getHeader("token");
        if (token==null){
            response1.setStatus(402);
            return;
        }
        JwtUtil.parseToken(token);
        chain.doFilter(request1,response1);




    }
}