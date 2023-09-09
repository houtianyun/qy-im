package xyz.qy.imserver.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import xyz.qy.imcommon.contant.Constant;
import xyz.qy.imserver.listener.GroupMessageChannelListener;
import xyz.qy.imserver.listener.PrivateMessageChannelListener;

import javax.annotation.Resource;

@Configuration
public class RedisConfig {
    @Resource
    private RedisConnectionFactory factory;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 设置值（value）的序列化采用jackson2JsonRedisSerializer
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer());
        // 设置键（key）的序列化采用StringRedisSerializer。
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public Jackson2JsonRedisSerializer jackson2JsonRedisSerializer(){
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 解决jackson2无法反序列化LocalDateTime的问题
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.registerModule(new JavaTimeModule());
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        return jackson2JsonRedisSerializer;
    }

    /**
     * 监听器配置
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(privateMessageListenerAdapter(), privateMessageChannelTopic());
        container.addMessageListener(groupMessageListenerAdapter(), groupMessageChannelTopic());
        return container;
    }

    @Bean
    public MessageListenerAdapter privateMessageListenerAdapter() {
        return new MessageListenerAdapter(privateMessageChannelListener());
    }

    @Bean
    public MessageListenerAdapter groupMessageListenerAdapter() {
        return new MessageListenerAdapter(groupMessageChannelListener());
    }

    @Bean
    public PrivateMessageChannelListener privateMessageChannelListener() {
        return new PrivateMessageChannelListener();
    }

    @Bean
    public GroupMessageChannelListener groupMessageChannelListener() {
        return new GroupMessageChannelListener();
    }

    /**
     * 私聊消息redis主题
     */
    @Bean
    ChannelTopic privateMessageChannelTopic() {
        return new ChannelTopic(Constant.PRIVATE_MSG_TOPIC);
    }

    /**
     * 群聊消息redis主题
     */
    @Bean
    ChannelTopic groupMessageChannelTopic() {
        return new ChannelTopic(Constant.GROUP_MSG_TOPIC);
    }
}
