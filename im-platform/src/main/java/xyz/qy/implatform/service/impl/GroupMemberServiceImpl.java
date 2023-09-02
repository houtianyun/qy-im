package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.qy.imclient.annotation.Lock;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.entity.CharacterAvatar;
import xyz.qy.implatform.entity.Group;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.GroupMemberMapper;
import xyz.qy.implatform.service.ICharacterAvatarService;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.IGroupService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.vo.GroupMemberVO;
import xyz.qy.implatform.vo.SwitchCharacterAvatarVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@CacheConfig(cacheNames = RedisKey.IM_CACHE_GROUP_MEMBER_ID)
@Service
public class GroupMemberServiceImpl extends ServiceImpl<GroupMemberMapper, GroupMember> implements IGroupMemberService {
    @Autowired
    private IGroupService groupService;

    @Autowired
    private ITemplateCharacterService templateCharacterService;

    @Autowired
    private ICharacterAvatarService characterAvatarService;

    /**
     * 添加群聊成员
     *
     * @param member 成员
     * @return
     */
    @CacheEvict(key="#member.getGroupId()")
    @Override
    public boolean save(GroupMember member) {
        return super.save(member);
    }

    /**
     * 批量添加成员
     *
     * @param groupId 群聊id
     * @param members 成员列表
     * @return
     */
    @CacheEvict(key="#groupId")
    @Override
    public boolean saveOrUpdateBatch(Long groupId,List<GroupMember> members) {
        return super.saveOrUpdateBatch(members);
    }

    /**
     * 根据群聊id和用户id查询群聊成员
     *
     * @param groupId 群聊id
     * @param userId 用户id
     * @return
     */
    @Override
    public GroupMember findByGroupAndUserId(Long groupId, Long userId) {
        QueryWrapper<GroupMember> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(GroupMember::getGroupId,groupId)
                .eq(GroupMember::getUserId,userId);
        return this.getOne(wrapper);
    }

    /**
     * 根据用户id查询群聊成员
     *
     * @param userId 用户id
     * @return
     */
    @Override
    public List<GroupMember> findByUserId(Long userId) {
        QueryWrapper<GroupMember> memberWrapper = new QueryWrapper();
        memberWrapper.lambda().eq(GroupMember::getUserId, userId)
                .eq(GroupMember::getQuit,false);
        return this.list(memberWrapper);
    }

    /**
     * 根据群聊id查询群聊成员（包括已退出）
     *
     * @param groupId 群聊id
     * @return
     */
    @Override
    public List<GroupMember> findByGroupId(Long groupId) {
        QueryWrapper<GroupMember> memberWrapper = new QueryWrapper();
        memberWrapper.lambda().eq(GroupMember::getGroupId, groupId);
        return this.list(memberWrapper);
    }

    /**
     * 根据群聊id查询未推出群聊成员
     *
     * @param groupId 群聊id
     * @return 未退出群聊成员
     */
    @Override
    public List<GroupMember> findNoQuitGroupMembers(Long groupId) {
        return this.findByGroupId(groupId).stream()
                .filter(item -> !item.getQuit()).collect(Collectors.toList());
    }

