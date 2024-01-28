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

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Component
public class GroupMessageResultTask extends AbstractMessageResultTask {
    @Qualifier("IMRedisTemplate")
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${im.result.batch:100}")
    private int batchSize;

    @Autowired
    private MessageListenerMulticaster listenerMulticaster;

    @Override
    public void pullMessage() {
        List<IMSendResult> results;
        do {
            results = loadBatch();
            if (!results.isEmpty()) {
                listenerMulticaster.multicast(IMListenerType.GROUP_MESSAGE, results);
            }
        } while (results.size() >= batchSize);
    }

    private List<IMSendResult> loadBatch() {
        String key = StrUtil.join(":", IMRedisKey.IM_RESULT_GROUP_QUEUE, appName);
        //这个接口redis6.2以上才支持
        //List<Object> list = redisTemplate.opsForList().leftPop(key, batchSize);
        List<IMSendResult> results = new LinkedList<>();
        JSONObject jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key);
        while (!Objects.isNull(jsonObject) && results.size() < batchSize) {
            results.add(jsonObject.toJavaObject(IMSendResult.class));
            jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key);
        }
        return results;
    }
}
