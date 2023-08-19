package xyz.qy.imclient.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

//@Configuration
//public class RedissonConfig {
//
//    @Autowired
//    private RedisProperties redisProperties;
//
//    @Bean
//    public RedissonClient redissonClient(){
//        Config config = new Config();
//        String redisUrl = String.format("redis://%s:%s",redisProperties.getHost()+"",redisProperties.getPort()+"");
//        config.useSingleServer().setAddress(redisUrl).setPassword(redisProperties.getPassword());
//        config.useSingleServer().setDatabase(1);
//        config.useSingleServer().setConnectionMinimumIdleSize(10);
//        return Redisson.create(config);
//    }
//}

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient() throws IOException {
        Config config = Config.fromYAML(RedissonConfig.class.getClassLoader()
                .getResource("redission-config.yml"));
        return Redisson.create(config);
    }
}


