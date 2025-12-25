package com.jhzhao.alibaba.springai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class WebMvcConfig {

    /**
     * 解决跨域问题
     * @return
     */
    @Bean
    public CorsFilter corsFilter(){
        CorsConfiguration corsConfig = new CorsConfiguration();
        //设置允许的请求的匹配模式
        corsConfig.setAllowedOriginPatterns(List.of("*"));
        corsConfig.addAllowedOrigin("http://localhost:5173"); // 精确指定前端 origin，开发时用这个
        //设置允许的请求方法
        corsConfig.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        //设置头 header
        corsConfig.setAllowedHeaders(List.of("*"));
        //设置允许可信
        corsConfig.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsFilter(source);
    }
}
