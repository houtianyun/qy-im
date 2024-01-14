package xyz.qy.imclient.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import xyz.qy.imclient.listener.GroupMsgSendResultChannelListener;
import xyz.qy.imclient.listener.PrivateMsgSendResultChannelListener;
import xyz.qy.imcommon.contant.IMConstant;

import javax.annotation.Resource;
import java.io.IOException;

@Configuration("IMRedisConfig")
public class RedisConfig {
    @Resource
    private RedisConnectionFactory factory;

//    @Bean("IMRedisTemplate")
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate();
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        // 设置值（value）的序列化采用jackson2JsonRedisSerializer
//        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer());
//        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer());
//        // 设置键（key）的序列化采用StringRedisSerializer。
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
//        redisTemplate.afterPropertiesSet();
//        return redisTemplate;
//    }
//
//    @Bean
//    public Jackson2JsonRedisSerializer jackson2JsonRedisSerializer() {
//        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
//        ObjectMapper om = new ObjectMapper();
//        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        // 解决jackson2无法反序列化LocalDateTime的问题
//        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
//        jackson2JsonRedisSerializer.setObjectMapper(om);
//        return jackson2JsonRedisSerializer;
//    }

    @Bean("IMRedisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 设置值（value）的序列化采用FastJsonRedisSerializer
        redisTemplate.setValueSerializer(fastJsonRedisSerializer());
        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer());
        // 设置键（key）的序列化采用StringRedisSerializer。
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    public FastJsonRedisSerializer fastJsonRedisSerializer(){
        FastJsonRedisSerializer <Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);
        return fastJsonRedisSerializer;
    }

    /**
     * 监听器配置
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(privateMsgSendResultListenerAdapter(), privateMsgSendResultChannelTopic());
        container.addMessageListener(groupMsgSendResultListenerAdapter(), groupMsgSendResultChannelTopic());
        return container;
    }

    @Bean
    public MessageListenerAdapter privateMsgSendResultListenerAdapter() {
        return new MessageListenerAdapter(privateMsgSendResultChannelListener());
    }

    @Bean
    public MessageListenerAdapter groupMsgSendResultListenerAdapter() {
        return new MessageListenerAdapter(groupMsgSendResultChannelListener());
    }

    @Bean
    public PrivateMsgSendResultChannelListener privateMsgSendResultChannelListener() {
        return new PrivateMsgSendResultChannelListener();
    }

    @Bean
    public GroupMsgSendResultChannelListener groupMsgSendResultChannelListener() {
        return new GroupMsgSendResultChannelListener();
    }

    /**
     * 私聊消息发送结果redis主题
     */
    @Bean
    ChannelTopic privateMsgSendResultChannelTopic() {
        return new ChannelTopic(IMConstant.PRIVATE_MSG_SEND_RESULT_TOPIC);
    }

    /**
     * 群聊消息发送结果redis主题
     */
    @Bean
    ChannelTopic groupMsgSendResultChannelTopic() {
        return new ChannelTopic(IMConstant.GROUP_MSG_SEND_RESULT_TOPIC);
    }

    @Bean
    public RedissonClient redissonClient() throws IOException {
        Config config = Config.fromYAML(RedisConfig.class.getClassLoader()
                .getResource("redission-config.yml"));
        return Redisson.create(config);
    }
}
