package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.PrivateMessageDTO;
import xyz.qy.implatform.entity.PrivateMessage;
import xyz.qy.implatform.vo.PrivateMessageVO;

import java.util.List;


public interface IPrivateMessageService extends IService<PrivateMessage> {

    Long sendMessage(PrivateMessageDTO dto);

    void sendPrivateMessage(PrivateMessageDTO dto, Long sendUserId);

    void recallMessage(Long id);

    List<PrivateMessageVO> findHistoryMessage(Long friendId, Long page,Long size);

    void pullUnreadMessage();

    List<PrivateMessageVO> loadMessage(Long minId);

    void readedMessage(Long friendId);

    /**
     *  获取某个会话中已读消息的最大id
     *
     * @param friendId 好友id
     */
    Long getMaxReadedId(Long friendId);
}
