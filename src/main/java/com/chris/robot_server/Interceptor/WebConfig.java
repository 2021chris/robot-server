package com.chris.robot_server.Interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Description：Interceptor拦截器配置类
 *
 * @author chris
 * @date 2021/8/18 10:43
 */
@Configuration
public class WebConfig  implements WebMvcConfigurer {
    @Autowired
    public LoginInterceptor loginInterceptor;


    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     registry.addMapping("/front/**")
    //             .allowedOrigins("http://recentlover.com","http://www.recentlover.com","https://recentlover.com","https://www.recentlover.com","https://kefu.recentlover.com") // 替换为你的前端域名
    //             .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    //             .allowedHeaders("*")
    //             .allowCredentials(true);
    // }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // .allowedOrigins("http://127.0.0.1:3000") // 替换为你的前端域名
                .allowedOriginPatterns("http://127.0.0.1:*", "http://localhost:*", "http://154.209.5.125:*", "http://palshort.net:*", "https://palshort.net:*", "https://tiaoma.palshort.com:*","null")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
    

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //拦截所有请求
        registry.addInterceptor(loginInterceptor);
    }
}
