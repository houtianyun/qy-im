package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.TalkAddDTO;
import xyz.qy.implatform.dto.TalkDelDTO;
import xyz.qy.implatform.dto.TalkUpdateDTO;
import xyz.qy.implatform.entity.Talk;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.TalkVO;

/**
 * @description: 动态
 * @author: HouTianYun
 * @create: 2023-11-19 21:38
 **/
public interface ITalkService extends IService<Talk> {

    void addTalk(TalkAddDTO talkAddDTO);

    void updateTalk(TalkUpdateDTO talkUpdateDTO);

    PageResultVO pageQueryTalkList();

    void delTalk(TalkDelDTO talkDelDTO);

    TalkVO getTalkDetail(Long talkId);

    /**
     * 验证用户评论角色是否异常
     *
     * @param talkId
     * @param characterId
     * @return
     */
    boolean verifyTalkCommentCharacter(Long talkId, Long characterId);
}
