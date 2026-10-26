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

        // 关键：getRequestURI() 会带上 context-path，拿到的是 /api/student/info，
        // 用它去和 "/student" 这种前缀比永远不成立，于是所有接口都被当成静态资源放行，
        // request 里的 userId 恒为 null —— 下单、我的订单、我的在售、个人资料全都会失效。
        // getServletPath() 返回的是去掉 context-path 后的路径（/student/info），才是能比对的值。
        String requestURI = request1.getServletPath();


        // 游客就能访问的接口：登录、注册、集市列表、按名字搜索。
        // 它们不要求 token，但如果浏览器带了合法 token，也顺便解析出 userId
        //（集市列表要按 userId 排除自己发布的商品）。
        if (isPublic(requestURI)) {
            parseTokenQuietly(request1);
            chain.doFilter(request1, response1);
            return;
        }

        // 非接口请求直接放行：前端打包进 static 后，页面和 assets 是不带 token 访问的，
        // 如果这里拦下来，浏览器打开首页就只会看到 401。
        if (!isApiRequest(requestURI)) {
            chain.doFilter(request1, response1);
            return;
        }

        //登录失败
        String token=request1.getHeader("token");
        if (token==null || token.isEmpty()){
            refuse(response1, "未登录或登录已过期");
            return;
        }
        //解析userId找买家/卖家
        Integer userId = readUserId(request1);
        if (userId == null) {
            // token 假/过期时给 401，前端拦截器会清掉本地登录态并跳登录页；
            // 原来这里是直接抛异常，浏览器只会收到 500。

            refuse(response1, "登录已过期，请重新登录");
            return;
        }
        request1.setAttribute("userId", userId);
        chain.doFilter(request1,response1);
    }

    /** 带了合法 token 就返回 userId，否则返回 null（不抛异常） */
    private static Integer readUserId(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            Claims claims = JwtUtil.parseToken(token);
            return (Integer) claims.get("id");
        } catch (Exception e) {
            return null;
        }
    }

    /** 公开接口：token 有效就用，无效/没带也照常放行 */
    private static void parseTokenQuietly(HttpServletRequest request) {
        Integer userId = readUserId(request);
        if (userId != null) {
            request.setAttribute("userId", userId);
        }
    }

    /** 统一的 401 响应体，前端 http.js 会读 msg */
    private static void refuse(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(401);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"" + msg + "\"}");
    }

    /** 不需要 token 的接口 */
    private static final String[] PUBLIC_PATHS = {
            "/student/login",
            "/student/denlu",
            "/goods/mina",     // 集市列表，游客也能看
            "/goods/select"    // 按名字搜索
    };

    /** 需要 token 的接口前缀 */
    private static final String[] API_PREFIXES = {"/student", "/goods", "/orders"};

    private static boolean isPublic(String uri) {
        for (String path : PUBLIC_PATHS) {
            if (uri.equals(path) || uri.startsWith(path + "/")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isApiRequest(String uri) {
        for (String prefix : API_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
