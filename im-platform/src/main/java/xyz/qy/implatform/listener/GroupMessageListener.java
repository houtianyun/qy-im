package xyz.qy.implatform.listener;

import xyz.qy.imclient.annotation.IMListener;
import xyz.qy.imclient.listener.MessageListener;
import xyz.qy.imcommon.enums.IMListenerType;
import xyz.qy.imcommon.enums.IMSendCode;
import xyz.qy.imcommon.model.GroupMessageInfo;
import xyz.qy.imcommon.model.SendResult;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.enums.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@IMListener(type = IMListenerType.GROUP_MESSAGE)
public class GroupMessageListener implements MessageListener {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void process(SendResult result){
        GroupMessageInfo messageInfo = (GroupMessageInfo) result.getMessageInfo();
        if(messageInfo.getType().equals(MessageType.TIP.code())){
            // 提示类数据不记录
            return;
        }

        // 保存该用户已拉取的最大消息id
        if(result.getCode().equals(IMSendCode.SUCCESS)) {
            String key = RedisKey.IM_GROUP_READED_POSITION + messageInfo.getGroupId() + ":" + result.getRecvId();
            redisTemplate.opsForValue().set(key, messageInfo.getId());
        }
    }
}
