package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.imclient.annotation.Lock;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.dto.TalkStarDTO;
import xyz.qy.implatform.entity.Talk;
import xyz.qy.implatform.entity.TalkStar;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.TalkStarMapper;
import xyz.qy.implatform.service.ITalkService;
import xyz.qy.implatform.service.ITalkStarService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.TalkStarVO;

import java.util.Objects;

/**
 * @description: 动态赞星
 * @author: HouTianYun
 * @create: 2023-12-24 15:39
 **/
@Service
public class TalkStarServiceImpl extends ServiceImpl<TalkStarMapper, TalkStar> implements ITalkStarService {
    @Autowired
    private ITalkService talkService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ITemplateCharacterService templateCharacterService;

    @Transactional
    @Lock(prefix = "im:talk:comment", key = "#talkStarDTO.getTalkId()")
    @Override
    public TalkStarVO like(TalkStarDTO talkStarDTO) {
        UserSession session = SessionContext.getSession();
        Long myUserId = session.getUserId();

        User user = userService.getById(myUserId);

        Long talkId = talkStarDTO.getTalkId();
        Talk talk = talkService.getById(talkId);
        if (Objects.isNull(talk) || talk.getDeleted()) {
            throw new GlobalException("当前动态已被删除");
        }
        if (!Objects.isNull(talkStarDTO.getCharacterId())) {
            if (talkService.verifyTalkCommentCharacter(talkId, talkStarDTO.getCharacterId())) {
                throw new GlobalException("只能使用选择过的角色");
            }
        }
        TalkStar talkStar = new TalkStar();
        talkStar.setTalkId(talkId);
        talkStar.setUserId(myUserId);
        talkStar.setCreateBy(myUserId);
        talkStar.setAnonymous(talkStarDTO.getAnonymous());
        if (!Objects.isNull(talkStarDTO.getCharacterId())) {
            TemplateCharacter templateCharacter = templateCharacterService.getById(talkStarDTO.getCharacterId());
            if (Objects.isNull(templateCharacter)) {
                throw new GlobalException("当前角色不存在");
            }
            talkStar.setCharacterId(templateCharacter.getId());
            talkStar.setNickname(talkStarDTO.getNickname());
            talkStar.setAvatar(talkStarDTO.getAvatar());
        } else {
            talkStar.setAvatar(user.getHeadImage());
        }
        if (talkStarDTO.getAnonymous() && Objects.isNull(talkStarDTO.getCharacterId())) {
            talkStar.setNickname(Constant.ANONYMOUS_NICK_NAME);
        } else if (!talkStarDTO.getAnonymous() && Objects.isNull(talkStarDTO.getCharacterId())){
            talkStar.setNickname(user.getNickName());
        }

        this.save(talkStar);
        TalkStarVO talkStarVO = BeanUtils.copyProperties(talkStar, TalkStarVO.class);
        if (talkStarVO.getAnonymous()) {
            talkStarVO.setUserId(Constant.ANONYMOUS_USER_ID);
        }
        talkStarVO.setIsOwner(Boolean.TRUE);
        return talkStarVO;
    }

    @Lock(prefix = "im:talk:comment", key = "#talkStarDTO.getTalkId()")
    @Override
    public void cancelLike(TalkStarDTO talkStarDTO) {
        UserSession session = SessionContext.getSession();
        Long myUserId = session.getUserId();
        LambdaQueryWrapper<TalkStar> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TalkStar::getTalkId, talkStarDTO.getTalkId());
        wrapper.eq(TalkStar::getUserId, myUserId);
        baseMapper.delete(wrapper);
    }
}
