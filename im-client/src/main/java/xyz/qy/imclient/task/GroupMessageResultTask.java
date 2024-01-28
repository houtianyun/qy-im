package xyz.qy.imclient.task;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.qy.imclient.listener.MessageListenerMulticaster;
import xyz.qy.imcommon.contant.IMRedisKey;
import xyz.qy.imcommon.enums.IMListenerType;
import xyz.qy.imcommon.model.IMSendResult;

import java.util.concurrent.TimeUnit;

@Component
public class GroupMessageResultTask extends AbstractMessageResultTask {
    @Qualifier("IMRedisTemplate")
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private MessageListenerMulticaster listenerMulticaster;

    @Override
    public void pullMessage() {
        String key = StrUtil.join(":", IMRedisKey.IM_RESULT_GROUP_QUEUE, appName);
        JSONObject jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key, 10, TimeUnit.SECONDS);
        if (jsonObject != null) {
            IMSendResult result = jsonObject.toJavaObject(IMSendResult.class);
            listenerMulticaster.multicast(IMListenerType.GROUP_MESSAGE, result);
        }
    }
}
