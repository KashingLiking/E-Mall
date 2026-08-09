package com.example.emall.configure;

import com.example.emall.exception.EMallException;
import com.example.emall.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    TokenUtil tokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        String uri = request.getRequestURI();
        String method = request.getMethod();
        if("/api/accounts".equals(uri) && "POST".equalsIgnoreCase(method)){
            return true;
        }
        // 公开接口：GET /api/seckill 查看秒杀活动列表无需登录
        if("/api/seckill".equals(uri) && "GET".equalsIgnoreCase(method)){
            return true;
        }
        if (token != null && tokenUtil.verifyToken(token)) {
            request.getSession().setAttribute("currentAccount",tokenUtil.getAccount(token));
            return true;
        }else {
            throw EMallException.notLogin();
        }
    }

}
