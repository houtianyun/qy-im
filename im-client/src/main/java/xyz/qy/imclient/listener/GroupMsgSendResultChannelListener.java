package xyz.qy.imclient.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import xyz.qy.imclient.task.GroupMessageResultResultTask;

/**
 * @description: 群聊消息发送结果redis通道监听
 * @author: HouTianYun
 * @create: 2023-09-09 09:45
 **/
public class GroupMsgSendResultChannelListener implements MessageListener {
    @Autowired
    private GroupMessageResultResultTask groupMessageResultResultTask;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        groupMessageResultResultTask.pullMessage();
    }
}
