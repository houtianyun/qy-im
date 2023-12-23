package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.dto.TalkAddDTO;
import xyz.qy.implatform.dto.TalkDelDTO;
import xyz.qy.implatform.dto.TalkQueryDTO;
import xyz.qy.implatform.dto.TalkUpdateDTO;
import xyz.qy.implatform.entity.Talk;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.TalkMapper;
import xyz.qy.implatform.service.IFriendService;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.ITalkService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.TalkVO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description: 动态
 * @author: HouTianYun
 * @create: 2023-11-19 21:39
 **/
@Slf4j
@Service
public class TalkServiceImpl extends ServiceImpl<TalkMapper, Talk> implements ITalkService {
    @Autowired
    private IUserService userService;

    @Autowired
    private IFriendService friendService;

    @Autowired
    private IGroupMemberService groupMemberService;

    @Autowired
    private ITemplateCharacterService characterService;

    @Override
    public void addTalk(TalkAddDTO talkAddDTO) {
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getId());
        Talk talk = BeanUtils.copyProperties(talkAddDTO, Talk.class);
        assert talk != null;
        talk.setUserId(session.getId());
        talk.setCreateBy(session.getId());
        talk.setAddress(user.getProvince());
        if (CollectionUtils.isNotEmpty(talkAddDTO.getImgUrls())) {
            talk.setImgUrl(String.join(Constant.COMMA, talkAddDTO.getImgUrls()));
        }
        if (!Objects.isNull(talkAddDTO.getCharacterId())) {
            TemplateCharacter character = characterService.getById(talkAddDTO.getCharacterId());
            if (Objects.isNull(character)) {
                throw new GlobalException("角色不存在");
            }
        }
        if (talk.getAnonymous()) {
            if (StringUtils.isBlank(talk.getNickName())) {
                talk.setNickName(Constant.ANONYMOUS_NICK_NAME);
            }
        } else {
            talk.setNickName(StringUtils.isNotBlank(talkAddDTO.getNickName()) ? talkAddDTO.getNickName() : user.getNickName());
            if (StringUtils.isBlank(talk.getAvatar())) {
                talk.setAvatar(user.getHeadImage());
            }
        }

        this.baseMapper.insert(talk);
    }

    @Override
    public void updateTalk(TalkUpdateDTO talkUpdateDTO) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getId();
        User user = userService.getById(userId);

        Talk talk = this.baseMapper.selectById(talkUpdateDTO.getId());
        if (!Objects.isNull(talk.getCharacterId())
                && !talk.getCharacterId().equals(talkUpdateDTO.getCharacterId())) {
            throw new GlobalException("角色不能修改");
        }
        if (!Objects.isNull(talkUpdateDTO.getCharacterId())) {
            TemplateCharacter character = characterService.getById(talkUpdateDTO.getCharacterId());
            if (Objects.isNull(character)) {
                throw new GlobalException("角色不存在");
            }
        }
        BeanUtils.copyProperties(talkUpdateDTO, talk);
        talk.setAddress(user.getProvince());
        talk.setUpdateBy(userId);
        if (CollectionUtils.isNotEmpty(talkUpdateDTO.getImgUrls())) {
            talk.setImgUrl(String.join(Constant.COMMA, talkUpdateDTO.getImgUrls()));
        }
        if (talk.getAnonymous()) {
            if (StringUtils.isBlank(talk.getNickName())) {
                talk.setNickName(Constant.ANONYMOUS_NICK_NAME);
            }
        } else {
            talk.setNickName(StringUtils.isNotBlank(talkUpdateDTO.getNickName()) ? talkUpdateDTO.getNickName() : user.getNickName());
            if (StringUtils.isBlank(talk.getAvatar())) {
                talk.setAvatar(user.getHeadImage());
            }
        }
        this.baseMapper.updateById(talk);
    }

    @Override
    public PageResultVO pageQueryTalkList() {
        TalkQueryDTO dto = new TalkQueryDTO();
        UserSession session = SessionContext.getSession();
        Long myUserId = session.getId();
        dto.setOwnerId(myUserId);

        // 查询获取好友用户id
        List<Long> friendIds = friendService.getFriendIdsByUserId(myUserId);
        dto.setFriendIds(CollectionUtils.isEmpty(friendIds) ? Collections.singletonList(Constant.ANONYMOUS_USER_ID) : friendIds);

        // 查询获取用户所在群其他用户id
        List<Long> groupMemberIds = groupMemberService.getAllGroupMemberIdsByUserId(myUserId);
        dto.setGroupMemberIds(CollectionUtils.isEmpty(groupMemberIds) ? Collections.singletonList(Constant.ANONYMOUS_USER_ID) : groupMemberIds);

        Page<Talk> talkPage = this.baseMapper.pageQueryAllTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), dto);
        List<Talk> records = talkPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return PageResultVO.builder().data(Collections.emptyList()).build();
        }
        List<TalkVO> talkVOS = records.stream().map(obj -> {
            TalkVO talkVO = BeanUtils.copyProperties(obj, TalkVO.class);
            assert talkVO != null;
            if (StringUtils.isNotBlank(obj.getImgUrl())) {
                talkVO.setImgUrls(Arrays.asList(obj.getImgUrl().split(Constant.COMMA)));
            }
            if (myUserId.equals(talkVO.getUserId())) {
                talkVO.setIsOwner(Boolean.TRUE);
            }
            if (talkVO.getAnonymous()) {
                talkVO.setUserId(Constant.ANONYMOUS_USER_ID);
                talkVO.setCreateBy(Constant.ANONYMOUS_USER_ID);
                talkVO.setUpdateBy(Constant.ANONYMOUS_USER_ID);
            }
            return talkVO;
        }).collect(Collectors.toList());

        return PageResultVO.builder().data(talkVOS).total(talkPage.getTotal()).build();
    }


    @Override
    public void delTalk(TalkDelDTO talkDelDTO) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getId();

        Talk talk = this.baseMapper.selectById(talkDelDTO.getId());
        if (Objects.isNull(talk)) {
            throw new GlobalException("当前动态不存在");
        }
        if (!userId.equals(talk.getUserId())) {
            throw new GlobalException("您不是当前动态作者");
        }
        talk.setDeleted(Boolean.TRUE);
        boolean update = this.updateById(talk);
        if (update) {
            log.info("成功删除动态,talkId={}：", talk.getId());
        } else {
            log.info("删除动态失败,talkId={}：", talk.getId());
        }
    }

    @Override
    public TalkVO getTalkDetail(Long talkId) {
        Talk talk = this.baseMapper.selectById(talkId);
        TalkVO talkVO = BeanUtils.copyProperties(talk, TalkVO.class);
        assert talkVO != null;
        if (StringUtils.isNotBlank(talk.getImgUrl())) {
            talkVO.setImgUrls(Arrays.asList(talk.getImgUrl().split(Constant.COMMA)));
        }
        if (Objects.isNull(talk.getCharacterId())) {
            talkVO.setEnableCharacterChoose(Boolean.TRUE);
        }

        return talkVO;
    }
}
