package xyz.qy.implatform.listener;

import xyz.qy.imclient.annotation.IMListener;
import xyz.qy.imclient.listener.MessageListener;
import xyz.qy.imcommon.enums.IMListenerType;
import xyz.qy.imcommon.enums.IMSendCode;
import xyz.qy.imcommon.model.IMSendResult;
import xyz.qy.implatform.contant.RedisKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import xyz.qy.implatform.vo.GroupMessageVO;

@Slf4j
@IMListener(type = IMListenerType.GROUP_MESSAGE)
public class GroupMessageListener implements MessageListener<GroupMessageVO> {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void process(IMSendResult<GroupMessageVO> result){
        GroupMessageVO messageInfo = result.getData();
        // 保存该用户已拉取的最大消息id
        if(result.getCode().equals(IMSendCode.SUCCESS.code())) {
            String key = String.join(":",RedisKey.IM_GROUP_READED_POSITION,messageInfo.getGroupId().toString(),result.getReceiver().getId().toString());
            redisTemplate.opsForValue().set(key, messageInfo.getId());
        }
    }
}
