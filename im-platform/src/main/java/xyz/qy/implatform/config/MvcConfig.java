package xyz.qy.implatform.config;

import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import xyz.qy.implatform.config.intercept.PageableInterceptor;
import xyz.qy.implatform.interceptor.AuthInterceptor;
import xyz.qy.implatform.interceptor.XssInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(XssInterceptor())
                .addPathPatterns("/**");
        registry.addInterceptor(authInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/website/**", "/captchaImage", "/social/login/*" ,
                        "/login","/logout","/register","/refreshToken",
                        "/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**");
        //分页拦截器
        registry.addInterceptor(new PageableInterceptor());
    }


    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor();
    }

    @Bean
    public XssInterceptor XssInterceptor() {
        return new XssInterceptor();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        // 使用BCrypt加密密码
        return new BCryptPasswordEncoder();
    }
}
