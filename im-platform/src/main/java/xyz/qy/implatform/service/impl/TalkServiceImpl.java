package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.dto.TalkAddDTO;
import xyz.qy.implatform.dto.TalkQueryDTO;
import xyz.qy.implatform.entity.Talk;
import xyz.qy.implatform.mapper.TalkMapper;
import xyz.qy.implatform.service.ITalkService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.TalkVO;

/**
 * @description: 说说
 * @author: HouTianYun
 * @create: 2023-11-19 21:39
 **/
@Service
public class TalkServiceImpl extends ServiceImpl<TalkMapper, Talk> implements ITalkService {
    @Override
    public void addTalk(TalkAddDTO talkAddDTO) {
        UserSession session = SessionContext.getSession();
        Talk talk = BeanUtils.copyProperties(talkAddDTO, Talk.class);
        assert talk != null;
        talk.setUserId(session.getId());
        talk.setCreateBy(session.getId());
        if (CollectionUtils.isNotEmpty(talkAddDTO.getImgUrls())) {
            talk.setImgUrl(String.join(Constant.COMMA, talkAddDTO.getImgUrls()));
        }
        this.baseMapper.insert(talk);
    }

    @Override
    public PageResultVO<TalkVO> pageQueryTalkList(TalkQueryDTO dto) {
        return null;
    }
}
