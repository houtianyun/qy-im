package xyz.qy.imclient.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import xyz.qy.imclient.task.PrivateMessageResultResultTask;

/**
 * @description: 私聊消息发送结果redis通道监听
 * @author: HouTianYun
 * @create: 2023-09-09 09:50
 **/
public class PrivateMsgSendResultChannelListener implements MessageListener {
    @Autowired
    private PrivateMessageResultResultTask privateMessageResultResultTask;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        privateMessageResultResultTask.pullMessage();
    }
}
