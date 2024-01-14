package xyz.qy.imserver.task;

import xyz.qy.imcommon.contant.RedisKey;
import xyz.qy.imcommon.enums.IMCmdType;
import xyz.qy.imcommon.model.IMRecvInfo;
import xyz.qy.imserver.netty.IMServerGroup;
import xyz.qy.imserver.netty.processor.AbstractMessageProcessor;
import xyz.qy.imserver.netty.processor.ProcessorFactory;
import xyz.qy.imserver.netty.ws.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PullUnreadGroupMessageTask extends  AbstractPullMessageTask {
    @Autowired
    private WebSocketServer WSServer;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void pullMessage() {
        // 从redis拉取未读消息
        String key = String.join(":",RedisKey.IM_UNREAD_GROUP_QUEUE,IMServerGroup.serverId+"");
        IMRecvInfo recvInfo = (IMRecvInfo)redisTemplate.opsForList().leftPop(key,10, TimeUnit.SECONDS);
        if(recvInfo != null){
            AbstractMessageProcessor processor = ProcessorFactory.createProcessor(IMCmdType.GROUP_MESSAGE);
            processor.process(recvInfo);
        }
    }
}
