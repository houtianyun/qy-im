package xyz.qy.implatform;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan(basePackages = {"xyz.qy.implatform.mapper"})
@SpringBootApplication
public class ImplatformApp {
    public static void main(String[] args) {
        SpringApplication.run(ImplatformApp.class,args);
    }
}
