package xyz.qy.imserver.netty.processor;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xyz.qy.imcommon.contant.RedisKey;
import xyz.qy.imcommon.enums.IMCmdType;
import xyz.qy.imcommon.enums.IMSendCode;
import xyz.qy.imcommon.model.IMRecvInfo;
import xyz.qy.imcommon.model.IMSendInfo;
import xyz.qy.imcommon.model.SendResult;
import xyz.qy.imserver.netty.UserChannelCtxMap;

import java.util.List;

@Slf4j
@Component
public class GroupMessageProcessor extends AbstractMessageProcessor<IMRecvInfo> {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Async
    @Override
    public void process(IMRecvInfo recvInfo) {
        Object data = recvInfo.getData();
        List<Long> recvIds = recvInfo.getRecvIds();
        log.info("接收到群消息，发送者:{},接收id:{}，内容:{}",recvInfo.getSendId(),recvIds,data);
        for(Long recvId:recvIds){
            try {
                ChannelHandlerContext channelCtx = UserChannelCtxMap.getChannelCtx(recvId,recvInfo.getRecvTerminal());
                if(channelCtx != null){
                    // 推送消息到用户
                    IMSendInfo sendInfo = new IMSendInfo();
                    sendInfo.setCmd(IMCmdType.GROUP_MESSAGE.code());
                    sendInfo.setData(data);
                    channelCtx.channel().writeAndFlush(sendInfo);
                    // 消息发送成功确认
                    String key = RedisKey.IM_RESULT_GROUP_QUEUE;
                    SendResult sendResult = new SendResult();
                    sendResult.setRecvId(recvId);
                    sendResult.setCode(IMSendCode.SUCCESS.code());
                    sendResult.setData(data);
                    redisTemplate.opsForList().rightPush(key,sendResult);

                }else {
                    // 消息发送失败确认
                    String key = RedisKey.IM_RESULT_GROUP_QUEUE;
                    SendResult sendResult = new SendResult();
                    sendResult.setRecvId(recvId);
                    sendResult.setCode(IMSendCode.NOT_FIND_CHANNEL.code());
                    sendResult.setData(data);
                    redisTemplate.opsForList().rightPush(key,sendResult);
                    log.error("未找到WS连接,发送者:{},接收id:{}，内容:{}",recvInfo.getSendId(),recvId,data);
                }
            }catch (Exception e){
                // 消息发送失败确认
                String key = RedisKey.IM_RESULT_GROUP_QUEUE;
                SendResult sendResult = new SendResult();
                sendResult.setRecvId(recvId);
                sendResult.setCode(IMSendCode.UNKONW_ERROR.code());
                sendResult.setData(data);
                redisTemplate.opsForList().rightPush(key,sendResult);
                log.error("发送消息异常,发送者:{},接收id:{}，内容:{}",recvInfo.getSendId(),recvId,data);
            }
        }
    }

}
