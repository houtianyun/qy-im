package xyz.qy.imclient.task;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.qy.imclient.listener.MessageListenerMulticaster;
import xyz.qy.imcommon.contant.IMRedisKey;
import xyz.qy.imcommon.enums.IMListenerType;
import xyz.qy.imcommon.model.IMSendResult;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PrivateMessageResultResultTask extends AbstractMessageResultTask {
    @Qualifier("IMRedisTemplate")
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MessageListenerMulticaster listenerMulticaster;

    @Override
    public void pullMessage() {
        String key = IMRedisKey.IM_RESULT_PRIVATE_QUEUE;
        JSONObject jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key, 10, TimeUnit.SECONDS);

        if (jsonObject != null) {
            IMSendResult result = jsonObject.toJavaObject(IMSendResult.class);
            listenerMulticaster.multicast(IMListenerType.PRIVATE_MESSAGE, result);
        }
    }
}
