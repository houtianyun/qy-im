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
import xyz.qy.implatform.entity.TalkComment;
import xyz.qy.implatform.entity.TalkStar;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.TalkMapper;
import xyz.qy.implatform.service.IFriendService;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.ITalkCommentService;
import xyz.qy.implatform.service.ITalkService;
import xyz.qy.implatform.service.ITalkStarService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.TalkCommentVO;
import xyz.qy.implatform.vo.TalkStarVO;
import xyz.qy.implatform.vo.TalkVO;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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

    @Autowired
    private ITalkStarService talkStarService;

    @Autowired
    private ITalkCommentService talkCommentService;

    @Override
    public void addTalk(TalkAddDTO talkAddDTO) {
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getUserId());
        Talk talk = BeanUtils.copyProperties(talkAddDTO, Talk.class);
        assert talk != null;
        talk.setUserId(session.getUserId());
        talk.setCreateBy(session.getUserId());
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
        Long userId = session.getUserId();
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
        } else {
            talk.setImgUrl(StringUtils.EMPTY);
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
        Long myUserId = session.getUserId();
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
        // 查询当前用户数据
        User user = userService.getById(myUserId);

        // 动态id
        List<Long> talkIds = records.stream().map(Talk::getId).collect(Collectors.toList());

        // 动态赞星数据
        List<TalkStar> talkStarList = talkStarService.lambdaQuery().in(TalkStar::getTalkId, talkIds)
                .eq(TalkStar::getDeleted, false)
                .orderByAsc(TalkStar::getCreateTime)
                .list();

        // 动态评论数据(包含删除的)
        List<TalkComment> allTalkCommentList = talkCommentService.lambdaQuery().in(TalkComment::getTalkId, talkIds)
                .orderByAsc(TalkComment::getCreateTime).list();
        // 根据评论id分组
        Map<Long, TalkComment> allTalkCommentMap = allTalkCommentList.stream().collect(Collectors.toMap(TalkComment::getId, Function.identity(), (key1, key2) -> key2));
        Map<Long, List<TalkComment>> allTalkCommentGroupMap = allTalkCommentList.stream().collect(Collectors.groupingBy(TalkComment::getTalkId));

        // 动态评论数据-未删除
        List<TalkComment> talkCommentList = allTalkCommentList.stream().filter(item -> !item.getDeleted()).collect(Collectors.toList());

        List<TalkStarVO> talkStarVOS = BeanUtils.copyProperties(talkStarList, TalkStarVO.class);

        List<TalkCommentVO> talkCommentVOS = BeanUtils.copyProperties(talkCommentList, TalkCommentVO.class);

        Map<Long, List<TalkStarVO>> talkStarMap = talkStarVOS.stream().collect(Collectors.groupingBy(TalkStarVO::getTalkId));
        Map<Long, List<TalkCommentVO>> talkCommentMap = talkCommentVOS.stream().collect(Collectors.groupingBy(TalkCommentVO::getTalkId));

        List<TalkVO> talkVOS = records.parallelStream().map(obj -> {
            TalkVO talkVO = BeanUtils.copyProperties(obj, TalkVO.class);
            assert talkVO != null;
            if (StringUtils.isNotBlank(obj.getImgUrl())) {
                talkVO.setImgUrls(Arrays.asList(obj.getImgUrl().split(Constant.COMMA)));
            }
            // 当前用户是否发布作者
            if (myUserId.equals(talkVO.getUserId())) {
                talkVO.setIsOwner(Boolean.TRUE);
                talkVO.setCommentAnonymous(talkVO.getAnonymous());
            }
            talkVO.setCommentUserAvatar(user.getHeadImage());
            talkVO.setCommentUserNickname(user.getNickName());
            Set<Long> characterIds = new HashSet<>();
            if (!Objects.isNull(talkVO.getCharacterId())) {
                characterIds.add(talkVO.getCharacterId());
                if (obj.getUserId().equals(myUserId)) {
                    talkVO.setCommentCharacterId(talkVO.getCharacterId());
                    talkVO.setCommentCharacterName(talkVO.getNickName());
                    talkVO.setCommentCharacterAvatar(talkVO.getAvatar());
                }
            }
            if (talkStarMap.containsKey(talkVO.getId())) {
                talkVO.setTalkStarVOS(talkStarMap.get(talkVO.getId()));
                // 找到当前用户点赞，并且角色id不为空的数据
                Optional<TalkStarVO> talkStarVOOptional = talkVO.getTalkStarVOS().stream().filter(item -> item.getUserId().equals(myUserId)
                        && !Objects.isNull(item.getCharacterId())).findFirst();
                talkStarVOOptional.ifPresent(talkStarVO -> {
                    talkVO.setCommentCharacterId(talkStarVO.getCharacterId());
                    talkVO.setCommentCharacterName(talkStarVO.getNickname());
                    talkVO.setCommentCharacterAvatar(talkStarVO.getAvatar());
                    talkVO.setCommentAnonymous(talkStarVO.getAnonymous());
                });
                characterIds.addAll(talkVO.getTalkStarVOS().stream().map(TalkStarVO::getCharacterId).collect(Collectors.toSet()));

                // 判断当前用户是否点赞此条动态
                talkVO.setIsLike(talkVO.getTalkStarVOS().stream()
                        .anyMatch(item -> item.getUserId().equals(myUserId)));

                talkVO.getTalkStarVOS().forEach(item -> {
                    if (myUserId.equals(item.getUserId())) {
                        item.setIsOwner(Boolean.TRUE);
                    }
                    if (item.getAnonymous()) {
                        item.setUserId(Constant.ANONYMOUS_USER_ID);
                    }
                });
            } else {
                talkVO.setTalkStarVOS(Collections.emptyList());
            }
            if (allTalkCommentGroupMap.containsKey(talkVO.getId())) {
                // 找到当前用户评论，并且角色id不为空的数据
                Optional<TalkComment> talkCommentOptional = allTalkCommentGroupMap.get(talkVO.getId()).stream().filter(item -> item.getUserId().equals(myUserId)
                        && !Objects.isNull(item.getCharacterId())).findFirst();
                talkCommentOptional.ifPresent(talkComment -> {
                    talkVO.setCommentCharacterId(talkComment.getCharacterId());
                    talkVO.setCommentCharacterName(talkComment.getUserNickname());
                    talkVO.setCommentCharacterAvatar(talkComment.getUserAvatar());
                    talkVO.setCommentAnonymous(talkComment.getAnonymous());
                });
            }
            if (talkCommentMap.containsKey(talkVO.getId())) {
                talkVO.setTalkCommentVOS(talkCommentMap.get(talkVO.getId()));
                talkVO.getTalkCommentVOS().forEach(item -> {
                    if (myUserId.equals(item.getUserId())) {
                        item.setIsOwner(Boolean.TRUE);
                    }
                    if (item.getAnonymous()) {
                        item.setUserId(Constant.ANONYMOUS_USER_ID);
                    }
                    if (item.getReplyCommentId() != null &&
                            allTalkCommentMap.containsKey(item.getReplyCommentId())) {
                        TalkComment talkComment = allTalkCommentMap.get(item.getReplyCommentId());
                        if (talkComment.getDeleted() || talkComment.getAnonymous()) {
                            item.setReplyUserId(Constant.ANONYMOUS_USER_ID);
                        }
                    }
                });
                characterIds.addAll(talkVO.getTalkCommentVOS().stream().map(TalkCommentVO::getCharacterId).collect(Collectors.toSet()));
            } else {
                talkVO.setTalkCommentVOS(Collections.emptyList());
            }
            talkVO.setSelectedCharacterIds(characterIds);
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
        Long userId = session.getUserId();

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

    @Override
    public boolean verifyTalkCommentCharacter(Long talkId, Long characterId) {
        if (Objects.isNull(talkId) || Objects.isNull(characterId)) {
            throw new GlobalException("参数异常");
        }
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        Talk talk = baseMapper.selectById(talkId);
        if (Objects.isNull(talk) || talk.getDeleted()) {
            throw new GlobalException("当前动态已被删除");
        }
        // 自己选择过的角色与入参的角色不一致
        if (userId.equals(talk.getUserId())
                && !Objects.isNull(talk.getCharacterId())) {
            if (!talk.getCharacterId().equals(characterId)) {
                return true;
            }
        }

        // 自己选择的角色与其他人选择的角色一样
        if (!userId.equals(talk.getUserId())
                && !Objects.isNull(talk.getCharacterId())) {
            if (talk.getCharacterId().equals(characterId)) {
                return true;
            }
        }

        List<TalkStar> talkStarList = talkStarService.lambdaQuery()
                .eq(TalkStar::getTalkId, talkId)
                .eq(TalkStar::getDeleted, false).list();
        if (CollectionUtils.isNotEmpty(talkStarList)) {
            // 自己选择的角色与其他人选择的角色一样
            Optional<TalkStar> optional1 = talkStarList.stream().filter(item -> !Objects.isNull(item.getCharacterId())
                    && characterId.equals(item.getCharacterId())
                    && !item.getUserId().equals(userId)).findFirst();
            if (optional1.isPresent()) {
                return true;
            }

            // 自己选择过的角色与入参的角色不一致
            Optional<TalkStar> optional2 = talkStarList.stream().filter(item -> !Objects.isNull(item.getCharacterId())
                    && !characterId.equals(item.getCharacterId())
                    && item.getUserId().equals(userId)).findFirst();
            if (optional2.isPresent()) {
                return true;
            }
        }

        List<TalkComment> talkCommentList = talkCommentService.lambdaQuery()
                .eq(TalkComment::getTalkId, talkId).list();
        if (CollectionUtils.isNotEmpty(talkCommentList)) {
            // 自己选择的角色与其他人选择的角色一样
            Optional<TalkComment> optional1 = talkCommentList.stream().filter(item -> !Objects.isNull(item.getCharacterId())
                    && characterId.equals(item.getCharacterId())
                    && !item.getUserId().equals(userId)).findFirst();
            if (optional1.isPresent()) {
                return true;
            }

            // 自己选择过的角色与入参的角色不一致
            Optional<TalkComment> optional2 = talkCommentList.stream().filter(item -> !Objects.isNull(item.getCharacterId())
                    && !characterId.equals(item.getCharacterId()) &&
                    item.getUserId().equals(userId)).findFirst();
            if (optional2.isPresent()) {
                return true;
            }
        }
        return false;
    }
}
