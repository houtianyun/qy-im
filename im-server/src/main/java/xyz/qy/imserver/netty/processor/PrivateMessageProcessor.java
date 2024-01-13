package xyz.qy.imserver.netty.processor;

import xyz.qy.imcommon.contant.RedisKey;
import xyz.qy.imcommon.enums.IMCmdType;
import xyz.qy.imcommon.enums.IMSendCode;
import xyz.qy.imcommon.model.IMRecvInfo;
import xyz.qy.imcommon.model.IMSendInfo;
import xyz.qy.imcommon.model.SendResult;
import xyz.qy.imserver.netty.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PrivateMessageProcessor extends AbstractMessageProcessor<IMRecvInfo> {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void process(IMRecvInfo recvInfo) {
        Long recvId = recvInfo.getRecvIds().get(0);
        log.info("接收到消息，发送者:{},接收者:{}，内容:{}",recvInfo.getSendId(),recvId,recvInfo.getData());
        try{
            ChannelHandlerContext channelCtx = UserChannelCtxMap.getChannelCtx(recvId,recvInfo.getRecvTerminal());
            if(channelCtx != null ){
                // 推送消息到用户
                IMSendInfo sendInfo = new IMSendInfo();
                sendInfo.setCmd(IMCmdType.PRIVATE_MESSAGE.code());
                sendInfo.setData(recvInfo.getData());
                channelCtx.channel().writeAndFlush(sendInfo);
                // 消息发送成功确认
                sendResult(recvInfo,IMSendCode.SUCCESS);
            }else{
                // 消息推送失败确认
                sendResult(recvInfo,IMSendCode.NOT_FIND_CHANNEL);
                log.error("未找到WS连接，发送者:{},接收者:{}，内容:{}",recvInfo.getSendId(),recvId,recvInfo.getData());
            }
        }catch (Exception e){
            // 消息推送失败确认
            sendResult(recvInfo,IMSendCode.UNKONW_ERROR);
            log.error("发送异常，发送者:{},接收者:{}，内容:{}",recvInfo.getSendId(),recvId,recvInfo.getData(),e);
        }

    }

    private void sendResult(IMRecvInfo recvInfo,IMSendCode sendCode){
        if(recvInfo.getSendResult()) {
            String key = RedisKey.IM_RESULT_PRIVATE_QUEUE;
            SendResult result = new SendResult();
            result.setRecvId(recvInfo.getRecvIds().get(0));
            result.setCode(sendCode.code());
            result.setData(recvInfo.getData());
            redisTemplate.opsForList().rightPush(key, result);
        }
    }
}
