package xyz.qy.imserver.netty.processor;

import xyz.qy.imcommon.contant.Constant;
import xyz.qy.imcommon.contant.RedisKey;
import xyz.qy.imcommon.enums.IMCmdType;
import xyz.qy.imcommon.enums.IMSendCode;
import xyz.qy.imcommon.model.IMRecvInfo;
import xyz.qy.imcommon.model.IMSendInfo;
import xyz.qy.imcommon.model.PrivateMessageInfo;
import xyz.qy.imcommon.model.SendResult;
import xyz.qy.imserver.netty.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PrivateMessageProcessor extends  MessageProcessor<IMRecvInfo<PrivateMessageInfo>> {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void process(IMRecvInfo<PrivateMessageInfo> recvInfo) {
        PrivateMessageInfo messageInfo = recvInfo.getData();
        Long recvId = recvInfo.getRecvIds().get(0);
        log.info("接收到消息，发送者:{},接收者:{}，内容:{}",messageInfo.getSendId(),recvId,messageInfo.getContent());
        String key = RedisKey.IM_RESULT_PRIVATE_QUEUE;
        try{
            ChannelHandlerContext channelCtx = UserChannelCtxMap.getChannelCtx(recvId);
            if(channelCtx != null ){
                // 推送消息到用户
                IMSendInfo sendInfo = new IMSendInfo();
                sendInfo.setCmd(IMCmdType.PRIVATE_MESSAGE.code());
                sendInfo.setData(messageInfo);
                channelCtx.channel().writeAndFlush(sendInfo);
                // 消息发送成功确认
                SendResult sendResult = new SendResult();
                sendResult.setRecvId(recvId);
                sendResult.setCode(IMSendCode.SUCCESS);
                sendResult.setMessageInfo(messageInfo);
                redisTemplate.opsForList().rightPush(key,sendResult);
            }else{
                // 消息推送失败确认
                SendResult sendResult = new SendResult();
                sendResult.setRecvId(recvId);
                sendResult.setCode(IMSendCode.NOT_FIND_CHANNEL);
                sendResult.setMessageInfo(messageInfo);
                redisTemplate.opsForList().rightPush(key,sendResult);
                log.error("未找到WS连接，发送者:{},接收者:{}，内容:{}",messageInfo.getSendId(),recvId,messageInfo.getContent());
            }
        } catch (Exception e) {
            // 消息推送失败确认
            SendResult sendResult = new SendResult();
            sendResult.setRecvId(recvId);
            sendResult.setCode(IMSendCode.UNKONW_ERROR);
            sendResult.setMessageInfo(messageInfo);
            redisTemplate.opsForList().rightPush(key,sendResult);
            log.error("发送异常，发送者:{},接收者:{}，内容:{}",messageInfo.getSendId(),recvId,messageInfo.getContent(),e);
        } finally {
            redisTemplate.convertAndSend(Constant.PRIVATE_MSG_SEND_RESULT_TOPIC, key);
        }
    }
}
