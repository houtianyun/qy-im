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

@Slf4j
@Component
public class PullUnreadPrivateMessageTask extends  AbstractPullMessageTask {

    @Autowired
    private WebSocketServer WSServer;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void pullMessage() {
        // 从redis拉取未读消息
        String key = RedisKey.IM_UNREAD_PRIVATE_QUEUE + IMServerGroup.serverId;
        List recvInfos = redisTemplate.opsForList().range(key,0,-1);
        for(Object o: recvInfos){
            redisTemplate.opsForList().leftPop(key);
            IMRecvInfo recvInfo = (IMRecvInfo)o;
            AbstractMessageProcessor processor = ProcessorFactory.createProcessor(IMCmdType.PRIVATE_MESSAGE);
            processor.process(recvInfo);

        }
    }
}
