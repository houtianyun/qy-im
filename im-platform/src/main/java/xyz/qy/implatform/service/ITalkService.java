package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.TalkAddDTO;
import xyz.qy.implatform.dto.TalkQueryDTO;
import xyz.qy.implatform.entity.Talk;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.TalkVO;

/**
 * @description: 说说
 * @author: HouTianYun
 * @create: 2023-11-19 21:38
 **/
public interface ITalkService extends IService<Talk> {

    void addTalk(TalkAddDTO talkAddDTO);

    PageResultVO<TalkVO> pageQueryTalkList(TalkQueryDTO dto);
}
