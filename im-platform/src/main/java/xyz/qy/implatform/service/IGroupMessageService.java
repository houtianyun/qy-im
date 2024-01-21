package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.GroupMessageDTO;
import xyz.qy.implatform.entity.GroupMessage;
import xyz.qy.implatform.vo.GroupMessageVO;

import java.util.List;


public interface IGroupMessageService extends IService<GroupMessage> {


    Long sendMessage(GroupMessageDTO dto);

    void sendGroupMessage(GroupMessageDTO dto, Long sendUserId);

    void recallMessage(Long id);

    void pullUnreadMessage();

    List<GroupMessageVO> loadMessage(Long minId);

    void readedMessage(Long groupId);

    List<GroupMessageVO> findHistoryMessage(Long groupId, Long page, Long size);
}
