package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.imcommon.model.PrivateMessageInfo;
import xyz.qy.implatform.entity.PrivateMessage;
import xyz.qy.implatform.vo.PrivateMessageVO;

import java.util.List;


public interface IPrivateMessageService extends IService<PrivateMessage> {

    Long sendMessage(PrivateMessageVO vo);

    void sendPrivateMessage(PrivateMessageVO vo, Long sendUserId);

    void recallMessage(Long id);

    List<PrivateMessageInfo> findHistoryMessage(Long friendId, Long page,Long size);

    void pullUnreadMessage();

}
