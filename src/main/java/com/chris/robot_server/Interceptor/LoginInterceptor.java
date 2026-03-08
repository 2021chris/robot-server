package com.chris.robot_server.Interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Description：
 *
 * @author chris
 * @date 2021/8/17 20:34
 */
@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    //在Controller执行之前调用，如果返回false，controller不执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            //获取方法上的注解
            Object o =  hm.getMethodAnnotation(LoginRequired.class);
            //获取类上的注解
            LoginRequired classLoginRequired = hm.getMethod().getDeclaringClass().getAnnotation(LoginRequired.class);
            //判断是否有注解
            if(o == null && classLoginRequired == null) {
                // 没有注解就放行
                return true;
            } else {
                // 判断是否登录
                String token = request.getHeader("token");

                if(token == null) { // 没有token
                    return false;
                }else {
                   
                }
            }
        }
        return true;
    }


    //controller执行之后，且页面渲染之前调用
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    //页面渲染之后调用，一般用于资源清理操作
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