    /**
     * 根据群聊id查询没有退出的群聊成员id
     *
     * @param groupId 群聊id
     * @return
     */
    @Cacheable(key="#groupId")
    @Override
    public List<Long> findUserIdsByGroupId(Long groupId) {
        QueryWrapper<GroupMember> memberWrapper = new QueryWrapper();
        memberWrapper.lambda().eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getQuit,false);
        List<GroupMember> members = this.list(memberWrapper);
        return members.stream().map(GroupMember::getUserId).collect(Collectors.toList());
    }

    /**
     *根据群聊id删除移除成员
     *
     * @param groupId  群聊id
     * @return
     */
    @CacheEvict(key = "#groupId")
    @Override
    public void removeByGroupId(Long groupId) {
        UpdateWrapper<GroupMember> wrapper = new UpdateWrapper();
        wrapper.lambda().eq(GroupMember::getGroupId,groupId)
                .set(GroupMember::getQuit,true);
        this.update(wrapper);
    }

    /**
     *根据群聊id和用户id移除成员
     *
     * @param groupId  群聊id
     * @param userId  用户id
     * @return
     */
    @CacheEvict(key = "#groupId")
    @Override
    public void removeByGroupAndUserId(Long groupId, Long userId) {
        UpdateWrapper<GroupMember> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(GroupMember::getGroupId,groupId)
                .eq(GroupMember::getUserId,userId)
                .set(GroupMember::getQuit,true);
        this.update(wrapper);
    }

    @CacheEvict(key = "#groupMemberVO.getGroupId()")
    @Override
    @Lock(prefix = "im:group:member:modify", key = "#groupMemberVO.getGroupId()")
    public void switchTemplateCharacter(GroupMemberVO groupMemberVO) {
        UserSession session = SessionContext.getSession();
        // 查询当前用户的群用户信息
        QueryWrapper<GroupMember> wrapper = new QueryWrapper();
        wrapper.lambda().eq(GroupMember::getGroupId, groupMemberVO.getGroupId())
        .eq(GroupMember::getUserId, session.getId());
        GroupMember groupMember = this.getOne(wrapper);
        if (ObjectUtil.isNull(groupMember) || groupMember.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面，无法切换模板人物");
        }

        // 查询群信息
        Group group = groupService.getById(groupMemberVO.getGroupId());
        if(group == null || group.getDeleted()){
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊不存在");
        }
        if (group.getIsTemplate() == Constant.NO) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "当前群聊不是模板群聊");
        }
        if (!group.getTemplateGroupId().equals(groupMemberVO.getTemplateGroupId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊类型已切换");
        }

        // 判断上次切换时间与当前时间间隔
        Date switchTime = groupMember.getSwitchTime();
        if (switchTime != null) {
            long interval = (new Date().getTime() - switchTime.getTime()) / 1000;
            if (interval < Constant.SWITCH_INTERVAL) {
                throw new GlobalException("距离上次切换模板人物未超过30分钟");
            }
        }

        // 判断所选模板人物是否属于当前模板群聊
        List<TemplateCharacter> templateCharacterList = templateCharacterService.lambdaQuery()
                .eq(TemplateCharacter::getTemplateGroupId, group.getTemplateGroupId())
                .list();
        List<Long> templateCharacterIds = templateCharacterList.stream().map(TemplateCharacter::getId).collect(Collectors.toList());
        if (!templateCharacterIds.contains(groupMemberVO.getTemplateCharacterId())) {
            throw new GlobalException("当前模板群聊不存在所选模板人物或模板群聊类型已切换");
        }

        // 查询新的模板人物是否已存在群聊里
        LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupMember::getGroupId, groupMemberVO.getGroupId())
                .eq(GroupMember::getTemplateCharacterId, groupMemberVO.getTemplateCharacterId())
                .eq(GroupMember::getQuit, false);
        List<GroupMember> groupMemberList = this.list(memberWrapper);
        if (CollectionUtils.isNotEmpty(groupMemberList)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "当前模板人物已有用户选择");
        }
        groupMember.setTemplateCharacterId(groupMemberVO.getTemplateCharacterId());
        groupMember.setAliasName(groupMemberVO.getTemplateCharacterName());
        groupMember.setHeadImage(groupMemberVO.getTemplateCharacterAvatar());
        groupMember.setSwitchTime(new Date());

        this.saveOrUpdateBatch(group.getId(), Collections.singletonList(groupMember));
    }

    @CacheEvict(key = "#avatarVO.getGroupId()")
    @Override
    @Lock(prefix = "im:group:member:modify", key = "#avatarVO.getGroupId()")
    public void switchCharacterAvatar(SwitchCharacterAvatarVO avatarVO) {
        UserSession session = SessionContext.getSession();
        // 查询当前用户的群用户信息
        QueryWrapper<GroupMember> wrapper = new QueryWrapper();
        wrapper.lambda().eq(GroupMember::getGroupId, avatarVO.getGroupId())
                .eq(GroupMember::getUserId, session.getId());
        GroupMember groupMember = this.getOne(wrapper);
        if (ObjectUtil.isNull(groupMember) || groupMember.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面，无法切换模板人物头像");
        }

        // 查询群信息
        Group group = groupService.getById(avatarVO.getGroupId());
        if(group == null || group.getDeleted()){
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊不存在");
        }
        if (group.getIsTemplate() == Constant.NO) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "当前群聊不是模板群聊");
        }
        if (!group.getTemplateGroupId().equals(avatarVO.getTemplateGroupId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊类型已切换");
        }
        // 判断模板人物是否属于当前模板群聊
        TemplateCharacter templateCharacter = templateCharacterService.getById(avatarVO.getTemplateCharacterId());
        if (!group.getTemplateGroupId().equals(templateCharacter.getTemplateGroupId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "当前模板群聊不存在该模板人物");
        }
        // 判断模板人物头像是否属于模板人物
        CharacterAvatar characterAvatar = characterAvatarService.getById(avatarVO.getCharacterAvatarId());
        if (!avatarVO.getTemplateCharacterId().equals(characterAvatar.getTemplateCharacterId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "当前模板人物不存在该模板人物头像");
        }

        groupMember.setCharacterAvatarId(avatarVO.getCharacterAvatarId());
        groupMember.setHeadImage(characterAvatar.getAvatar());
        if (characterAvatar.getLevel() == 1) {
            groupMember.setAliasName(characterAvatar.getName());
        } else {
            groupMember.setAliasName(characterAvatar.getTemplateCharacterName());
        }
        groupMember.setAvatarAlias(characterAvatar.getName());

        this.saveOrUpdateBatch(group.getId(), Collections.singletonList(groupMember));
    }
}
